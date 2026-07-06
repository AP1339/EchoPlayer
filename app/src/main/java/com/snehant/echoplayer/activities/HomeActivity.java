package com.snehant.echoplayer.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.snehant.echoplayer.R;
import com.snehant.echoplayer.adapters.SongAdapter;
import com.snehant.echoplayer.interfaces.OnSongClickListener;
import com.snehant.echoplayer.models.Song;
import com.snehant.echoplayer.player.PlaybackManager;
import com.snehant.echoplayer.utils.PermissionHelper;
import com.snehant.echoplayer.viewmodel.HomeViewModel;

import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements OnSongClickListener {

    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerSongs;
    private SongAdapter adapter;
    private HomeViewModel homeViewModel;
    private TextView txtSongCount;
    private TextView txtMiniTitle;
    private TextView txtMiniArtist;
    private ImageButton btnMiniPlay;
    private ImageButton btnMiniPrevious;
    private ImageButton btnMiniNext;
    private PlaybackManager playbackManager;
    private List<Song> allSongs;

    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        playbackManager = PlaybackManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupViewModel();
        checkPermissionAndLoadSongs();
        setupMiniPlayerControls();
        setupPlaybackListener();

        // Update UI with current state
        updateMiniPlayer(playbackManager.getCurrentSong());
    }

    private void initViews() {
        try {
            recyclerSongs = findViewById(R.id.recyclerSongs);
            txtSongCount = findViewById(R.id.txtSongCount);
            txtMiniTitle = findViewById(R.id.txtMiniTitle);
            txtMiniArtist = findViewById(R.id.txtMiniArtist);
            btnMiniPlay = findViewById(R.id.btnMiniPlay);
            btnMiniPrevious = findViewById(R.id.btnMiniPrevious);
            btnMiniNext = findViewById(R.id.btnMiniNext);
            emptyState = findViewById(R.id.layoutEmptyState);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void setupPlaybackListener() {
        playbackManager.setPlaybackListener(new PlaybackManager.PlaybackListener() {
            @Override
            public void onSongChanged(Song song) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onSongChanged callback received: " + (song != null ? song.getTitle() : "null"));
                    updateMiniPlayer(song);
                });
            }
        });
    }

    private void setupMiniPlayerControls() {
        // Play/Pause button
        btnMiniPlay.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Play/Pause button clicked");
                if (playbackManager.isPlaying()) {
                    playbackManager.pause();
                    btnMiniPlay.setImageResource(android.R.drawable.ic_media_play);
                    Log.d(TAG, "Paused playback");
                } else {
                    Song currentSong = playbackManager.getCurrentSong();
                    if (currentSong != null) {
                        playbackManager.resume();
                        btnMiniPlay.setImageResource(android.R.drawable.ic_media_pause);
                        Log.d(TAG, "Resumed playback: " + currentSong.getTitle());
                    } else {
                        Toast.makeText(this, "No song selected", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "No current song to resume");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in play/pause: " + e.getMessage());
                Toast.makeText(this, "Error controlling playback", Toast.LENGTH_SHORT).show();
            }
        });

        // Previous button
        btnMiniPrevious.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Previous button clicked");
                playbackManager.previous();
            } catch (Exception e) {
                Log.e(TAG, "Error in previous: " + e.getMessage());
                Toast.makeText(this, "Error going to previous", Toast.LENGTH_SHORT).show();
            }
        });

        // Next button
        btnMiniNext.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Next button clicked");
                playbackManager.next();
            } catch (Exception e) {
                Log.e(TAG, "Error in next: " + e.getMessage());
                Toast.makeText(this, "Error going to next", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "Mini player controls setup complete");
    }

    private void updateMiniPlayer(Song song) {
        try {
            if (song != null) {
                txtMiniTitle.setText(song.getTitle());
                txtMiniArtist.setText(song.getArtist());
                btnMiniPlay.setImageResource(android.R.drawable.ic_media_pause);
                Log.d(TAG, "Updated miniplayer with: " + song.getTitle());
            } else {
                txtMiniTitle.setText("Nothing Playing");
                txtMiniArtist.setText("Import music to begin");
                btnMiniPlay.setImageResource(android.R.drawable.ic_media_play);
                Log.d(TAG, "Updated miniplayer with empty state");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating miniplayer: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        try {
            adapter = new SongAdapter(this);
            recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
            recyclerSongs.setAdapter(adapter);
            Log.d(TAG, "RecyclerView setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
        }
    }

    private void setupViewModel() {
        try {
            homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

            homeViewModel.getSongs().observe(this, songs -> {
                try {
                    allSongs = songs;
                    adapter.setSongs(songs);

                    // Set playlist in PlaybackManager
                    playbackManager.setPlaylist(songs);

                    // Update Song Count
                    if (txtSongCount != null) {
                        txtSongCount.setText(songs.size() + " Songs");
                    }

                    // Hide empty state when songs are available
                    if (emptyState != null) {
                        if (songs.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                        }
                    }

                    Log.d(TAG, "Songs loaded: " + songs.size());
                } catch (Exception e) {
                    Log.e(TAG, "Error updating UI with songs: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ViewModel: " + e.getMessage());
        }
    }

    private void checkPermissionAndLoadSongs() {
        try {
            if (PermissionHelper.hasAudioPermission(this)) {
                homeViewModel.loadSongs();
                Log.d(TAG, "Permission granted, loading songs");
            } else {
                PermissionHelper.requestAudioPermission(this);
                Log.d(TAG, "Requesting permission");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == PermissionHelper.AUDIO_PERMISSION_REQUEST) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    homeViewModel.loadSongs();
                    Log.d(TAG, "Permission granted after request");
                } else {
                    Toast.makeText(this, "Permission required to access music", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Permission denied");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: " + e.getMessage());
        }
    }

    @Override
    public void onSongClick(Song song, int position) {
        try {
            Log.d(TAG, "Song clicked: " + song.getTitle());
            Log.d(TAG, "Song URI: " + song.getMediaUri());

            // Play the song
            playbackManager.play(song);

            Toast.makeText(this, "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
            Toast.makeText(this, "Error playing song: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Release resources when activity is destroyed
            playbackManager.release();
            Log.d(TAG, "PlaybackManager released");
        } catch (Exception e) {
            Log.e(TAG, "Error releasing PlaybackManager: " + e.getMessage());
        }
    }
}