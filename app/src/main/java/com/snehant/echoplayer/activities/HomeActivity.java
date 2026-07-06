package com.snehant.echoplayer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.Calendar;
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
    private EditText edtSearch;
    private ImageView btnClearSearch;

    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "=== ACTIVITY CREATED ===");

        playbackManager = PlaybackManager.getInstance(this);

        // Force a Toast to confirm the activity is running
        Toast.makeText(this, "HomeActivity started!", Toast.LENGTH_SHORT).show();


        playbackManager = PlaybackManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupViewModel();
        checkPermissionAndLoadSongs();
        setupMiniPlayerControls();
        setupPlaybackListener();
        setupSearch();
        setupPlaylistClick();
        updateGreeting();

        // Update UI with current state
        updateMiniPlayer(playbackManager.getCurrentSong());

        // Restore playback state
        restorePlaybackState();
    }

    private void initViews() {
        Log.d(TAG, "!!! initViews() is RUNNING !!!");
        try {
            recyclerSongs = findViewById(R.id.recyclerSongs);
            txtSongCount = findViewById(R.id.txtSongCount);
            txtMiniTitle = findViewById(R.id.txtMiniTitle);
            txtMiniArtist = findViewById(R.id.txtMiniArtist);
            btnMiniPlay = findViewById(R.id.btnMiniPlay);
            btnMiniPrevious = findViewById(R.id.btnMiniPrevious);
            btnMiniNext = findViewById(R.id.btnMiniNext);
            emptyState = findViewById(R.id.layoutEmptyState);
            edtSearch = findViewById(R.id.edtSearch);
            btnClearSearch = findViewById(R.id.btnClearSearch);

            Log.d(TAG, "Basic views found - now looking for miniPlayerCard");

            // TRY ALL POSSIBLE WAYS TO FIND THE MINIPLAYER CARD
            View miniPlayerCard = null;

            // Method 1: Direct find
            miniPlayerCard = findViewById(R.id.miniPlayerCard);
            if (miniPlayerCard != null) {
                Log.d(TAG, "Method 1: Found miniPlayerCard directly!");
            }

            // Method 2: Through miniPlayer include
            if (miniPlayerCard == null) {
                View miniPlayerInclude = findViewById(R.id.miniPlayer);
                if (miniPlayerInclude != null) {
                    Log.d(TAG, "Found miniPlayer include, searching inside...");
                    miniPlayerCard = miniPlayerInclude.findViewById(R.id.miniPlayerCard);
                    if (miniPlayerCard != null) {
                        Log.d(TAG, "Method 2: Found miniPlayerCard through include!");
                    }
                }
            }

            // Method 3: Through miniPlayerContainer
            if (miniPlayerCard == null) {
                View miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
                if (miniPlayerContainer != null) {
                    Log.d(TAG, "Found miniPlayerContainer, searching inside...");
                    miniPlayerCard = miniPlayerContainer.findViewById(R.id.miniPlayerCard);
                    if (miniPlayerCard != null) {
                        Log.d(TAG, "Method 3: Found miniPlayerCard through container!");
                    }
                }
            }

            // Method 4: Try to find by traversing the view tree
            if (miniPlayerCard == null) {
                Log.d(TAG, "Trying to find by traversing view tree...");
                View rootView = findViewById(android.R.id.content);
                miniPlayerCard = findViewWithTag(rootView, "miniPlayerCard");
                if (miniPlayerCard != null) {
                    Log.d(TAG, "Method 4: Found miniPlayerCard by traversing!");
                }
            }

            if (miniPlayerCard != null) {
                Log.d(TAG, "✅✅✅ SUCCESS! MiniPlayerCard FOUND! Setting click listener.");
                miniPlayerCard.setOnClickListener(v -> {
                    Log.d(TAG, "🎵🎵🎵 MINIPLAYER TAPPED! Opening NowPlaying");
                    Intent intent = new Intent(HomeActivity.this, NowPlayingActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.e(TAG, "❌❌❌ FAILED! MiniPlayerCard NOT found ANYWHERE!");
            }

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method for Method 4
    private View findViewWithTag(View view, String tag) {
        if (view == null) return null;
        if (view.getId() == R.id.miniPlayerCard) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findViewWithTag(group.getChildAt(i), tag);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void setupPlaylistClick() {
        View cardLocalMusic = findViewById(R.id.cardLocalMusic);
        if (cardLocalMusic != null) {
            cardLocalMusic.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PlaylistActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupMiniPlayerClick() {
        View miniPlayerCard = findViewById(R.id.miniPlayerCard);
        if (miniPlayerCard != null) {
            Log.d(TAG, "MiniPlayerCard found! Setting click listener");
            miniPlayerCard.setOnClickListener(v -> {
                Log.d(TAG, "MiniPlayer clicked! Opening NowPlaying");
                Intent intent = new Intent(HomeActivity.this, NowPlayingActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "MiniPlayerCard NOT found! Check layout ID");
        }
    }

    private void setupPlaybackListener() {
        playbackManager.setPlaybackListener(new PlaybackManager.PlaybackListener() {
            @Override
            public void onSongChanged(Song song) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onSongChanged callback received: " + (song != null ? song.getTitle() : "null"));
                    updateMiniPlayer(song);
                    savePlaybackState();
                });
            }

            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onPlayStateChanged: " + isPlaying);
                    if (isPlaying) {
                        btnMiniPlay.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        btnMiniPlay.setImageResource(android.R.drawable.ic_media_play);
                    }
                    savePlaybackState();
                });
            }

            @Override
            public void onShuffleChanged(boolean isShuffle) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onShuffleChanged: " + isShuffle);
                });
            }

            @Override
            public void onRepeatChanged(int repeatMode) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onRepeatChanged: " + repeatMode);
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

                    // Update playlist count
                    TextView txtPlaylistCount = findViewById(R.id.txtPlaylistCount);
                    if (txtPlaylistCount != null) {
                        txtPlaylistCount.setText(songs.size() + " songs");
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

                    // Restore playback state after songs are loaded
                    restorePlaybackState();
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
            // Save state before destroying
            savePlaybackState();
            // Release resources when activity is destroyed
            playbackManager.release();
            Log.d(TAG, "PlaybackManager released");
        } catch (Exception e) {
            Log.e(TAG, "Error releasing PlaybackManager: " + e.getMessage());
        }
    }

    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "☀️ Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "🌤️ Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "🌅 Good Evening";
        } else {
            greeting = "🌙 Good Night";
        }

        TextView txtGreeting = findViewById(R.id.txtGreeting);
        if (txtGreeting != null) {
            txtGreeting.setText(greeting);
        }
    }

    private void setupSearch() {
        edtSearch = findViewById(R.id.edtSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            edtSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
            // Reset to show all songs (first 5)
            if (allSongs != null) {
                if (allSongs.size() > 5) {
                    adapter.setSongs(new ArrayList<>(allSongs.subList(0, 5)));
                } else {
                    adapter.setSongs(allSongs);
                }
            }
        });
    }

    private void filterSongs(String query) {
        if (allSongs == null) return;

        if (query.isEmpty()) {
            // Show first 5 songs when search is empty
            if (allSongs.size() > 5) {
                adapter.setSongs(new ArrayList<>(allSongs.subList(0, 5)));
            } else {
                adapter.setSongs(allSongs);
            }
            return;
        }

        List<Song> filtered = new ArrayList<>();
        for (Song song : allSongs) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase()) ||
                    song.getAlbum().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(song);
            }
        }

        // Show only 5 filtered results
        if (filtered.size() > 5) {
            adapter.setSongs(new ArrayList<>(filtered.subList(0, 5)));
        } else {
            adapter.setSongs(filtered);
        }
    }

    private void savePlaybackState() {
        SharedPreferences prefs = getSharedPreferences("playback", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Song currentSong = playbackManager.getCurrentSong();
        if (currentSong != null) {
            editor.putLong("song_id", currentSong.getId());
            editor.putInt("position", playbackManager.getCurrentPosition());
            editor.putBoolean("is_playing", playbackManager.isPlaying());
            editor.apply();
            Log.d(TAG, "Playback state saved");
        }
    }

    private void restorePlaybackState() {
        SharedPreferences prefs = getSharedPreferences("playback", MODE_PRIVATE);
        long songId = prefs.getLong("song_id", -1);
        int position = prefs.getInt("position", 0);
        boolean isPlaying = prefs.getBoolean("is_playing", false);

        if (songId != -1 && allSongs != null) {
            for (Song song : allSongs) {
                if (song.getId() == songId) {
                    Log.d(TAG, "Restoring playback state for: " + song.getTitle());
                    playbackManager.play(song);
                    playbackManager.seekTo(position);
                    if (!isPlaying) {
                        playbackManager.pause();
                    }
                    break;
                }
            }
        }
    }

    public void onMiniPlayerClick(View view) {
        Log.d(TAG, "🎵🎵🎵 onMiniPlayerClick called from XML!");
        Intent intent = new Intent(HomeActivity.this, NowPlayingActivity.class);
        startActivity(intent);
    }
}