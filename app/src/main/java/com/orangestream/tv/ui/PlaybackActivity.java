package com.orangestream.tv.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.orangestream.tv.R;
import com.orangestream.tv.model.Movie;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PlaybackActivity extends FragmentActivity {

    private PlayerView mPlayerView;
    private WebView mWebView;
    private ExoPlayer mPlayer;
    private ProgressBar mLoadingSpinner;
    private Movie mMovie;
    private String mServer = "vidlink";
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private static final String DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    // Synchronous polyfill and style injected into EVERY HTML frame (main frame and nested iframes)
    private static final String SYNCHRONOUS_INJECTION =
            "<script>" +
            "(function() {" +
            "  var dummyWin = {" +
            "    closed: false," +
            "    close: function() { this.closed = true; }," +
            "    focus: function() {}," +
            "    blur: function() {}," +
            "    postMessage: function() {}," +
            "    location: { href: 'about:blank' }" +
            "  };" +
            "  try {" +
            "    window.open = function() { return dummyWin; };" +
            "    Object.defineProperty(window, 'open', {" +
            "      get: function() { return function() { return dummyWin; }; }," +
            "      set: function() {}," +
            "      configurable: true" +
            "    });" +
            "  } catch(e) {}" +
            "  try {" +
            "    if (window.frameElement) {" +
            "      window.frameElement.removeAttribute('sandbox');" +
            "    }" +
            "  } catch(e) {}" +
            "  try {" +
            "    localStorage.removeItem('isSandboxed');" +
            "    sessionStorage.removeItem('isSandboxed');" +
            "  } catch(e) {}" +
            "})();" +
            "</script>" +
            "<style>" +
            "  #sbxErr, #sbxwo, [id*='sbx'], div[id*='sbx']," +
            "  div[style*='2147483647'], div[style*='7fffffff'] {" +
            "    display: none !important;" +
            "    visibility: hidden !important;" +
            "    opacity: 0 !important;" +
            "    pointer-events: none !important;" +
            "    width: 0 !important;" +
            "    height: 0 !important;" +
            "  }" +
            "</style>";

    private static final OkHttpClient sHttpClient = new OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_playback);

        mMovie = (Movie) getIntent().getSerializableExtra("movie");
        String serverExtra = getIntent().getStringExtra("server");
        if (serverExtra != null) mServer = serverExtra;

        mPlayerView = findViewById(R.id.playerView);
        mWebView = findViewById(R.id.playerWebView);
        mLoadingSpinner = findViewById(R.id.loadingSpinner);

        startPlayback();
    }

    private void startPlayback() {
        if (mMovie == null) {
            finish();
            return;
        }

        mLoadingSpinner.setVisibility(View.VISIBLE);

        String streamUrl = mMovie.getStreamUrl();

        // 1. If it's a Movie or TV show without direct streamUrl, generate Embed URL
        if (!mMovie.isSport() && (streamUrl == null || streamUrl.isEmpty())) {
            streamUrl = buildEmbedUrl(mMovie, mServer);
        }

        // 2. Decide Player Engine
        if (streamUrl != null && (streamUrl.contains(".m3u8") || streamUrl.contains(".mp4"))) {
            initExoPlayer(streamUrl);
        } else if (streamUrl != null && !streamUrl.isEmpty()) {
            initWebView(streamUrl);
        } else {
            Toast.makeText(this, "URL siaran tidak valid", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String buildEmbedUrl(Movie movie, String server) {
        long id = movie.getId();
        boolean isTv = "tv".equalsIgnoreCase(movie.getMediaType());

        if (isTv) {
            switch (server) {
                case "autoembed":
                    return "https://autoembed.co/tv/tmdb/" + id + "-1-1";
                case "multiembed":
                    return "https://multiembed.mov/?video_id=" + id + "&tmdb=1&s=1&e=1";
                case "twoembed":
                    return "https://www.2embed.cc/embedtv/" + id + "&s=1&e=1";
                case "vidsrc":
                    return "https://vidsrc.pm/embed/tv/" + id + "/1/1";
                case "vidlink":
                default:
                    return "https://vidlink.pro/tv/" + id + "/1/1";
            }
        } else {
            switch (server) {
                case "autoembed":
                    return "https://autoembed.co/movie/tmdb/" + id;
                case "multiembed":
                    return "https://multiembed.mov/?video_id=" + id + "&tmdb=1";
                case "twoembed":
                    return "https://www.2embed.cc/embed/" + id;
                case "vidsrc":
                    return "https://vidsrc.pm/embed/movie/" + id;
                case "vidlink":
                default:
                    return "https://vidlink.pro/movie/" + id;
            }
        }
    }

    private void initExoPlayer(String url) {
        mWebView.setVisibility(View.GONE);
        mPlayerView.setVisibility(View.VISIBLE);

        mPlayer = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();

        mPlayerView.setPlayer(mPlayer);

        Uri videoUri = Uri.parse(url);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        mPlayer.setMediaItem(mediaItem);
        mPlayer.prepare();
        mPlayer.setPlayWhenReady(true);

        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    mLoadingSpinner.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    mLoadingSpinner.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_ENDED) {
                    finish();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                mLoadingSpinner.setVisibility(View.GONE);
                Toast.makeText(PlaybackActivity.this, "Gagal memutar siaran: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initWebView(String url) {
        mPlayerView.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);

        // Reset any cached sandbox flags in local storage
        try {
            WebStorage.getInstance().deleteAllData();
        } catch (Exception ignored) {}

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setUserAgentString(DESKTOP_UA);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri reqUri = request.getUrl();
                if (reqUri == null) return super.shouldInterceptRequest(view, request);

                String urlStr = reqUri.toString();
                String host = reqUri.getHost() != null ? reqUri.getHost().toLowerCase() : "";
                String path = reqUri.getPath() != null ? reqUri.getPath().toLowerCase() : "";

                // 1. Block aggressive popups, trackers, and sandbox error redirect pages
                if (host.contains("optimserve") || host.contains("aclib") || host.contains("adnxs")
                        || host.contains("popads") || host.contains("popunder") || host.contains("histats")
                        || host.contains("gussiessmutchy") || host.contains("llvpn") || path.endsWith("/ad.html")
                        || urlStr.contains("zoneId=") || urlStr.contains("/asb.html")) {
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
                }

                // 2. Intercept HTML documents across ALL frames (main frame AND subframe iframes)
                Map<String, String> reqHeaders = request.getRequestHeaders();
                String accept = reqHeaders != null ? reqHeaders.get("Accept") : null;
                if (accept == null && reqHeaders != null) accept = reqHeaders.get("accept");

                boolean isHtml = request.isForMainFrame()
                        || (accept != null && accept.contains("text/html"))
                        || path.isEmpty() || path.equals("/") || path.endsWith(".html") || path.endsWith(".php")
                        || urlStr.contains("/embed/") || urlStr.contains("/source/") || urlStr.contains("autoembed")
                        || urlStr.contains("multiembed") || urlStr.contains("2embed") || urlStr.contains("vidsrc")
                        || urlStr.contains("vidlink");

                if (isHtml && (urlStr.startsWith("http://") || urlStr.startsWith("https://"))) {
                    try {
                        Request.Builder reqBuilder = new Request.Builder().url(urlStr);
                        if (reqHeaders != null) {
                            for (Map.Entry<String, String> entry : reqHeaders.entrySet()) {
                                String k = entry.getKey();
                                if (!k.equalsIgnoreCase("Accept-Encoding") && !k.equalsIgnoreCase("Content-Length")) {
                                    reqBuilder.addHeader(k, entry.getValue());
                                }
                            }
                        }
                        reqBuilder.header("User-Agent", DESKTOP_UA);

                        try (Response response = sHttpClient.newCall(reqBuilder.build()).execute()) {
                            if (!response.isSuccessful()) {
                                return super.shouldInterceptRequest(view, request);
                            }

                            ResponseBody body = response.body();
                            if (body == null) return super.shouldInterceptRequest(view, request);

                            String contentType = response.header("Content-Type", "text/html; charset=utf-8");
                            String mimeType = "text/html";
                            String encoding = "UTF-8";
                            if (contentType != null && contentType.contains(";")) {
                                String[] parts = contentType.split(";");
                                mimeType = parts[0].trim();
                                for (String part : parts) {
                                    if (part.toLowerCase().contains("charset=")) {
                                        encoding = part.substring(part.indexOf('=') + 1).trim();
                                    }
                                }
                            }

                            if (!mimeType.equalsIgnoreCase("text/html")) {
                                return new WebResourceResponse(mimeType, encoding, body.byteStream());
                            }

                            String html = body.string();

                            // Strip any sandbox attributes from any iframe tag in the incoming HTML
                            html = html.replaceAll("(?i)\\s+sandbox(=[\"'][^\"']*[\"'])?", "");

                            // Neutralize anti-sandbox redirect targets
                            html = html.replace("/asb.html", "#");

                            // Inject synchronous polyfill right at head start
                            int headIdx = html.toLowerCase().indexOf("<head>");
                            if (headIdx != -1) {
                                html = html.substring(0, headIdx + 6) + SYNCHRONOUS_INJECTION + html.substring(headIdx + 6);
                            } else {
                                int htmlIdx = html.toLowerCase().indexOf("<html");
                                if (htmlIdx != -1) {
                                    int tagEnd = html.indexOf('>', htmlIdx);
                                    if (tagEnd != -1) {
                                        html = html.substring(0, tagEnd + 1) + "<head>" + SYNCHRONOUS_INJECTION + "</head>" + html.substring(tagEnd + 1);
                                    } else {
                                        html = SYNCHRONOUS_INJECTION + html;
                                    }
                                } else {
                                    html = SYNCHRONOUS_INJECTION + html;
                                }
                            }

                            return new WebResourceResponse(mimeType, encoding, new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
                        }
                    } catch (Exception ignored) {
                        // Fallback to WebView's native loader
                    }
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadingSpinner.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri != null ? uri.getScheme() : null;
                // Block external non-web schemes (intent://, market://, etc.)
                if (scheme != null && !scheme.startsWith("http")) {
                    return true;
                }
                // Allow iframe navigation within player
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 70) {
                    mLoadingSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                // Return a properly configured dummy WebView so window.open never returns null and never opens intrusive popups
                WebView dummyWebView = new WebView(view.getContext());
                WebSettings dummySettings = dummyWebView.getSettings();
                dummySettings.setJavaScriptEnabled(true);
                dummySettings.setDomStorageEnabled(true);

                dummyWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                        return true; // Neutralize popup navigation
                    }
                });

                dummyWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        window.destroy();
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(dummyWebView);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomView = view;
                mCustomViewCallback = callback;
                ((FrameLayout) findViewById(android.R.id.content)).addView(view);
                mWebView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) return;
                ((FrameLayout) findViewById(android.R.id.content)).removeView(mCustomView);
                mCustomView = null;
                mCustomViewCallback.onCustomViewHidden();
                mWebView.setVisibility(View.VISIBLE);
            }
        });

        mWebView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView != null) {
                WebChromeClient client = new WebChromeClient();
                client.onHideCustomView();
                return true;
            }
            releasePlayer();
            finish();
            return true;
        }

        if (mPlayer != null) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                        mPlayerView.showController();
                    } else {
                        mPlayer.play();
                    }
                    return true;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mPlayer.seekTo(Math.max(0, mPlayer.getCurrentPosition() - 10000));
                    mPlayerView.showController();
                    return true;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mPlayer.seekTo(Math.min(mPlayer.getDuration(), mPlayer.getCurrentPosition() + 10000));
                    mPlayerView.showController();
                    return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        if (mWebView != null) {
            mWebView.loadUrl("about:blank");
            mWebView.stopLoading();
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
