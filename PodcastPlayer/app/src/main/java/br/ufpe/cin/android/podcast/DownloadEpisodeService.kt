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
        //obtain viewModel for the application
        viewModel = ItemFeedViewModel(application)

        //create directory to where the media file will be written (if it doesn't already exist)
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
            //update the view model to inform the download is commencing
            //visible in the app's UI as a spinning loading icon
            viewModel.updateToDownloading(itemFeed)
            val (_, _, result) = itemFeed.downloadLink
                .httpGet()
                .response()


            when (result) {
                is Result.Failure -> {
                    //update the view model to inform the download has failed
                    //returns the UI to a state in which the download may be attempted again
                    viewModel.updateToDefault(itemFeed)
                }
                is Result.Success -> {
                    //get data and write to file
                    val data = result.get()
                    val writeTo = File(mediaFile, fileLocation)
                    writeTo.createNewFile()
                    writeTo.writeBytes(data)

                    //send a broadcast to inform the download is finished
                    broadcastDownloadComplete(itemFeed, writeTo.canonicalPath)
                }
            }
        }
    }

    private fun broadcastDownloadComplete(itemFeed: ItemFeed, filepath: String) {
        //get the appropriate local broadcast manager
        val lbm = LocalBroadcastManager.getInstance(applicationContext)

        //set the intent's action so as to fall into the appropriate IntentFilter
        val action = MainActivity.DOWNLOAD_FINISHED_ACTION
        val intent = Intent(action)

        //pack all necessary data into the intent
        itemFeed.placeIntoIntent(intent)
        intent.putExtra("filepath", filepath)

        //broadcast the intent to the local broadcast manager
        lbm.sendBroadcast(intent)
    }
}