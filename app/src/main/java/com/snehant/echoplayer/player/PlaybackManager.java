package com.snehant.echoplayer.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.snehant.echoplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaybackManager implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlaybackManager";
    private static PlaybackManager instance;
    private Context context;
    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isPlaying = false;
    private PlaybackListener listener;

    private PlaybackManager(Context context) {
        this.context = context.getApplicationContext();
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(this);
    }

    public static synchronized PlaybackManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlaybackManager(context);
        }
        return instance;
    }

    public void play(Song song) {
        try {
            Log.d(TAG, "play() called with: " + (song != null ? song.getTitle() : "null"));

            if (song == null) {
                Log.w(TAG, "Song is null, cannot play");
                return;
            }

            // Find song in playlist
            int index = playlist.indexOf(song);
            if (index == -1) {
                playlist.add(song);
                index = playlist.size() - 1;
                Log.d(TAG, "Song added to playlist at index: " + index);
            }

            // Check if this song is already the current song AND we're not in the middle of changing songs
            if (currentIndex == index && currentIndex >= 0 && mediaPlayer != null) {
                // Same song, just resume if paused
                if (!isPlaying) {
                    resume();
                }
                Log.d(TAG, "Same song, resuming");
                return;
            }

            // Update current index BEFORE preparing media
            currentIndex = index;

            // Stop and reset media player
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            } catch (Exception e) {
                Log.e(TAG, "Error resetting mediaplayer: " + e.getMessage());
                // Create new mediaplayer if reset fails
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(this);
            }

            // Set new data source and prepare
            Log.d(TAG, "Setting data source for: " + song.getTitle());
            Log.d(TAG, "URI: " + song.getMediaUri());

            mediaPlayer.setDataSource(context, Uri.parse(song.getMediaUri()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            Log.d(TAG, "Song started successfully: " + song.getTitle());

            // Notify listener
            if (listener != null) {
                listener.onSongChanged(song);
                Log.d(TAG, "Listener notified of song change: " + song.getTitle());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void play(int index) {
        Log.d(TAG, "play(index) called with index: " + index);
        if (index >= 0 && index < playlist.size()) {
            play(playlist.get(index));
        } else {
            Log.w(TAG, "Invalid index: " + index + ", playlist size: " + playlist.size());
        }
    }

    public void pause() {
        Log.d(TAG, "pause() called");
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                Log.d(TAG, "Paused successfully");
            } else {
                Log.d(TAG, "MediaPlayer is not playing, ignoring pause");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in pause: " + e.getMessage());
        }
    }

    public void resume() {
        Log.d(TAG, "resume() called");
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying() && currentIndex >= 0) {
                mediaPlayer.start();
                isPlaying = true;
                Log.d(TAG, "Resumed successfully");
            } else {
                Log.d(TAG, "Cannot resume - mediaPlayer: " + (mediaPlayer != null) +
                        ", isPlaying: " + (mediaPlayer != null && mediaPlayer.isPlaying()) +
                        ", currentIndex: " + currentIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in resume: " + e.getMessage());
        }
    }

    public void previous() {
        Log.d(TAG, "previous() called, currentIndex: " + currentIndex + ", playlist size: " + playlist.size());
        if (playlist.isEmpty()) {
            Log.w(TAG, "Playlist is empty, cannot go previous");
            return;
        }

        int newIndex;
        if (currentIndex > 0) {
            newIndex = currentIndex - 1;
        } else {
            newIndex = playlist.size() - 1;
        }

        // Set currentIndex to -1 temporarily to prevent the "same song" check from returning early
        currentIndex = -1;
        Log.d(TAG, "New index: " + newIndex + ", playing: " + playlist.get(newIndex).getTitle());
        play(playlist.get(newIndex));
    }

    public void next() {
        Log.d(TAG, "next() called, currentIndex: " + currentIndex + ", playlist size: " + playlist.size());
        if (playlist.isEmpty()) {
            Log.w(TAG, "Playlist is empty, cannot go next");
            return;
        }

        int newIndex;
        if (currentIndex < playlist.size() - 1) {
            newIndex = currentIndex + 1;
        } else {
            newIndex = 0;
        }

        // Set currentIndex to -1 temporarily to prevent the "same song" check from returning early
        currentIndex = -1;
        Log.d(TAG, "New index: " + newIndex + ", playing: " + playlist.get(newIndex).getTitle());
        play(playlist.get(newIndex));
    }

    public void stop() {
        Log.d(TAG, "stop() called");
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                isPlaying = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in stop: " + e.getMessage());
        }
    }

    public void release() {
        Log.d(TAG, "release() called");
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing mediaplayer: " + e.getMessage());
            }
            mediaPlayer = null;
        }
        instance = null;
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setPlaylist(List<Song> songs) {
        Log.d(TAG, "setPlaylist() called with " + (songs != null ? songs.size() : 0) + " songs");
        if (songs != null) {
            this.playlist = new ArrayList<>(songs);
            this.currentIndex = -1;
            Log.d(TAG, "Playlist set, size: " + this.playlist.size());
        } else {
            this.playlist = new ArrayList<>();
            this.currentIndex = -1;
            Log.d(TAG, "Playlist set to empty");
        }
    }

    public void setPlaybackListener(PlaybackListener listener) {
        this.listener = listener;
        Log.d(TAG, "PlaybackListener set: " + (listener != null ? "not null" : "null"));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion() called - song finished playing");
        isPlaying = false;
        // Auto-play next song
        next();
    }

    public interface PlaybackListener {
        void onSongChanged(Song song);
    }
}