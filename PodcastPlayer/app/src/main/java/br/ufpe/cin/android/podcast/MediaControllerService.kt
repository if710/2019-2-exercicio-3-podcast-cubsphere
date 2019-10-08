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
        //create notification channel for this foreground service
        val id = "media_service"
        val channel = NotificationChannel(id, "media_channel", NotificationManager.IMPORTANCE_DEFAULT)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)

        //build notification for this foreground service
        val builder = NotificationCompat.Builder(this, id)
        val notification = builder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        //start this foreground service
        startForeground(101, notification)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //set the viewModel
        viewModel = ItemFeedViewModel(application)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        val itemFeed = ItemFeed.fromIntent(intent!!)
        //play or pause media depending on intent action
        when(intent.action) {
            ACTION_PLAY -> playMedia(itemFeed)
            ACTION_PAUSE -> pauseMedia(itemFeed)
        }
    }

    //simply continue media playback
    private fun continueMedia(itemFeed: ItemFeed) {
        mediaPlayer?.start()
        if (mediaPlayer!!.isPlaying) {
            viewModel.updatePlay(itemFeed)
        }
    }

    //play a new media file
    private fun playNewMedia(itemFeed: ItemFeed) {
        discardPrevious()
        initializeMediaPlayer(itemFeed)

        mediaPlayer!!.start()
        if (mediaPlayer!!.isPlaying) {
            //seek to position where media playback for this file was last paused
            mediaPlayer!!.seekTo(itemFeed.pauseTime)
            viewModel.updatePlay(itemFeed)
            prevItemFeed = itemFeed
        }
    }

    //using the itemFeeds' titles to discern, determine if the media to be played is different from the currently paused file
    //perform playback action accordingly
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
                //store the pause time in the repository
                viewModel.updatePause(itemFeed, mediaPlayer!!.currentPosition)
            }
        }
    }

    //discard the media currently playing in preparation for a new media file
    private fun discardPrevious() {
        if (prevItemFeed != null) {
            //store the time at which the currently playing media was stopped
            viewModel.updatePause(prevItemFeed!!, mediaPlayer!!.currentPosition)
        }
        mediaPlayer?.reset()
    }

    private fun initializeMediaPlayer(itemFeed: ItemFeed) {
        if (mediaPlayer == null) {
            //initialize mediaPlayer for the first time
            val uri = Uri.parse(itemFeed.downloadLocation)!!
            mediaPlayer = MediaPlayer.create(applicationContext, uri)

            //delete file once playback is complete
            mediaPlayer!!.setOnCompletionListener {
                viewModel.updatePlaybackComplete(itemFeed!!)
                prevItemFeed = null
                mediaPlayer!!.reset()
            }
        } else {
            //update mediaPlayer by setting its data source
            mediaPlayer?.setDataSource(itemFeed.downloadLocation)
            mediaPlayer?.prepare()
        }
    }
}