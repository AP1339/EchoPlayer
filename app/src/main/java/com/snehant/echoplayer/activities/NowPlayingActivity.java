package com.snehant.echoplayer.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.snehant.echoplayer.R;
import com.snehant.echoplayer.models.Song;
import com.snehant.echoplayer.player.PlaybackManager;

public class NowPlayingActivity extends AppCompatActivity {

    private TextView txtTitle, txtArtist, txtCurrentTime, txtTotalTime;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnShuffle, btnRepeat, btnRepeatOne;
    private TextView btnClose;
    private SeekBar seekBar;
    private PlaybackManager playbackManager;
    private Handler progressHandler = new Handler();
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        playbackManager = PlaybackManager.getInstance(this);

        initViews();
        setupControls();
        updateUI();
        startProgressUpdates();
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtNowPlayingTitle);
        txtArtist = findViewById(R.id.txtNowPlayingArtist);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtTotalTime = findViewById(R.id.txtTotalTime);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnRepeatOne = findViewById(R.id.btnRepeatOne);
        btnClose = findViewById(R.id.btnClose);
        seekBar = findViewById(R.id.seekBar);

        btnClose.setOnClickListener(v -> finish());
    }

    private void setupControls() {
        // Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (playbackManager.isPlaying()) {
                playbackManager.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play);
            } else {
                playbackManager.resume();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });

        // Previous
        btnPrevious.setOnClickListener(v -> {
            playbackManager.previous();
            updateUI();
        });

        // Next
        btnNext.setOnClickListener(v -> {
            playbackManager.next();
            updateUI();
        });

        // Shuffle
        btnShuffle.setOnClickListener(v -> {
            playbackManager.toggleShuffle();
            updateShuffleRepeatUI();
            Toast.makeText(this,
                    playbackManager.isShuffle() ? "🔀 Shuffle ON" : "🔀 Shuffle OFF",
                    Toast.LENGTH_SHORT).show();
        });

        // Repeat All
        btnRepeat.setOnClickListener(v -> {
            playbackManager.toggleRepeatAll();
            updateShuffleRepeatUI();
            Toast.makeText(this,
                    playbackManager.isRepeatAll() ? "🔁 Repeat All ON" : "🔁 Repeat All OFF",
                    Toast.LENGTH_SHORT).show();
        });

        // Repeat One
        btnRepeatOne.setOnClickListener(v -> {
            playbackManager.toggleRepeatOne();
            updateShuffleRepeatUI();
            Toast.makeText(this,
                    playbackManager.isRepeatOne() ? "🔂 Repeat One ON" : "🔂 Repeat One OFF",
                    Toast.LENGTH_SHORT).show();
        });

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playbackManager.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        Song currentSong = playbackManager.getCurrentSong();
        if (currentSong != null) {
            txtTitle.setText(currentSong.getTitle());
            txtArtist.setText(currentSong.getArtist());
            btnPlayPause.setImageResource(R.drawable.ic_pause);

            int duration = playbackManager.getDuration();
            if (duration > 0) {
                seekBar.setMax(duration);
                txtTotalTime.setText(formatTime(duration));
            }
        } else {
            txtTitle.setText("Nothing Playing");
            txtArtist.setText("Select a song to play");
            btnPlayPause.setImageResource(R.drawable.ic_play);
            seekBar.setProgress(0);
            txtCurrentTime.setText("0:00");
            txtTotalTime.setText("0:00");
        }
        updateShuffleRepeatUI();
    }

    private void updateShuffleRepeatUI() {
        // Shuffle
        if (playbackManager.isShuffle()) {
            btnShuffle.setColorFilter(getColor(R.color.primary));
            btnShuffle.setAlpha(1.0f);
        } else {
            btnShuffle.setColorFilter(getColor(R.color.text_secondary));
            btnShuffle.setAlpha(0.4f);
        }

        // Repeat All
        if (playbackManager.isRepeatAll()) {
            btnRepeat.setColorFilter(getColor(R.color.primary));
            btnRepeat.setAlpha(1.0f);
        } else {
            btnRepeat.setColorFilter(getColor(R.color.text_secondary));
            btnRepeat.setAlpha(0.4f);
        }

        // Repeat One
        if (playbackManager.isRepeatOne()) {
            btnRepeatOne.setColorFilter(getColor(R.color.primary));
            btnRepeatOne.setAlpha(1.0f);
        } else {
            btnRepeatOne.setColorFilter(getColor(R.color.text_secondary));
            btnRepeatOne.setAlpha(0.4f);
        }
    }

    private void startProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (playbackManager.isPlaying()) {
                    int currentPosition = playbackManager.getCurrentPosition();
                    int duration = playbackManager.getDuration();
                    if (duration > 0) {
                        seekBar.setMax(duration);
                        seekBar.setProgress(currentPosition);
                        txtCurrentTime.setText(formatTime(currentPosition));
                        txtTotalTime.setText(formatTime(duration));
                    }
                }
                progressHandler.postDelayed(this, 1000);
            }
        };
        progressHandler.post(progressRunnable);
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(progressRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        startProgressUpdates();
    }
}