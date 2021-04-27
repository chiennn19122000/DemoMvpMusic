package com.example.mvpmusicapp.ui.song

import android.view.View
import com.example.mvpmusicapp.base.BaseViewHolder
import com.example.mvpmusicapp.data.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongViewHolder(
    private val itemView: View,
    onItemClick: (Song) -> Unit
) : BaseViewHolder<Song>(itemView) {

    private var itemData: Song? = null

    init {
        itemView.setOnClickListener {
            itemData?.let {
                onItemClick(it)
            }
        }
    }

    override fun bindData(item: Song) {
        itemData = item
        itemView.run {
            textTitle.text = item.title
            textSinger.text = item.singer
        }
    }
}
