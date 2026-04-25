package com.railprep.feature.home.profile.diag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.core.common.diag.PdfDiag
import com.railprep.core.common.diag.PdfDiagSnapshot
import com.railprep.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val PDF_CACHE_DIR = "pdfs"

data class DiagUiState(
    val appVersion: String = "",
    val userId: String? = null,
    val pdfCacheBytes: Long = 0L,
    val lastPdf: PdfDiagSnapshot? = null,
    val cleared: Boolean = false,
)

@HiltViewModel
class DiagViewModel @Inject constructor(
    private val app: Application,
    private val authRepository: AuthRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(DiagUiState())
    val state: StateFlow<DiagUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        val version = runCatching {
            app.packageManager.getPackageInfo(app.packageName, 0).versionName ?: ""
        }.getOrDefault("")
        val userId = authRepository.currentUserSync()?.id
        viewModelScope.launch {
            val size = withContext(Dispatchers.IO) { pdfCacheSize() }
            _state.update {
                it.copy(
                    appVersion = version,
                    userId = userId,
                    pdfCacheBytes = size,
                    lastPdf = PdfDiag.last,
                )
            }
        }
    }

    fun clearPdfCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { pdfCacheDir().listFiles()?.forEach { it.delete() } }
            _state.update { it.copy(pdfCacheBytes = 0L, cleared = true) }
        }
    }

    fun clearFlag() = _state.update { it.copy(cleared = false) }

    private fun pdfCacheDir(): File = File(app.cacheDir, PDF_CACHE_DIR)
    private fun pdfCacheSize(): Long = pdfCacheDir().listFiles()?.sumOf { it.length() } ?: 0L
}
