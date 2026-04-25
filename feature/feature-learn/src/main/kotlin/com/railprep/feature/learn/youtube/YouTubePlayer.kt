package com.railprep.feature.learn.youtube

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewMediaIntegrityApiStatusConfig
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

private const val TAG = "RailPrepVideo"

/**
 * YouTube playback via the official IFrame Player API inside a WebView — same recipe the
 * android-youtube-player library (MIT) uses internally, verified by decompiling core 12.1.2.
 *
 * Two settings matter and the earlier pass missed both:
 *  1. `mediaPlaybackRequiresUserGesture = false` — with the default `true`, the player can't
 *     paint its thumbnail / play-button overlay. Result was a solid black rect. The YT IFrame
 *     API still honours our `autoplay: 0` player var, so real playback still needs a tap.
 *  2. `baseUrl` must match the `origin` player var. The IFrame API's postMessage handshake
 *     compares parent origin to the `origin` param; mismatch leaves the player silent.
 *
 * YouTube-side config:
 *  - `origin: https://www.youtube.com` + `enablejsapi: 1` match Google's documented recipe.
 *  - `rel: 0` keeps related-video tiles to the same channel.
 *  - `iv_load_policy: 3` suppresses video annotations.
 *  - `autoplay: 0` enforces our no-autoplay stance.
 *  - Default IFrame controls + branding stay visible (ToS).
 */
@Composable
fun YouTubePlayer(
    videoId: String,
    @Suppress("UNUSED_PARAMETER") lifecycleOwner: LifecycleOwner,
    onError: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(videoId) {
        Log.i(TAG, "attach videoId=$videoId")
        onDispose { Log.i(TAG, "dispose videoId=$videoId") }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                setBackgroundColor(Color.BLACK)
                WebView.setWebContentsDebuggingEnabled(true)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }
                // The WebView Media Integrity API (Android 14+) lets YouTube reject embeds when
                // the device's GMS attestation doesn't match what YT expects. Some dev devices
                // (e.g. ones with Revanced's gmscore alongside real Play Services) return a bad
                // attestation and YT responds with error 152. Opt the API out for this view so
                // YT falls back to the pre-attestation embed path.
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEBVIEW_MEDIA_INTEGRITY_API_STATUS)) {
                    WebSettingsCompat.setWebViewMediaIntegrityApiStatus(
                        settings,
                        WebViewMediaIntegrityApiStatusConfig.Builder(
                            WebViewMediaIntegrityApiStatusConfig.WEBVIEW_MEDIA_INTEGRITY_API_DISABLED,
                        ).build(),
                    )
                    Log.i(TAG, "MediaIntegrity API explicitly disabled")
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        val line = "console[${msg.messageLevel()}] ${msg.message()}"
                        if (line.contains("yt:error")) {
                            Log.e(TAG, line)
                            onError?.invoke()
                        } else {
                            Log.i(TAG, line)
                        }
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Log.i(TAG, "onPageFinished $url")
                    }
                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError,
                    ) {
                        if (request.isForMainFrame) {
                            Log.e(TAG, "onReceivedError ${error.errorCode} ${error.description} url=${request.url}")
                        }
                    }
                }
                val html = """
                    <!DOCTYPE html>
                    <html>
                      <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                        <style>
                          html, body { height: 100%; width: 100%; margin: 0; padding: 0; background:#000; overflow: hidden; position: fixed; }
                        </style>
                      </head>
                      <body>
                        <div id="player"></div>
                        <script defer src="https://www.youtube.com/iframe_api"></script>
                        <script>
                          function onYouTubeIframeAPIReady() {
                            new YT.Player('player', {
                              height: '100%',
                              width: '100%',
                              videoId: '$videoId',
                              playerVars: {
                                'autoplay': 0,
                                'controls': 1,
                                'enablejsapi': 1,
                                'fs': 1,
                                'iv_load_policy': 3,
                                'origin': 'https://railprep.app',
                                'playsinline': 1,
                                'rel': 0
                              },
                              events: {
                                'onReady': function(e) { console.log('yt:onReady'); },
                                'onStateChange': function(e) { console.log('yt:state=' + e.data); },
                                'onError': function(e) { console.log('yt:error=' + e.data); }
                              }
                            });
                          }
                        </script>
                      </body>
                    </html>
                """.trimIndent()
                Log.i(TAG, "loading IFrame-API wrapper videoId=$videoId")
                loadDataWithBaseURL("https://railprep.app/", html, "text/html", "utf-8", null)
            }
        },
        onRelease = { webView ->
            Log.i(TAG, "release videoId=$videoId")
            runCatching {
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.destroy()
            }
        },
    )
}
