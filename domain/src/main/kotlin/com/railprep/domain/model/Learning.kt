package com.railprep.domain.model

/** Top-level subject catalog entry (Mathematics, Reasoning, etc.). */
data class Subject(
    val id: String,
    val slug: String,
    val titleEn: String,
    val titleHi: String,
    val icon: String?,
    val displayOrder: Int,
)

data class Chapter(
    val id: String,
    val subjectId: String,
    val slug: String,
    val titleEn: String,
    val titleHi: String,
    val displayOrder: Int,
)

enum class ContentType { YT_VIDEO, PDF_URL, ARTICLE, QUIZ }

enum class License { CC_BY_SA, GODL_INDIA, PUBLIC_DOMAIN, YT_STANDARD, NCERT_LINKED }

enum class TopicStatus { ACTIVE, STALE, REMOVED }

/**
 * A pointer to external content. We never host media — `externalVideoId` is a YouTube video ID,
 * `externalPdfUrl` is a direct HTTPS URL (typically ncert.nic.in). Exactly one URL field is
 * populated per [contentType] (enforced by a DB CHECK constraint).
 */
data class Topic(
    val id: String,
    val chapterId: String,
    val titleEn: String,
    val titleHi: String?,
    val contentType: ContentType,
    val externalVideoId: String?,
    val externalPdfUrl: String?,
    val articleUrl: String?,
    val source: String,
    val license: License,
    val durationSeconds: Int?,
    val status: TopicStatus,
    val displayOrder: Int,
    /** Tags drive weak-topic routing — see AttemptRepository.weakTopicSuggestions. */
    val tags: List<String> = emptyList(),
    val bookmarked: Boolean = false,
)

/**
 * One row in the post-submit "study this next" recommendation list.
 * The user got [missCount] questions wrong on this tag; [topic] is the matching primer.
 */
data class WeakTopicRecommendation(
    val tag: String,
    val missCount: Int,
    val topic: Topic,
)

data class Bookmark(
    val userId: String,
    val topicId: String,
)
