package com.example.mvpmusicapp.ui.song

import android.Manifest
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.example.mvpmusicapp.R
import com.example.mvpmusicapp.base.BaseActivity
import com.example.mvpmusicapp.data.model.Song
import com.example.mvpmusicapp.data.source.SongRepository
import com.example.mvpmusicapp.data.source.local.LocalDataSource
import com.example.mvpmusicapp.ui.song.servive.SongForegroundService
import com.example.mvpmusicapp.ui.song.servive.SongNotificationControl
import kotlinx.android.synthetic.main.activity_song.*

class SongActivity : BaseActivity(), SongContract.View, SeekBar.OnSeekBarChangeListener {

    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val myHandler = Handler()
    private val deley = 100L
    private val adapter = SongAdapter(onItemClick = { song -> songClick(song) })
    private var songService: SongForegroundService? = null
    private var songs = mutableListOf<Song>()
    private var time = 0
    private var presenter: SongPresenter? = null
    private val connection = object : ServiceConnection, SongNotificationControl {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SongForegroundService.SongBinder
            songService = binder.getService()
            songService?.setSongs(songs)
            songService?.setCallback(this)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            songService?.stop()
        }

        override fun onChange() {
            showSongInfo()
        }

        override fun onPlayPause() {
            buttonPlayOrPause.apply {
                if (songService?.isPlaying() == true) {
                    setImageResource(R.drawable.ic_pause_circle_24dp)
                } else {
                    setImageResource(R.drawable.ic_play_circle_24dp)
                }
            }
        }

        override fun onHideLayoutControl() {
            groupControl.visibility = View.GONE
        }
    }

    override val layoutResource = R.layout.activity_song

    override fun initComponents() {

        if (checkPermissions()) {
            initAdapter()
            initView()
            initPresenter()
            Log.d("aaa", Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID))
            presenter?.start()
        } else {
            askForPermission()
        }
        buttonPlayOrPause.apply {
            setOnClickListener {
                if (songService?.isPlaying() == true) {
                    songService?.pause()
                    this.setImageResource(R.drawable.ic_play_circle_24dp)
                } else {
                    songService?.start()
                    this.setImageResource(R.drawable.ic_pause_circle_24dp)
                }
            }
        }
    }

    override fun showSongs(songs: MutableList<Song>) {
        this.songs = songs
        bindService(SongForegroundService.getIntent(this), connection, BIND_AUTO_CREATE)
        adapter.replaceData(songs)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        time = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        songService?.seekTo(time)
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (p in permissions) {
                val status = checkSelfPermission(p)
                if (status == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
        }
        return true
    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1)
        }
    }

    private fun initAdapter() {
        recyclerSongs.adapter = adapter
        recyclerSongs.setHasFixedSize(true)
    }

    private fun initView() {
        groupControl.visibility = View.GONE
        seekBar.setOnSeekBarChangeListener(this)
        buttonPrevious.setOnClickListener {
            songService?.jump(-1)
            showSongInfo()
        }
        buttonNext.setOnClickListener {
            songService?.jump(1)
            showSongInfo()
        }
    }

    private fun initPresenter() {
        presenter =
            SongPresenter(
                SongRepository.getInstance(LocalDataSource.getInstance(contentResolver)),
                this
            )
    }

    private fun songClick(song: Song) {
        songService?.create(songs.indexOf(song))
        groupControl.visibility = View.VISIBLE
        showSongInfo()
    }

    private fun showSongInfo() {
        textControlTitle.text = songService?.getTitle()
        songService?.getDuration()?.let {
                seekBar.max = it
        }
        myHandler.postDelayed(updateSeekBar, deley)
    }

    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {
            songService?.getCurrentPosition()?.let { seekBar.setProgress(it) }
            myHandler.postDelayed(this, deley)
        }
    }
}
