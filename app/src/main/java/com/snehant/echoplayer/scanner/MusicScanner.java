package com.snehant.echoplayer.scanner;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.snehant.echoplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicScanner {

    public List<Song> scanDevice(Context context) {

        List<Song> songList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + "!=0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        );

        if (cursor != null) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {

                songList.add(
                        new Song(
                                cursor.getLong(idColumn),
                                cursor.getString(titleColumn),
                                cursor.getString(artistColumn),
                                cursor.getString(albumColumn),
                                cursor.getString(pathColumn),
                                cursor.getLong(durationColumn),
                                cursor.getLong(albumIdColumn)
                        )
                );

            }

            cursor.close();
        }

        return songList;
    }

}