package com.snehant.echoplayer.models;

import java.util.Objects;

public class Song {

    private final long id;
    private final String title;
    private final String artist;
    private final String album;
    private final String mediaUri;
    private final long duration;
    private final long albumId;

    public Song(long id,
                String title,
                String artist,
                String album,
                String mediaUri,
                long duration,
                long albumId) {

        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.mediaUri = mediaUri;
        this.duration = duration;
        this.albumId = albumId;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public long getDuration() {
        return duration;
    }

    public long getAlbumId() {
        return albumId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id &&
                Objects.equals(mediaUri, song.mediaUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mediaUri);
    }
}