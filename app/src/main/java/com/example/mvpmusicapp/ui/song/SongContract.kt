package com.example.mvpmusicapp.ui.song

import com.example.mvpmusicapp.base.BasePresenter
import com.example.mvpmusicapp.data.model.Song

interface SongContract {
    interface View
    {
        fun showSongs(songs: MutableList<Song>)
        fun showError(message: String)
    }
    interface Presenter : BasePresenter<View> {
        fun getSongs()
    }
}
