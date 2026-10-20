package com.orangestream.tv.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
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
    private ExoPlayer mPlayer;
    private ProgressBar mLoadingSpinner;
    private Movie mMovie;
    private String mServer = "autoembed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_playback);

        mMovie = (Movie) getIntent().getSerializableExtra("movie");
        String serverExtra = getIntent().getStringExtra("server");
        if (serverExtra != null) mServer = serverExtra;

        mPlayerView = findViewById(R.id.playerView);
        mLoadingSpinner = findViewById(R.id.loadingSpinner);

        initPlayer();
    }

    private void initPlayer() {
        if (mMovie == null) {
            finish();
            return;
        }

        mLoadingSpinner.setVisibility(View.VISIBLE);

        mPlayer = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();

        mPlayerView.setPlayer(mPlayer);

        String streamUrl = mMovie.getStreamUrl();
        if (streamUrl == null || streamUrl.isEmpty()) {
            // Default demo or direct stream
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
        }

        Uri videoUri = Uri.parse(streamUrl);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPlayer == null) return super.onKeyDown(keyCode, event);

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

            case KeyEvent.KEYCODE_BACK:
                releasePlayer();
                finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
