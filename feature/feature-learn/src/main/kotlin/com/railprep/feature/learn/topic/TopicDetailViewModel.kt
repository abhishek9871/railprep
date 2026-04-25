package com.railprep.feature.learn.topic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Topic
import com.railprep.domain.repository.BookmarkRepository
import com.railprep.domain.repository.LearnRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.learn.pdf.PdfCache
import com.railprep.feature.learn.pdf.PdfDownloadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TopicDetail"

enum class PdfFailure { UNSUPPORTED, NETWORK }

data class TopicDetailUiState(
    val loading: Boolean = true,
    val topic: Topic? = null,
    val bookmarked: Boolean = false,
    val pdfDownloading: Boolean = false,
    val pdfFile: File? = null,
    val pdfFailure: PdfFailure? = null,
    val error: String? = null,
)

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    private val app: Application,
    private val learnRepository: LearnRepository,
    private val bookmarkRepository: BookmarkRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(TopicDetailUiState())
    val state: StateFlow<TopicDetailUiState> = _state.asStateFlow()

    private val pdfCache = PdfCache(app)

    fun load(topicId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, pdfFailure = null, pdfFile = null) }
            when (val r = learnRepository.getTopic(topicId)) {
                is DomainResult.Success -> {
                    val topic = r.value
                    _state.update { it.copy(loading = false, topic = topic) }
                    refreshBookmarkState(topicId)
                    if (topic.externalPdfUrl != null) ensurePdfDownloaded(topic)
                }
                is DomainResult.Failure -> _state.update {
                    it.copy(loading = false, error = "Couldn't load topic.")
                }
            }
        }
    }

    fun retryPdf() {
        val topic = _state.value.topic ?: return
        if (topic.externalPdfUrl == null) return
        viewModelScope.launch { ensurePdfDownloaded(topic) }
    }

    private fun refreshBookmarkState(topicId: String) {
        viewModelScope.launch {
            when (val r = bookmarkRepository.isBookmarked(topicId)) {
                is DomainResult.Success -> _state.update { it.copy(bookmarked = r.value) }
                is DomainResult.Failure -> { /* silent */ }
            }
        }
    }

    fun toggleBookmark() {
        val topic = _state.value.topic ?: return
        val currentlyBookmarked = _state.value.bookmarked
        viewModelScope.launch {
            val r = if (currentlyBookmarked)
                bookmarkRepository.removeBookmark(topic.id)
            else
                bookmarkRepository.addBookmark(topic.id)
            when (r) {
                is DomainResult.Success -> _state.update { it.copy(bookmarked = !currentlyBookmarked) }
                is DomainResult.Failure -> _state.update {
                    it.copy(error = "Couldn't update bookmark.")
                }
            }
        }
    }

    private suspend fun ensurePdfDownloaded(topic: Topic) {
        val url = topic.externalPdfUrl ?: return
        _state.update { it.copy(pdfDownloading = true, pdfFailure = null, pdfFile = null) }
        when (val result = pdfCache.downloadIfMissing(topic.id, url)) {
            is PdfDownloadResult.Success -> _state.update {
                it.copy(pdfDownloading = false, pdfFile = result.file, pdfFailure = null)
            }
            is PdfDownloadResult.BadContent -> {
                Log.e(TAG, "bad pdf content for ${topic.id}: ct=${result.contentType}")
                _state.update { it.copy(pdfDownloading = false, pdfFailure = PdfFailure.UNSUPPORTED) }
            }
            is PdfDownloadResult.HttpError -> {
                Log.e(TAG, "http ${result.code} for ${topic.id}")
                _state.update { it.copy(pdfDownloading = false, pdfFailure = PdfFailure.UNSUPPORTED) }
            }
            is PdfDownloadResult.NetworkError -> _state.update {
                it.copy(pdfDownloading = false, pdfFailure = PdfFailure.NETWORK)
            }
        }
    }

    fun reportPlayerError() {
        val topic = _state.value.topic ?: return
        viewModelScope.launch {
            runCatching { learnRepository.reportStale(topic.id) }
                .onFailure { Log.w(TAG, "reportStale failed: ${it.message}") }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
