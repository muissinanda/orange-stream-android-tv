package com.orangestream.tv.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
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

public class PlaybackActivity extends FragmentActivity {

    private PlayerView mPlayerView;
    private WebView mWebView;
    private ExoPlayer mPlayer;
    private ProgressBar mLoadingSpinner;
    private Movie mMovie;
    private String mServer = "autoembed";
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

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
                case "multiembed":
                    return "https://multiembed.mov/?video_id=" + id + "&tmdb=1&s=1&e=1";
                case "twoembed":
                    return "https://www.2embed.cc/embedtv/" + id + "&s=1&e=1";
                case "vidsrc":
                    return "https://vidsrc.pm/embed/tv/" + id + "/1/1";
                case "autoembed":
                default:
                    return "https://autoembed.co/tv/tmdb/" + id + "-1-1";
            }
        } else {
            switch (server) {
                case "multiembed":
                    return "https://multiembed.mov/?video_id=" + id + "&tmdb=1";
                case "twoembed":
                    return "https://www.2embed.cc/embed/" + id;
                case "vidsrc":
                    return "https://vidsrc.pm/embed/movie/" + id;
                case "autoembed":
                default:
                    return "https://autoembed.co/movie/tmdb/" + id;
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

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String reqUrl = request.getUrl().toString();
                // Allow player host navigations, block random ad redirects
                if (reqUrl.contains("autoembed") || reqUrl.contains("multiembed") || 
                    reqUrl.contains("2embed") || reqUrl.contains("vidsrc") || 
                    reqUrl.contains("embed.st") || reqUrl.contains("boyblade.my.id") ||
                    reqUrl.contains("workers.dev")) {
                    return false;
                }
                return true; // Block ad popups
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadingSpinner.setVisibility(View.GONE);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 80) {
                    mLoadingSpinner.setVisibility(View.GONE);
                }
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
