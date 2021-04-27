package com.example.mvpmusicapp.data.source

import com.example.mvpmusicapp.data.model.Song
import com.example.mvpmusicapp.data.source.local.OnDataLoadedCallback

interface SongDataSource {
    fun getSongs(listener: OnDataLoadedCallback<MutableList<Song>>)
}
