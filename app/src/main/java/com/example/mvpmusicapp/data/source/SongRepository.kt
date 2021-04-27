package com.example.mvpmusicapp.data.source

import com.example.mvpmusicapp.data.model.Song
import com.example.mvpmusicapp.data.source.local.OnDataLoadedCallback

class SongRepository private constructor(
    private val local: SongDataSource
) : SongDataSource {

    override fun getSongs(listener: OnDataLoadedCallback<MutableList<Song>>) {
        local.getSongs(listener)
    }

    companion object {
        private var instance: SongRepository? = null
        fun getInstance(local: SongDataSource) = instance ?: SongRepository(local).also {
            instance = it
        }
    }
}
