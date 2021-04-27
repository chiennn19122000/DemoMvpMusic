package com.example.mvpmusicapp.data.source.local

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.example.mvpmusicapp.data.model.Song
import com.example.mvpmusicapp.data.source.SongDataSource

class LocalDataSource private constructor(private val contentResolver: ContentResolver) :
    SongDataSource {
    override fun getSongs(listener: OnDataLoadedCallback<MutableList<Song>>) {
        LocalAsyncTask(listener) {
            getSongFromDevice()
        }.execute()
    }

    private fun getSongFromDevice(): MutableList<Song> {
        val songs = mutableListOf<Song>()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC } != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.ArtistColumns.ARTIST
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                songs.add(Song(it))
            }
        }
        return songs
    }

    companion object {
        private var instance: LocalDataSource? = null
        fun getInstance(contentResolover: ContentResolver) =
            instance ?: LocalDataSource(contentResolover).also { instance = it }
    }
}
