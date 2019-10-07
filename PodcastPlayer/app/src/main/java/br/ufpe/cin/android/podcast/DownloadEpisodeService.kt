package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

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
        val itemFeed = ItemFeed.fromIntent(intent!!)
        downloadContent(itemFeed)
    }

    private fun downloadContent(itemFeed: ItemFeed) {
        val fileLocation = Uri.parse(itemFeed.downloadLink).lastPathSegment!!

        GlobalScope.launch {
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
                    broadcastDownloadComplete(itemFeed, writeTo.canonicalPath)
                }
            }
        }
    }

    private fun broadcastDownloadComplete(itemFeed: ItemFeed, downloadLocation: String) {
        val lbm = LocalBroadcastManager.getInstance(applicationContext)
        val action = MainActivity.DOWNLOAD_FINISHED_ACTION
        val intent = Intent(action)
        itemFeed.placeIntoIntent(intent)
        intent.putExtra("downloadLocation", downloadLocation)
        lbm.sendBroadcast(intent)
    }
}