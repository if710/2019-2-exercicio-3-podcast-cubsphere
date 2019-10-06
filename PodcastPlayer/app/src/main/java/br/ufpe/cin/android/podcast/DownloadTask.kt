package br.ufpe.cin.android.podcast

import android.os.AsyncTask
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DownloadTask(private val viewModel: ItemFeedViewModel): AsyncTask<String, Void, Void>() {
    override fun doInBackground(vararg params: String): Void? {
        //launches a coroutine in global scope
        //this is OK since this is a top-level coroutine operating on the whole application lifetime and is never cancelled
        GlobalScope.launch{
            val (_, _, result) = params[0]
                .httpGet()
                .responseString()

            when (result) {
                is Result.Failure -> {
                    failAwait()
                   // continue@loop
                }
                is Result.Success -> {
                    //updates data
                    viewModel.insertAll(Parser.parse(result.value))
                    successAwait()
                    resetBackoff()
                //    continue@loop
                }
            }
        }
        return null
    }

    private val secsToMillis = 1000L

    private val backoffMax = 300
    private var backoff = 2

    //initially a shorter wait timer in case of a minor network error
    //exponential backoff
    private suspend fun failAwait() {
        delay(backoff * secsToMillis)
        backoff =
            if (backoff >= backoffMax) {
                backoffMax
            } else {
                2 * backoff
            }
    }

    private fun resetBackoff() { backoff = 2 }

    //wait a reasonable amount of time before trying to fetch a possible update
    private val successTime = 300
    private suspend fun successAwait() {
        delay(successTime * secsToMillis)
    }
}