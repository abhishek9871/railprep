-- Phase F production hardening:
-- - heartbeat/watchdog_runs are operational diagnostics, readable to signed-in users only.
-- - FK indexes remove pre-launch Supabase performance-advisor findings.

CREATE POLICY "heartbeat_select_auth"
ON heartbeat
FOR SELECT
USING ((select auth.uid()) IS NOT NULL);

CREATE POLICY "watchdog_runs_select_auth"
ON watchdog_runs
FOR SELECT
USING ((select auth.uid()) IS NOT NULL);

CREATE INDEX IF NOT EXISTS ix_attempts_test_id ON attempts (test_id);
CREATE INDEX IF NOT EXISTS ix_attempt_answers_question_id ON attempt_answers (question_id);
CREATE INDEX IF NOT EXISTS ix_attempt_answers_selected_option_id ON attempt_answers (selected_option_id);
CREATE INDEX IF NOT EXISTS ix_bookmarks_topic_id ON bookmarks (topic_id);
CREATE INDEX IF NOT EXISTS ix_digest_attempts_digest_date ON digest_attempts (digest_date);
