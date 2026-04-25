package com.railprep.feature.learn.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "RailPrepPdf"

/**
 * Lazy PDF viewer — renders each page on demand as it scrolls into view. This avoids the O(pageCount)
 * bitmap allocation that truncated long NCERT textbooks mid-render.
 *
 * The PdfRenderer is opened once per file, shared across page composables, and closed on leave-composition.
 * A tiny LRU map holds recently-rendered bitmaps so quick re-scrolls don't re-decode.
 */
@Composable
fun PdfViewer(file: File, modifier: Modifier = Modifier) {
    var pageCount by remember(file) { mutableStateOf(0) }
    var renderer by remember(file) { mutableStateOf<PdfRenderer?>(null) }
    var pfd by remember(file) { mutableStateOf<ParcelFileDescriptor?>(null) }
    var error by remember(file) { mutableStateOf<String?>(null) }
    val cache = remember(file) { PageBitmapCache(max = 8) }

    LaunchedEffect(file) {
        runCatching {
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val r = PdfRenderer(descriptor)
            pfd = descriptor
            renderer = r
            pageCount = r.pageCount
            Log.i(TAG, "PdfViewer opened ${file.name} pages=${r.pageCount}")
        }.onFailure {
            error = it.message ?: "Could not open PDF"
            Log.e(TAG, "PdfViewer open failed: ${file.absolutePath}", it)
        }
    }

    DisposableEffect(file) {
        onDispose {
            cache.clear()
            runCatching { renderer?.close() }
            runCatching { pfd?.close() }
            renderer = null
            pfd = null
        }
    }

    if (error != null) {
        Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(error!!, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val currentRenderer = renderer
    if (currentRenderer == null || pageCount == 0) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
    ) {
        items(count = pageCount, key = { it }) { idx ->
            PdfPage(
                index = idx,
                renderer = currentRenderer,
                cache = cache,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun PdfPage(
    index: Int,
    renderer: PdfRenderer,
    cache: PageBitmapCache,
    modifier: Modifier = Modifier,
) {
    var bmp by remember(index, renderer) { mutableStateOf(cache[index]) }
    // Read a stable aspect from the renderer without holding the page open.
    val aspect = remember(index, renderer) {
        runCatching {
            renderer.openPage(index).use { p -> p.height.toFloat() / p.width.toFloat() }
        }.getOrDefault(1.414f) // A4 portrait fallback
    }

    LaunchedEffect(index, renderer) {
        if (bmp == null) {
            val rendered = withContext(Dispatchers.IO) {
                runCatching {
                    synchronized(renderer) {
                        renderer.openPage(index).use { page ->
                            // Target ~1080px width (good on most phones) without multiplying heap.
                            val targetW = 1080
                            val scale = targetW.toFloat() / page.width.toFloat()
                            val w = (page.width * scale).toInt().coerceAtLeast(1)
                            val h = (page.height * scale).toInt().coerceAtLeast(1)
                            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                            page.render(out, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            out
                        }
                    }
                }.onFailure { Log.e(TAG, "render page $index failed", it) }.getOrNull()
            }
            if (rendered != null) {
                cache[index] = rendered
                bmp = rendered
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / aspect)
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center,
    ) {
        val current = bmp
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CircularProgressIndicator()
        }
    }
}

private class PageBitmapCache(private val max: Int) {
    private val map = object : LinkedHashMap<Int, Bitmap>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Bitmap>): Boolean {
            return if (size > max) {
                eldest.value.recycle()
                true
            } else false
        }
    }

    @Synchronized operator fun get(key: Int): Bitmap? = map[key]?.takeUnless { it.isRecycled }

    @Synchronized operator fun set(key: Int, value: Bitmap) { map[key] = value }

    @Synchronized fun clear() {
        map.values.forEach { runCatching { if (!it.isRecycled) it.recycle() } }
        map.clear()
    }
}
