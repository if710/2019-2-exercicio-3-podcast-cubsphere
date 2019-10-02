package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File

const val filePrefix = "downloads/"

class DownloadEpisodeService : IntentService(DownloadEpisodeService::class.simpleName) {
    private val viewModel = ItemFeedViewModel(this.application)
    init {
        File(filePrefix).mkdirs()
    }

    override fun onHandleIntent(intent: Intent?) {
        downloadContent(intent!!)
    }

    private fun downloadContent(intent: Intent) {
        val title = intent.getStringExtra("title")!!
        val url = intent.getStringExtra("url")!!
        val fileLocation = filePrefix + Uri.parse(url).lastPathSegment
        viewModel.updateToDownloading(title)
        val (_, _, result) = url
            .httpGet()
            .response()

        when (result) {
            is Result.Failure -> {
                viewModel.updateToDefault(title)
            }
            is Result.Success -> {
                val data = result.get()
                File(fileLocation).writeBytes(data)
                viewModel.updateToFinished(title, fileLocation)
            }
        }
    }
}