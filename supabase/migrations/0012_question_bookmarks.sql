CREATE TABLE question_bookmarks (
  user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  question_id uuid NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  bookmarked_at timestamptz NOT NULL DEFAULT now(),
  note text,
  PRIMARY KEY (user_id, question_id)
);

CREATE INDEX ix_qb_user_recent
ON question_bookmarks (user_id, bookmarked_at DESC);

ALTER TABLE question_bookmarks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "qb_select_own"
ON question_bookmarks
FOR SELECT
USING ((select auth.uid()) = user_id);

CREATE POLICY "qb_insert_own"
ON question_bookmarks
FOR INSERT
WITH CHECK ((select auth.uid()) = user_id);

CREATE POLICY "qb_update_own"
ON question_bookmarks
FOR UPDATE
USING ((select auth.uid()) = user_id)
WITH CHECK ((select auth.uid()) = user_id);

CREATE POLICY "qb_delete_own"
ON question_bookmarks
FOR DELETE
USING ((select auth.uid()) = user_id);
