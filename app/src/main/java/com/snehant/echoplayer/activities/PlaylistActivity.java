package com.snehant.echoplayer.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.snehant.echoplayer.R;
import com.snehant.echoplayer.adapters.SongAdapter;
import com.snehant.echoplayer.interfaces.OnSongClickListener;
import com.snehant.echoplayer.models.Song;
import com.snehant.echoplayer.player.PlaybackManager;
import com.snehant.echoplayer.viewmodel.HomeViewModel;

public class PlaylistActivity extends AppCompatActivity implements OnSongClickListener {

    private RecyclerView recyclerSongs;
    private SongAdapter adapter;
    private HomeViewModel homeViewModel;
    private TextView txtTitle, txtSongCount;
    private PlaybackManager playbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playbackManager = PlaybackManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews() {
        recyclerSongs = findViewById(R.id.recyclerSongs);
        txtTitle = findViewById(R.id.txtTitle);
        txtSongCount = findViewById(R.id.txtSongCount);

        txtTitle.setText("Local Music");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SongAdapter(this);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        recyclerSongs.setAdapter(adapter);
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getSongs().observe(this, songs -> {
            adapter.setAllSongs(songs);
            if (txtSongCount != null) {
                txtSongCount.setText(songs.size() + " songs");
            }
        });

        homeViewModel.loadSongs();
    }

    @Override
    public void onSongClick(Song song, int position) {
        playbackManager.play(song);
        finish();
    }
}