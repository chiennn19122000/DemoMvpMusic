package com.example.mvpmusicapp.ui.song.servive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mvpmusicapp.R
import com.example.mvpmusicapp.data.model.Song
import java.net.URI

class SongForegroundService : Service(), MediaPlayer.OnCompletionListener {
    var mediaPlayer: MediaPlayer? = null
    private var remoteViews: RemoteViews? = null
    private var songs = mutableListOf<Song>()
    private var index = 0
    private var songNotificationControl: SongNotificationControl? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        val intentFilter = IntentFilter().apply {
            addAction(CLOSE)
            addAction(PREVIOUS)
            addAction(PLAY)
            addAction(NEXT)
        }
        registerReceiver(receiver, intentFilter)
    }

    override fun onBind(intent: Intent?): IBinder {
        return SongBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCompletion(mp: MediaPlayer?) {
        jump(1)
        songNotificationControl?.onChange()
    }

    fun setSongs(songs: MutableList<Song>) {
        if (mediaPlayer != null) {
            release()
        }
        this.songs = songs
    }

    fun setCallback(songControl: SongNotificationControl) {
        this.songNotificationControl = songControl
    }

    fun create(position: Int) {
        index = position
        release()
        mediaPlayer = MediaPlayer.create(
            this,
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs[index].id)
        )
        start()
        mediaPlayer?.setOnCompletionListener(this)
    }

    fun isPlaying(): Boolean {
        if (mediaPlayer != null) {
            return mediaPlayer?.isPlaying == true
        }
        return false
    }

    fun jump(step: Int) {
        index = (index + step + songs.size) % songs.size
        create(index)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun start() {
        mediaPlayer?.start()
        createNotification()
    }

    fun pause() {
        mediaPlayer?.pause()
        createNotification()
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun release() {
        mediaPlayer?.release()
    }

    fun getDuration() = mediaPlayer?.duration

    fun getCurrentPosition() = mediaPlayer?.currentPosition

    fun getTitle() = songs[index].title

    fun getSinger() = songs[index].singer

    private fun createNotification() {
        val intent = Intent(this, javaClass)
        startService(intent)

        createNotificationChannel()
        createRemoteViews()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setCustomBigContentView(remoteViews)
            .setSmallIcon(R.drawable.ic_musical_notification)
            .build()
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun createRemoteViews() {
        remoteViews = RemoteViews(packageName, R.layout.notification_music)
        registerAction(PLAY, R.id.imageNotificationPlayPause)
        registerAction(NEXT, R.id.imageNotificationNext)
        registerAction(PREVIOUS, R.id.imageNotificationPrevious)
        registerAction(CLOSE, R.id.imageNotificationCancel)
        remoteViews?.setTextViewText(R.id.textNotificationTitle, getTitle())
        remoteViews!!.setImageViewResource(
            R.id.imageNotificationPlayPause,
            if (isPlaying()) R.drawable.ic_pause_circle_24dp else R.drawable.ic_play_circle_24dp
        )
    }

    private fun registerAction(action: String, id: Int) {
        val intent = Intent(action)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        remoteViews?.setOnClickPendingIntent(id, pendingIntent)
    }

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                PLAY -> {
                    if (isPlaying()) {
                        pause()
                    } else {
                        start()
                    }
                    songNotificationControl?.onPlayPause()
                }
                NEXT -> {
                    jump(1)
                    songNotificationControl?.onChange()
                }
                PREVIOUS -> {
                    jump(-1)
                    songNotificationControl?.onChange()
                }
                CLOSE -> {
                    release()
                    mediaPlayer = null
                    stopForeground(true)
                    songNotificationControl?.onHideLayoutControl()
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        stopForeground(true)
        unregisterReceiver(receiver)
    }

    class SongBinder(private val foregroundService: SongForegroundService) : Binder() {
        fun getService(): SongForegroundService = foregroundService
    }



    companion object {
        const val CHANNEL_ID = "ForegroundService.channel.id"
        const val PLAY = "action.play"
        const val NEXT = "action.next"
        const val PREVIOUS = "action.previous"
        const val CLOSE = "action.close"

        fun getIntent(context:Context) = Intent(context,SongForegroundService::class.java)
    }
}
