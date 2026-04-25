package com.railprep.feature.learn.pdf

import android.content.Context
import android.util.Log
import com.railprep.core.common.diag.PdfDiag
import com.railprep.core.common.diag.PdfDiagSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.TlsVersion
import java.io.File
import java.util.concurrent.TimeUnit

private const val TAG = "RailPrepPdf"
private const val CACHE_DIR = "pdfs"
private const val MAX_CACHE_BYTES = 100L * 1024L * 1024L
private const val MIN_PDF_BYTES = 50_000L
private const val UA =
    "Mozilla/5.0 (Linux; Android 16; CPH2491) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/147.0.0.0 Mobile Safari/537.36 RailPrep/2.5"

sealed class PdfDownloadResult {
    data class Success(val file: File) : PdfDownloadResult()
    data class BadContent(val contentType: String?, val firstBytesHex: String) : PdfDownloadResult()
    data class HttpError(val code: Int) : PdfDownloadResult()
    data class NetworkError(val message: String) : PdfDownloadResult()
}

class PdfCache(private val context: Context) {

    private val http by lazy {
        // NCERT's Apache terminates connections when the TLS handshake advertises clients that
        // look too new. Restricting to a broad modern cipher suite with TLS 1.2 + 1.3 plus
        // retrying on connection failure handles the flaky edge.
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .build()
        OkHttpClient.Builder()
            .connectionSpecs(listOf(spec, ConnectionSpec.COMPATIBLE_TLS))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(75, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val dir: File
        get() = File(context.cacheDir, CACHE_DIR).also { it.mkdirs() }

    fun fileFor(topicId: String): File = File(dir, "$topicId.pdf")

    suspend fun downloadIfMissing(topicId: String, url: String): PdfDownloadResult =
        withContext(Dispatchers.IO) {
            val target = fileFor(topicId)
            if (target.exists() && target.length() >= MIN_PDF_BYTES && isPdfMagic(target)) {
                target.setLastModified(System.currentTimeMillis())
                return@withContext PdfDownloadResult.Success(target)
            }
            if (target.exists()) target.delete()

            val tmp = File(dir, "$topicId.pdf.part")
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", UA)
                .header("Accept", "application/pdf,*/*;q=0.8")
                .header("Accept-Language", "en-IN,en;q=0.9,hi;q=0.8")
                .header("Referer", "https://ncert.nic.in/textbook.php")
                .header("Connection", "keep-alive")
                .build()
            var result: PdfDownloadResult? = null
            var attempt = 0
            while (attempt < 3 && (result == null || result is PdfDownloadResult.NetworkError)) {
                if (attempt > 0) Thread.sleep(500L * attempt)
                attempt++
                result = runCatching {
                    http.newCall(req).execute().use { resp ->
                    val code = resp.code
                    val ct = resp.header("Content-Type")
                    if (!resp.isSuccessful) {
                        recordDiag(url, code, 0, ct, "", "HTTP_ERROR", "status $code")
                        return@runCatching PdfDownloadResult.HttpError(code)
                    }
                    val body = resp.body ?: run {
                        recordDiag(url, code, 0, ct, "", "EMPTY_BODY", "null body")
                        return@runCatching PdfDownloadResult.NetworkError("empty body")
                    }
                    tmp.outputStream().use { out -> body.byteStream().copyTo(out) }
                    val bytes = tmp.length()
                    val firstHex = firstBytesHex(tmp, 8)
                    val isPdf = bytes >= MIN_PDF_BYTES && isPdfMagic(tmp) &&
                        (ct?.contains("pdf", ignoreCase = true) == true || ct == null)
                    if (!isPdf) {
                        recordDiag(url, code, bytes, ct, firstHex, "BAD_CONTENT",
                            "ct=$ct firstBytes=$firstHex size=$bytes")
                        tmp.delete()
                        return@runCatching PdfDownloadResult.BadContent(ct, firstHex)
                    }
                    if (!tmp.renameTo(target)) {
                        tmp.copyTo(target, overwrite = true)
                        tmp.delete()
                    }
                    enforceLruLimit()
                    recordDiag(url, code, bytes, ct, firstHex, "OK", null)
                    PdfDownloadResult.Success(target)
                }
                }.getOrElse { t ->
                    Log.e(TAG, "download $url attempt=$attempt failed: ${t.message}")
                    tmp.delete()
                    recordDiag(url, 0, 0, null, "", "EXCEPTION", t.message)
                    PdfDownloadResult.NetworkError(t.message ?: "unknown")
                }
            }
            result ?: PdfDownloadResult.NetworkError("no attempts")
        }

    fun totalSizeBytes(): Long = dir.listFiles()?.sumOf { it.length() } ?: 0L

    fun clearAll() { dir.listFiles()?.forEach { it.delete() } }

    private fun enforceLruLimit() {
        val files = dir.listFiles()?.filter { it.extension == "pdf" }
            ?.sortedBy { it.lastModified() }?.toMutableList() ?: return
        var total = files.sumOf { it.length() }
        while (total > MAX_CACHE_BYTES && files.isNotEmpty()) {
            val oldest = files.removeAt(0)
            val size = oldest.length()
            if (oldest.delete()) total -= size else break
        }
    }

    private fun isPdfMagic(file: File): Boolean = runCatching {
        file.inputStream().use { input ->
            val header = ByteArray(4)
            val read = input.read(header)
            read == 4 && header[0] == 0x25.toByte() && header[1] == 0x50.toByte() &&
                header[2] == 0x44.toByte() && header[3] == 0x46.toByte()
        }
    }.getOrDefault(false)

    private fun firstBytesHex(file: File, n: Int): String = runCatching {
        file.inputStream().use { input ->
            val buf = ByteArray(n)
            val read = input.read(buf)
            if (read <= 0) "" else buf.copyOf(read).joinToString("") { "%02X".format(it) }
        }
    }.getOrDefault("")

    private fun recordDiag(
        url: String,
        httpCode: Int,
        bytes: Long,
        contentType: String?,
        firstHex: String,
        outcome: String,
        error: String?,
    ) {
        val snap = PdfDiagSnapshot(
            url = url,
            httpCode = httpCode,
            bytes = bytes,
            contentType = contentType,
            magicHex = firstHex,
            outcome = outcome,
            error = error,
            timestampMs = System.currentTimeMillis(),
        )
        PdfDiag.record(snap)
        if (outcome == "OK") {
            Log.i(TAG, "ok $url bytes=$bytes ct=$contentType")
        } else {
            Log.e(TAG, "fail $outcome $url http=$httpCode ct=$contentType bytes=$bytes first=$firstHex err=$error")
        }
    }
}
