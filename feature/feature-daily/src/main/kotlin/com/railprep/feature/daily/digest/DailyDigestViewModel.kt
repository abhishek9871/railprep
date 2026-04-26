package com.railprep.feature.daily.digest

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Digest
import com.railprep.domain.model.Profile
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.repository.LanguageRepository
import com.railprep.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val TAG = "RailPrepDaily"
private const val IST_ZONE_ID = "Asia/Kolkata"

/** UI state for the 10-question digest player. */
data class DailyDigestUiState(
    val loading: Boolean = true,
    val date: LocalDate? = null,
    val digest: Digest? = null,
    val currentIndex: Int = 0,
    /** questionId → selectedOptionId (null means visited but skipped). */
    val answers: Map<String, String?> = emptyMap(),
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    /** Updated profile returned by submit_digest — carries the new streak counters. */
    val profileAfterSubmit: Profile? = null,
    val confirmSubmitVisible: Boolean = false,
    val showHi: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DailyDigestViewModel @Inject constructor(
    private val digestRepository: DigestRepository,
    private val languageRepository: LanguageRepository,
    @Suppress("UNUSED_PARAMETER") savedState: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(DailyDigestUiState())
    val state: StateFlow<DailyDigestUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val today = todayInIst()
            val prefersHi = languageRepository.observeCurrent().first()?.code == "hi"
            _state.update { it.copy(loading = true, error = null, date = today, showHi = it.showHi || prefersHi) }
            when (val r = digestRepository.loadForDate(today)) {
                is DomainResult.Success -> {
                    Log.i(TAG, "digest-loaded date=$today qs=${r.value.questions.size}")
                    _state.update { it.copy(loading = false, digest = r.value) }
                }
                is DomainResult.Failure -> {
                    Log.w(TAG, "digest-load failed: ${r.error.message}")
                    _state.update { it.copy(loading = false, error = "load") }
                }
            }
        }
    }

    fun selectOption(questionId: String, optionId: String?) {
        _state.update { s ->
            val next = s.answers.toMutableMap().also { it[questionId] = optionId }
            s.copy(answers = next, error = null)
        }
    }

    fun goNext() = _state.update { s ->
        val total = s.digest?.questions?.size ?: return@update s
        s.copy(currentIndex = (s.currentIndex + 1).coerceAtMost(total - 1))
    }

    fun goPrev() = _state.update { s ->
        s.copy(currentIndex = (s.currentIndex - 1).coerceAtLeast(0))
    }

    fun goTo(index: Int) = _state.update { it.copy(currentIndex = index) }

    fun toggleBilingual() = _state.update { it.copy(showHi = !it.showHi) }

    fun requestSubmit() = _state.update { it.copy(confirmSubmitVisible = true) }
    fun cancelSubmit() = _state.update { it.copy(confirmSubmitVisible = false) }

    fun confirmSubmit() {
        val s = _state.value
        val date = s.date ?: return
        val digest = s.digest ?: return
        if (s.submitting || s.submitted) return
        _state.update { it.copy(confirmSubmitVisible = false, submitting = true, error = null) }
        viewModelScope.launch {
            // Send only the digest's question_ids (server filters anyway, but stay tidy).
            val payload = digest.questions.map { q -> q.id to s.answers[q.id] }
            when (val r = digestRepository.submit(date, payload)) {
                is DomainResult.Success -> {
                    Log.i(TAG, "digest-submit-success date=$date streak=${r.value.streakCurrent}")
                    _state.update {
                        it.copy(submitting = false, submitted = true, profileAfterSubmit = r.value)
                    }
                }
                is DomainResult.Failure -> {
                    Log.w(TAG, "digest-submit failed: ${r.error.message}")
                    _state.update { it.copy(submitting = false, error = "submit") }
                }
            }
        }
    }
}

internal fun todayInIst(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.of(IST_ZONE_ID)).date
