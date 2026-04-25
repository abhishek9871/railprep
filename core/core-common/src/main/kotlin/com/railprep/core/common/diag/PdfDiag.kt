package com.railprep.core.common.diag

data class PdfDiagSnapshot(
    val url: String,
    val httpCode: Int,
    val bytes: Long,
    val contentType: String?,
    val magicHex: String,
    val outcome: String,
    val error: String?,
    val timestampMs: Long,
)

object PdfDiag {
    @Volatile
    var last: PdfDiagSnapshot? = null
        private set

    fun record(snapshot: PdfDiagSnapshot) { last = snapshot }
}
