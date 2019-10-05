package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.app.NotificationCompat

class MediaControllerService : IntentService(MediaControllerService::class.java.simpleName){

    companion object {
        const val ACTION_PAUSE = "br.ufpe.cin.android.podcast.pause"
        const val ACTION_PLAY = "br.ufpe.cin.android.podcast.play"

        private var mediaPlayer: MediaPlayer? = null
        private var prevItemFeed: ItemFeed? = null
    }

    private lateinit var viewModel: ItemFeedViewModel

    override fun onCreate() {
        val id = "media_service"
        val channel = NotificationChannel(id, "media_channel", NotificationManager.IMPORTANCE_DEFAULT)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, id)
        val notification = builder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(101, notification)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        viewModel = ItemFeedViewModel(application)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        val itemFeed = ItemFeed.fromIntent(intent!!)
        when(intent.action) {
            ACTION_PLAY -> playMedia(itemFeed)
            ACTION_PAUSE -> pauseMedia(itemFeed)
        }
    }

    private fun continueMedia(itemFeed: ItemFeed) {
        mediaPlayer?.start()
        if (mediaPlayer!!.isPlaying) {
            viewModel.updatePlay(itemFeed)
        }
    }

    private fun playNewMedia(itemFeed: ItemFeed) {
        discardPrevious()
        initializeMediaPlayer(itemFeed)

        mediaPlayer!!.start()
        if (mediaPlayer!!.isPlaying) {
            viewModel.updatePlay(itemFeed)
            prevItemFeed = itemFeed
        }
    }

    private fun playMedia(itemFeed: ItemFeed) {
        if (itemFeed.title == prevItemFeed?.title) {
            continueMedia(itemFeed)
        } else {
            playNewMedia(itemFeed)
        }
    }

    private fun pauseMedia(itemFeed: ItemFeed) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            if (!mediaPlayer!!.isPlaying){
                viewModel.updatePause(itemFeed)
            }
        }
    }

    private fun discardPrevious() {
        if (prevItemFeed != null) {
            viewModel.updatePause(prevItemFeed!!)
        }
        mediaPlayer?.reset()
    }

    private fun initializeMediaPlayer(itemFeed: ItemFeed) {
        if (mediaPlayer == null) {
            val uri = Uri.parse(itemFeed.downloadLocation)!!
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
        } else {
            mediaPlayer?.setDataSource(itemFeed.downloadLocation)
            mediaPlayer?.prepare()
        }
    }
}