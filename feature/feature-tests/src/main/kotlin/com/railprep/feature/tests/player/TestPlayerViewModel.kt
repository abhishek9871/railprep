package com.railprep.feature.tests.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railprep.domain.model.Attempt
import com.railprep.domain.model.AttemptStatus
import com.railprep.domain.repository.AttemptRepository
import com.railprep.domain.repository.TestsRepository
import com.railprep.domain.util.DomainResult
import com.railprep.feature.tests.diag.AttemptDiag
import com.railprep.feature.tests.offline.AttemptLocalDao
import com.railprep.feature.tests.offline.LocalAttemptAnswerEntity
import com.railprep.feature.tests.offline.LocalAttemptMetaEntity
import com.railprep.feature.tests.work.AutoSubmitScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RailPrepTests"
private const val UPSERT_DEBOUNCE_MS = 500L
private const val TICK_MS = 1000L
// Tick log throttle — one ln every 5s keeps logcat readable without losing drift signal.
private const val TICK_LOG_EVERY_MS = 5000L
private const val KEY_INDEX = "currentIndex"
private const val KEY_HI = "showHi"

@HiltViewModel
class TestPlayerViewModel @Inject constructor(
    private val testsRepository: TestsRepository,
    private val attemptRepository: AttemptRepository,
    private val localDao: AttemptLocalDao,
    private val autoSubmitScheduler: AutoSubmitScheduler,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(TestPlayerState())
    val state: StateFlow<TestPlayerState> = _state.asStateFlow()

    private val upsertJobs = mutableMapOf<String, Job>()
    private var tickerJob: Job? = null

    fun load(attemptId: String) {
        if (_state.value.attemptId == attemptId && !_state.value.loading) return

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, attemptId = attemptId) }

            // 1) Server attempt row (canonical test_id + deadline + status).
            val attemptRes = attemptRepository.get(attemptId)
            val attempt = (attemptRes as? DomainResult.Success)?.value
            if (attempt == null) {
                _state.update { it.copy(loading = false, error = "load-attempt") }
                return@launch
            }

            if (attempt.status == AttemptStatus.SUBMITTED) {
                // Already submitted — player shouldn't be here; surface to UI so it can pop.
                _state.update { it.copy(loading = false, attempt = attempt, submitted = true) }
                return@launch
            }

            // 2) Test + sections + questions (all via RLS-filtered selects).
            val testRes = testsRepository.get(attempt.testId)
            val sectionsRes = testsRepository.listSections(attempt.testId)
            val questionsRes = testsRepository.listQuestions(attempt.testId)
            if (testRes !is DomainResult.Success || sectionsRes !is DomainResult.Success || questionsRes !is DomainResult.Success) {
                _state.update { it.copy(loading = false, error = "load-content") }
                return@launch
            }
            val test = testRes.value
            val sections = sectionsRes.value.sortedBy { it.displayOrder }
            val orderedSectionIds = sections.map { it.id }
            val questions = questionsRes.value.sortedWith(
                compareBy({ orderedSectionIds.indexOf(it.sectionId) }, { it.displayOrder }),
            )

            // 3) Server answers + local answers → merge.
            val serverAnswers = (attemptRepository.listAnswers(attemptId) as? DomainResult.Success)?.value.orEmpty()
            val localAnswers = localDao.listAnswers(attemptId)

            val merged = mutableMapOf<String, PlayerAnswer>()
            for (sa in serverAnswers) {
                merged[sa.questionId] = PlayerAnswer(sa.selectedOptionId, sa.flagged, synced = true)
            }
            // Un-synced local rows override — they represent writes the client made after
            // the last successful sync. Conflict policy: last-write-by-answered_at.
            for (la in localAnswers) {
                val existing = merged[la.questionId]
                val localNewer = la.answeredAtEpochMs > 0L &&
                    (existing == null || !la.synced)
                if (localNewer) {
                    merged[la.questionId] = PlayerAnswer(la.selectedOptionId, la.flagged, synced = la.synced)
                }
            }

            // 4) Local meta — restore index + bilingual. SavedStateHandle takes precedence
            //    (covers rotation without touching disk).
            val meta = localDao.getMeta(attemptId)
            if (meta == null) {
                localDao.upsertMeta(
                    LocalAttemptMetaEntity(
                        attemptId = attemptId,
                        testId = attempt.testId,
                        deadlineEpochMs = attempt.serverDeadlineAt.toEpochMilliseconds(),
                    ),
                )
            }
            val savedIdx: Int? = savedState.get<Int>(KEY_INDEX)
            val savedHi: Boolean? = savedState.get<Boolean>(KEY_HI)
            val initialIdx = savedIdx ?: meta?.lastQuestionIndex ?: 0
            val initialHi = savedHi ?: meta?.bilingualIsHi ?: false

            val pendingUnsynced = localDao.countUnsynced(attemptId)

            _state.update {
                it.copy(
                    loading = false,
                    test = test,
                    sections = sections,
                    questions = questions,
                    answers = merged.toMap(),
                    attempt = attempt,
                    attemptId = attemptId,
                    deadlineEpochMs = attempt.serverDeadlineAt.toEpochMilliseconds(),
                    remainingMs = attempt.serverDeadlineAt.toEpochMilliseconds() - System.currentTimeMillis(),
                    currentIndex = initialIdx.coerceIn(0, (questions.size - 1).coerceAtLeast(0)),
                    showHi = initialHi,
                    pendingUnsynced = pendingUnsynced,
                )
            }
            Log.i(
                TAG,
                "attempt-started aid=${attemptId.takeLast(8)} test=${test.slug} " +
                    "questions=${questions.size} deadline=${attempt.serverDeadlineAt} " +
                    "resumed-answers=${merged.size}",
            )
            // Belt-and-braces: if the app was cold-started with an orphan
            // IN_PROGRESS attempt (schedule() was lost when the OS killed the
            // process before submit), re-enqueue. KEEP policy — no-op if
            // already scheduled.
            autoSubmitScheduler.ensureScheduled(
                attemptId = attempt.id,
                deadlineEpochMs = attempt.serverDeadlineAt.toEpochMilliseconds(),
            )
            startTicker()
            // Best-effort flush of any un-synced local answers from a previous network drop.
            viewModelScope.launch { flushUnsynced() }
        }
    }

    /** Re-fetches the attempt row so the deadline is re-derived if the user changed
     *  device time while backgrounded. Call from the screen's onStart / lifecycle. */
    fun refreshDeadline() {
        val aid = _state.value.attemptId.ifEmpty { return }
        viewModelScope.launch {
            val r = attemptRepository.get(aid)
            if (r is DomainResult.Success) {
                val deadline = r.value.serverDeadlineAt.toEpochMilliseconds()
                _state.update {
                    it.copy(
                        attempt = r.value,
                        deadlineEpochMs = deadline,
                        remainingMs = deadline - System.currentTimeMillis(),
                    )
                }
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var nextLog = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = _state.value.deadlineEpochMs - now
                if (remaining <= 0) {
                    Log.i(TAG, "timer-expired aid=${_state.value.attemptId.takeLast(8)} → auto-submit")
                    _state.update { it.copy(remainingMs = 0, timeUp = true) }
                    recordDiag()
                    submit()
                    return@launch
                }
                _state.update { it.copy(remainingMs = remaining) }
                recordDiag()
                if (now >= nextLog) {
                    Log.d(
                        TAG,
                        "timer-tick aid=${_state.value.attemptId.takeLast(8)} " +
                            "remaining=${remaining / 1000}s pending=${_state.value.pendingUnsynced}",
                    )
                    nextLog = now + TICK_LOG_EVERY_MS
                }
                delay(TICK_MS)
            }
        }
    }

    private fun recordDiag() {
        val s = _state.value
        AttemptDiag.record(
            AttemptDiag.Snapshot(
                attemptId = s.attemptId.takeIf { it.isNotEmpty() },
                deadlineEpochMs = s.deadlineEpochMs.takeIf { it > 0 },
                localNowMs = System.currentTimeMillis(),
                lastServerSyncMs = 0L, // updated on successful upsert
                pendingLocalWrites = s.pendingUnsynced,
            ),
        )
    }

    fun selectOption(questionId: String, optionId: String?) {
        val aid = _state.value.attemptId.ifEmpty { return }
        val flagged = _state.value.answers[questionId]?.flagged ?: false
        _state.update {
            it.copy(
                answers = it.answers + (questionId to PlayerAnswer(optionId, flagged, synced = false)),
            )
        }
        schedulePersist(aid, questionId, optionId, flagged)
    }

    fun toggleFlag(questionId: String) {
        val aid = _state.value.attemptId.ifEmpty { return }
        val existing = _state.value.answers[questionId]
        val newFlag = !(existing?.flagged ?: false)
        _state.update {
            it.copy(
                answers = it.answers + (questionId to PlayerAnswer(existing?.selectedOptionId, newFlag, synced = false)),
            )
        }
        schedulePersist(aid, questionId, existing?.selectedOptionId, newFlag)
    }

    fun clearSelection(questionId: String) = selectOption(questionId, null)

    private fun schedulePersist(
        attemptId: String,
        questionId: String,
        selectedOptionId: String?,
        flagged: Boolean,
    ) {
        // 1) Write to Room immediately (survives process death).
        viewModelScope.launch {
            localDao.upsertAnswer(
                LocalAttemptAnswerEntity(
                    attemptId = attemptId,
                    questionId = questionId,
                    selectedOptionId = selectedOptionId,
                    flagged = flagged,
                    answeredAtEpochMs = System.currentTimeMillis(),
                    synced = false,
                ),
            )
            _state.update { it.copy(pendingUnsynced = localDao.countUnsynced(attemptId)) }
        }
        // 2) Debounced server upsert — 500ms after the user stops changing.
        upsertJobs[questionId]?.cancel()
        upsertJobs[questionId] = viewModelScope.launch {
            delay(UPSERT_DEBOUNCE_MS)
            val r = attemptRepository.upsertAnswer(attemptId, questionId, selectedOptionId, flagged)
            if (r is DomainResult.Success) {
                localDao.markSynced(attemptId, questionId)
                _state.update {
                    it.copy(
                        answers = it.answers + (
                            questionId to (it.answers[questionId]?.copy(synced = true)
                                ?: PlayerAnswer(selectedOptionId, flagged, synced = true))
                            ),
                        pendingUnsynced = localDao.countUnsynced(attemptId),
                    )
                }
                localDao.setLastSync(attemptId, System.currentTimeMillis())
                Log.i(
                    TAG,
                    "answer-upsert aid=${attemptId.takeLast(8)} q=${questionId.takeLast(6)} " +
                        "opt=${selectedOptionId?.takeLast(6) ?: "skip"} flagged=$flagged sync=ok",
                )
            } else {
                Log.w(
                    TAG,
                    "answer-upsert-failed aid=${attemptId.takeLast(8)} q=${questionId.takeLast(6)} " +
                        "— will retry on next flush",
                )
                _state.update { it.copy(pendingUnsynced = localDao.countUnsynced(attemptId)) }
            }
        }
    }

    private suspend fun flushUnsynced() {
        val aid = _state.value.attemptId.ifEmpty { return }
        val rows = localDao.listAnswers(aid).filter { !it.synced }
        for (row in rows) {
            val r = attemptRepository.upsertAnswer(aid, row.questionId, row.selectedOptionId, row.flagged)
            if (r is DomainResult.Success) {
                localDao.markSynced(aid, row.questionId)
            }
        }
        _state.update { it.copy(pendingUnsynced = localDao.countUnsynced(aid)) }
    }

    fun goNext() {
        val max = (_state.value.questions.size - 1).coerceAtLeast(0)
        val next = (_state.value.currentIndex + 1).coerceAtMost(max)
        setIndex(next)
    }

    fun goPrev() {
        val prev = (_state.value.currentIndex - 1).coerceAtLeast(0)
        setIndex(prev)
    }

    fun jumpTo(idx: Int) {
        val max = (_state.value.questions.size - 1).coerceAtLeast(0)
        setIndex(idx.coerceIn(0, max))
        closePalette()
    }

    private fun setIndex(idx: Int) {
        _state.update { it.copy(currentIndex = idx) }
        savedState[KEY_INDEX] = idx
        val aid = _state.value.attemptId.ifEmpty { return }
        viewModelScope.launch { localDao.setIndex(aid, idx) }
    }

    fun toggleBilingual() {
        val newVal = !_state.value.showHi
        _state.update { it.copy(showHi = newVal) }
        savedState[KEY_HI] = newVal
        val aid = _state.value.attemptId.ifEmpty { return }
        viewModelScope.launch { localDao.setBilingual(aid, newVal) }
    }

    fun openPalette() = _state.update { it.copy(paletteOpen = true) }
    fun closePalette() = _state.update { it.copy(paletteOpen = false) }

    fun requestSubmit() = _state.update { it.copy(confirmSubmit = true) }
    fun cancelSubmit() = _state.update { it.copy(confirmSubmit = false) }

    fun submit() {
        if (_state.value.submitting || _state.value.submitted) return
        _state.update { it.copy(submitting = true, confirmSubmit = false, error = null) }
        viewModelScope.launch {
            // Flush any pending answers first — don't lose un-synced picks.
            flushUnsynced()
            // Cancel in-flight debounced upserts; their results no longer matter.
            upsertJobs.values.forEach { it.cancel() }
            upsertJobs.clear()

            when (val r = attemptRepository.submit(_state.value.attemptId)) {
                is DomainResult.Success -> {
                    tickerJob?.cancel()
                    val aid = _state.value.attemptId
                    localDao.clearAnswers(aid)
                    localDao.clearMeta(aid)
                    // Cancel the process-death auto-submit worker — best-effort,
                    // harmless if it fires late because the RPC will reject
                    // non-IN_PROGRESS status.
                    autoSubmitScheduler.cancel(aid)
                    _state.update { it.copy(submitting = false, submitted = true, attempt = r.value) }
                    Log.i(
                        TAG,
                        "submit-success aid=${aid.takeLast(8)} status=${r.value.status} " +
                            "correct=${r.value.correctCount} wrong=${r.value.wrongCount} " +
                            "skipped=${r.value.skippedCount} score=${r.value.score} " +
                            "max=${r.value.maxScore}",
                    )
                }
                is DomainResult.Failure -> {
                    Log.e(TAG, "submit-failed: ${r.error.message}")
                    _state.update { it.copy(submitting = false, error = "submit") }
                }
            }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        upsertJobs.values.forEach { it.cancel() }
        upsertJobs.clear()
        super.onCleared()
    }
}
