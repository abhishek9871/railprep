package com.railprep.data.remote.supabase

import com.railprep.domain.model.Chapter
import com.railprep.domain.model.ContentType
import com.railprep.domain.model.License
import com.railprep.domain.model.Subject
import com.railprep.domain.model.Topic
import com.railprep.domain.model.TopicStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectDto(
    @SerialName("id") val id: String,
    @SerialName("slug") val slug: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_hi") val titleHi: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("display_order") val displayOrder: Int = 0,
)

@Serializable
data class ChapterDto(
    @SerialName("id") val id: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("slug") val slug: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_hi") val titleHi: String,
    @SerialName("display_order") val displayOrder: Int = 0,
)

@Serializable
data class TopicDto(
    @SerialName("id") val id: String,
    @SerialName("chapter_id") val chapterId: String,
    @SerialName("title_en") val titleEn: String,
    @SerialName("title_hi") val titleHi: String? = null,
    @SerialName("content_type") val contentType: String,
    @SerialName("external_video_id") val externalVideoId: String? = null,
    @SerialName("external_pdf_url") val externalPdfUrl: String? = null,
    @SerialName("article_url") val articleUrl: String? = null,
    @SerialName("source") val source: String,
    @SerialName("license") val license: String,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("status") val status: String = "active",
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("tags") val tags: List<String> = emptyList(),
)

@Serializable
data class BookmarkRowDto(
    @SerialName("topic_id") val topicId: String,
)

fun SubjectDto.toDomain() = Subject(
    id = id, slug = slug, titleEn = titleEn, titleHi = titleHi,
    icon = icon, displayOrder = displayOrder,
)

fun ChapterDto.toDomain() = Chapter(
    id = id, subjectId = subjectId, slug = slug,
    titleEn = titleEn, titleHi = titleHi, displayOrder = displayOrder,
)

fun TopicDto.toDomain(bookmarked: Boolean = false) = Topic(
    id = id,
    chapterId = chapterId,
    titleEn = titleEn,
    titleHi = titleHi,
    contentType = ContentType.valueOf(contentType),
    externalVideoId = externalVideoId,
    externalPdfUrl = externalPdfUrl,
    articleUrl = articleUrl,
    source = source,
    license = licenseFromWire(license),
    durationSeconds = durationSeconds,
    status = statusFromWire(status),
    displayOrder = displayOrder,
    tags = tags,
    bookmarked = bookmarked,
)

private fun licenseFromWire(s: String): License = when (s) {
    "CC-BY-SA" -> License.CC_BY_SA
    "GODL-India" -> License.GODL_INDIA
    "PUBLIC_DOMAIN" -> License.PUBLIC_DOMAIN
    "YT_STANDARD" -> License.YT_STANDARD
    "NCERT_LINKED" -> License.NCERT_LINKED
    else -> License.YT_STANDARD
}

private fun statusFromWire(s: String): TopicStatus = when (s) {
    "active" -> TopicStatus.ACTIVE
    "stale" -> TopicStatus.STALE
    "removed" -> TopicStatus.REMOVED
    else -> TopicStatus.ACTIVE
}
