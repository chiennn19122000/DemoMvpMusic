package com.example.mvpmusicapp.ui.song

import com.example.mvpmusicapp.data.model.Song
import com.example.mvpmusicapp.data.source.SongRepository
import com.example.mvpmusicapp.data.source.local.OnDataLoadedCallback

class SongPresenter(
    private val repository: SongRepository,
    private val view: SongContract.View
) : SongContract.Presenter {

    override fun start() {
        getSongs()
    }

    override fun getSongs() {
        repository.getSongs(object : OnDataLoadedCallback<MutableList<Song>> {
            override fun onSuccess(data: MutableList<Song>) {
                view.showSongs(data)
            }

            override fun onFailure(message: String) {
                view.showError(message)
            }
        })
    }
}
