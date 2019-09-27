package br.ufpe.cin.android.podcast

import android.Manifest
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            //permanently re-fetches data to get any updates to the feed
            loop@ while(true) {
                val (_, _, result) = params[0]
                    .httpGet()
                    .responseString()

                when (result) {
                    is Result.Failure -> {
                        failAwait()
                        continue@loop
                    }
                    is Result.Success -> {
                        //updates data
                        viewModel.insertAll(Parser.parse(result.value))
                        successAwait()
                        resetBackoff()
                        continue@loop
                    }
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

class MainActivity : AppCompatActivity() {

    companion object {
        private val INTERNET_PERMISSIONS = arrayOf(Manifest.permission.INTERNET)
        private const val INTERNET_REQUEST = 710
    }

    private var viewModel: ItemFeedViewModel? = null
    private val resourceURL = "https://animenewsnetwork.com/anncast/rss.xml?podcast=audio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get custom adapter
        val feedAdapter = FeedAdapter(this)

        //apply to recycler view
        findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = feedAdapter
        }

        //set view model to update the adapter with every modification to its underlying data
        viewModel = ViewModelProviders.of(this).get(ItemFeedViewModel::class.java)
        viewModel!!.itemFeeds.observe(this, Observer { feed -> feed?.let { feedAdapter.setFeed(feed) } })

        //initiate internet-related tasks
        handleInternet()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            INTERNET_REQUEST -> handleInternet()
        }
    }

    private var hasRequestedPreviously = false
    private fun handleInternet() {
        if (hasInternet()) {
            downloadData()
        } else if (!hasRequestedPreviously) {
            hasRequestedPreviously = true //only make the request once
            requestInternet()
        }
    }

    private fun hasInternet() = Manifest.permission.INTERNET.hasPermission()

    private fun requestInternet() = ActivityCompat.requestPermissions(this, INTERNET_PERMISSIONS, INTERNET_REQUEST)

    private fun downloadData() = DownloadTask(viewModel!!).execute(resourceURL)

    private fun String.hasPermission(): Boolean =
        PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this@MainActivity,this)
}
