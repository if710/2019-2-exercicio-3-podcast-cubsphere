package br.ufpe.cin.android.podcast

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.kittinunf.fuel.httpGet

class DownloadFeedWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    companion object {
        var viewModel: ItemFeedViewModel? = null
        var url: String? = null

        fun setViewModelAndUrl (vm: ItemFeedViewModel, link: String) {
            viewModel = vm
            url = link
        }
    }

    override fun doWork(): Result {
        val (_, _, result) = url!!
            .httpGet()
            .responseString()

        return when (result) {
            is com.github.kittinunf.result.Result.Success -> {
                viewModel!!.insertAll(Parser.parse(result.value))
                Result.success()
            }
            else -> Result.retry()
        }
    }
}