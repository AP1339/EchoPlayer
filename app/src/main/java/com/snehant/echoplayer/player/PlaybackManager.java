package com.snehant.echoplayer.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.snehant.echoplayer.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaybackManager implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlaybackManager";
    private static PlaybackManager instance;
    private Context context;
    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private List<Song> originalPlaylist = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeatAll = false;
    private boolean isRepeatOne = false;
    private PlaybackListener listener;
    private ProgressListener progressListener;
    private Handler progressHandler = new Handler(Looper.getMainLooper());

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

            int index = playlist.indexOf(song);
            if (index == -1) {
                playlist.add(song);
                index = playlist.size() - 1;
                Log.d(TAG, "Song added to playlist at index: " + index);
            }

            if (currentIndex == index && currentIndex >= 0 && mediaPlayer != null) {
                if (!isPlaying) {
                    resume();
                }
                Log.d(TAG, "Same song, resuming");
                return;
            }

            currentIndex = index;

            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            } catch (Exception e) {
                Log.e(TAG, "Error resetting mediaplayer: " + e.getMessage());
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(this);
            }

            Log.d(TAG, "Setting data source for: " + song.getTitle());
            Log.d(TAG, "URI: " + song.getMediaUri());

            mediaPlayer.setDataSource(context, Uri.parse(song.getMediaUri()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            Log.d(TAG, "Song started successfully: " + song.getTitle());

            if (listener != null) {
                listener.onSongChanged(song);
                Log.d(TAG, "Listener notified of song change: " + song.getTitle());
            }

            startProgressUpdates();
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
                if (listener != null) {
                    listener.onPlayStateChanged(false);
                }
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
                if (listener != null) {
                    listener.onPlayStateChanged(true);
                }
                startProgressUpdates();
            } else {
                Log.d(TAG, "Cannot resume");
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

        if (isRepeatOne) {
            // Repeat one - play same song
            play(playlist.get(currentIndex));
            return;
        }

        int newIndex;
        if (currentIndex < playlist.size() - 1) {
            newIndex = currentIndex + 1;
        } else {
            if (isRepeatAll) {
                newIndex = 0; // Repeat all
            } else {
                // No repeat, stop playback
                currentIndex = -1;
                isPlaying = false;
                if (listener != null) {
                    listener.onSongChanged(null);
                }
                return;
            }
        }

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

    public void toggleShuffle() {
        isShuffle = !isShuffle;
        Log.d(TAG, "toggleShuffle: " + isShuffle);
        if (isShuffle) {
            originalPlaylist = new ArrayList<>(playlist);
            Collections.shuffle(playlist);
            currentIndex = -1;
        } else {
            if (!originalPlaylist.isEmpty()) {
                playlist = new ArrayList<>(originalPlaylist);
                currentIndex = -1;
            }
        }
        if (listener != null) {
            listener.onShuffleChanged(isShuffle);
        }
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void toggleRepeatAll() {
        isRepeatAll = !isRepeatAll;
        if (isRepeatAll) {
            isRepeatOne = false;
        }
        Log.d(TAG, "toggleRepeatAll: " + isRepeatAll);
        if (listener != null) {
            listener.onRepeatChanged(isRepeatAll ? 1 : 0);
        }
    }

    public void toggleRepeatOne() {
        isRepeatOne = !isRepeatOne;
        if (isRepeatOne) {
            isRepeatAll = false;
        }
        Log.d(TAG, "toggleRepeatOne: " + isRepeatOne);
        if (listener != null) {
            listener.onRepeatChanged(isRepeatOne ? 2 : 0);
        }
    }

    public boolean isRepeatAll() {
        return isRepeatAll;
    }

    public boolean isRepeatOne() {
        return isRepeatOne;
    }

    public int getRepeatMode() {
        if (isRepeatOne) return 2;
        if (isRepeatAll) return 1;
        return 0;
    }

    public void seekTo(int position) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
                Log.d(TAG, "Seeked to: " + position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error seeking: " + e.getMessage());
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getDuration() {
        try {
            return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void release() {
        Log.d(TAG, "release() called");
        if (progressHandler != null) {
            progressHandler.removeCallbacksAndMessages(null);
        }
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
            this.originalPlaylist = new ArrayList<>(songs);
            this.currentIndex = -1;
            Log.d(TAG, "Playlist set, size: " + this.playlist.size());
        } else {
            this.playlist = new ArrayList<>();
            this.originalPlaylist = new ArrayList<>();
            this.currentIndex = -1;
            Log.d(TAG, "Playlist set to empty");
        }
    }

    public void setPlaybackListener(PlaybackListener listener) {
        this.listener = listener;
        Log.d(TAG, "PlaybackListener set: " + (listener != null ? "not null" : "null"));
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
        if (isPlaying) {
            startProgressUpdates();
        }
    }

    private void startProgressUpdates() {
        progressHandler.removeCallbacksAndMessages(null);
        progressHandler.post(new Runnable() {
            @Override
            public void run() {
                if (progressListener != null && mediaPlayer != null && isPlaying) {
                    try {
                        progressListener.onProgressUpdate(
                                mediaPlayer.getCurrentPosition(),
                                mediaPlayer.getDuration()
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Error in progress update: " + e.getMessage());
                    }
                }
                if (isPlaying) {
                    progressHandler.postDelayed(this, 1000);
                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion() called - song finished playing");
        isPlaying = false;
        if (listener != null) {
            listener.onPlayStateChanged(false);
        }
        next();
    }

    public interface PlaybackListener {
        void onSongChanged(Song song);
        void onPlayStateChanged(boolean isPlaying);
        void onShuffleChanged(boolean isShuffle);
        void onRepeatChanged(int repeatMode);
    }

    public interface ProgressListener {
        void onProgressUpdate(int currentPosition, int totalDuration);
    }
}