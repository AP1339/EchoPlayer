package com.snehant.echoplayer.repository;

import android.content.Context;

import com.snehant.echoplayer.models.Song;
import com.snehant.echoplayer.scanner.MusicScanner;

import java.util.List;

public class MusicRepository {

    private final MusicScanner musicScanner;

    public MusicRepository() {
        musicScanner = new MusicScanner();
    }

    public List<Song> getAllSongs(Context context) {
        return musicScanner.scanDevice(context);
    }
}