package com.snehant.echoplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.snehant.echoplayer.models.Song;
import com.snehant.echoplayer.repository.MusicRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Song>> songsLiveData =
            new MutableLiveData<>();

    private final MusicRepository repository;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new MusicRepository();
    }

    public LiveData<List<Song>> getSongs() {
        return songsLiveData;
    }

    public void loadSongs() {

        executorService.execute(() -> {

            List<Song> songs =
                    repository.getAllSongs(getApplication());

            songsLiveData.postValue(songs);

        });

    }

}