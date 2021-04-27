package com.example.mvpmusicapp.ui.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvpmusicapp.R
import com.example.mvpmusicapp.base.BaseAdapter
import com.example.mvpmusicapp.base.BaseViewHolder
import com.example.mvpmusicapp.data.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter(
    private var onItemClick: (Song) -> Unit
) : BaseAdapter<Song>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Song> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view, onItemClick)
    }
}
