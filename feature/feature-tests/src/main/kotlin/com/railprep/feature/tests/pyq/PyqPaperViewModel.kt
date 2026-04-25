package com.railprep.feature.tests.pyq

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestKind
import com.railprep.domain.repository.TestsRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.learn.pdf.PdfCache
import com.railprep.feature.learn.pdf.PdfDownloadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PyqPaper"

enum class PyqPaperFailure { NETWORK, UNSUPPORTED, NOT_A_PYQ_LINK, NOT_FOUND }

data class PyqPaperUiState(
    val loading: Boolean = true,
    val test: Test? = null,
    val pdfFile: File? = null,
    val pdfDownloading: Boolean = false,
    val failure: PyqPaperFailure? = null,
)

/**
 * Renders an external PYQ paper PDF (adda247-hosted) on-device.
 * Reuses [PdfCache] + PdfViewer from feature-learn — the same pipeline
 * that serves NCERT textbooks. No extraction, no scoring — the PDF has
 * the answer key baked in (correct options are ticked green by adda247).
 */
@HiltViewModel
class PyqPaperViewModel @Inject constructor(
    private val app: Application,
    private val testsRepository: TestsRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PyqPaperUiState())
    val state: StateFlow<PyqPaperUiState> = _state.asStateFlow()

    private val pdfCache = PdfCache(app)

    fun load(testId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failure = null, pdfFile = null) }
            when (val r = testsRepository.get(testId)) {
                is DomainResult.Success -> {
                    val test = r.value
                    if (test.kind != TestKind.PYQ_LINK || test.externalUrl.isNullOrBlank()) {
                        _state.update {
                            it.copy(loading = false, test = test, failure = PyqPaperFailure.NOT_A_PYQ_LINK)
                        }
                        Log.w(TAG, "load: test $testId is not a PYQ_LINK (kind=${test.kind})")
                        return@launch
                    }
                    _state.update { it.copy(loading = false, test = test) }
                    ensurePdfDownloaded(test)
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, failure = PyqPaperFailure.NOT_FOUND)
                }
            }
        }
    }

    fun retryPdf() {
        val test = _state.value.test ?: return
        if (test.externalUrl.isNullOrBlank()) return
        viewModelScope.launch { ensurePdfDownloaded(test) }
    }

    private suspend fun ensurePdfDownloaded(test: Test) {
        val url = test.externalUrl ?: return
        // Prefix the cache key so PYQ PDFs don't collide with NCERT topic PDFs
        // (topic ids and test ids are both uuids, but logically different domains).
        val cacheKey = "pyq-${test.id}"
        _state.update { it.copy(pdfDownloading = true, failure = null, pdfFile = null) }
        when (val r = pdfCache.downloadIfMissing(cacheKey, url)) {
            is PdfDownloadResult.Success -> _state.update {
                it.copy(pdfDownloading = false, pdfFile = r.file, failure = null)
            }
            is PdfDownloadResult.BadContent -> {
                Log.e(TAG, "bad content for ${test.id}: ct=${r.contentType}")
                _state.update { it.copy(pdfDownloading = false, failure = PyqPaperFailure.UNSUPPORTED) }
            }
            is PdfDownloadResult.HttpError -> {
                Log.e(TAG, "http ${r.code} for ${test.id}")
                _state.update { it.copy(pdfDownloading = false, failure = PyqPaperFailure.UNSUPPORTED) }
            }
            is PdfDownloadResult.NetworkError -> _state.update {
                it.copy(pdfDownloading = false, failure = PyqPaperFailure.NETWORK)
            }
        }
    }
}
