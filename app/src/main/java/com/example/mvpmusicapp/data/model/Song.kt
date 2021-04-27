package com.example.mvpmusicapp.data.model

import android.database.Cursor
import android.provider.MediaStore

data class Song(var id: Long, var title: String, var singer: String) {
    constructor(cursor: Cursor) : this(
        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST))
    )
}
