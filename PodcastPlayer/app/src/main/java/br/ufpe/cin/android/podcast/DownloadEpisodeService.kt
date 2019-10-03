package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.os.Debug
import android.util.Log
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File
import java.util.logging.Logger

const val mediaSubdirectory = "media/"

class DownloadEpisodeService : IntentService(DownloadEpisodeService::class.simpleName) {

    private lateinit var viewModel: ItemFeedViewModel
    private lateinit var mediaFile: File

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        viewModel = ItemFeedViewModel(application)
        mediaFile = File(application.filesDir, mediaSubdirectory)
        mediaFile.mkdir()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        val itemFeed = reconstructItemFeed(intent!!)
        downloadContent(itemFeed)
    }

    private fun reconstructItemFeed(intent: Intent) : ItemFeed {
        return ItemFeed (
            title = intent.getStringExtra("title")!!,
            link = intent.getStringExtra("link")!!,
            pubDate = intent.getStringExtra("pubDate")!!,
            description = intent.getStringExtra("description")!!,
            downloadLink = intent.getStringExtra("downloadLink")!!
        )
    }

    private fun downloadContent(itemFeed: ItemFeed) {
        val fileLocation = Uri.parse(itemFeed.downloadLink).lastPathSegment!!

        viewModel.updateToDownloading(itemFeed)
        val (_, _, result) = itemFeed.downloadLink
            .httpGet()
            .response()

        when (result) {
            is Result.Failure -> {
                viewModel.updateToDefault(itemFeed)
            }
            is Result.Success -> {
                val data = result.get()
                val writeTo = File(mediaFile, fileLocation)
                writeTo.createNewFile()
                writeTo.writeBytes(data)
                viewModel.updateToFinished(itemFeed, fileLocation)
            }
        }
    }
}