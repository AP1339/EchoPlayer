package com.snehant.echoplayer.scanner;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.snehant.echoplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicScanner {

    public List<Song> scanDevice(Context context) {

        List<Song> songList = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC"
        );

        if (cursor != null) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);

                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                );

                songList.add(new Song(
                        id,
                        cursor.getString(titleColumn),
                        cursor.getString(artistColumn),
                        cursor.getString(albumColumn),
                        uri.toString(),
                        cursor.getLong(durationColumn),
                        cursor.getLong(albumIdColumn)
                ));
            }

            cursor.close();
        }

        return songList;
    }
}