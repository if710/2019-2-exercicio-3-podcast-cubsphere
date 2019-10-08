package br.ufpe.cin.android.podcast

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private val INTERNET_PERMISSIONS = arrayOf(Manifest.permission.INTERNET)
        private const val INTERNET_REQUEST = 710

        private val FOREGROUND_PERMISSIONS = arrayOf(Manifest.permission.FOREGROUND_SERVICE)
        private const val FOREGROUND_REQUEST = 711
        const val DOWNLOAD_FINISHED_ACTION = "br.ufpe.cin.android.podcast.cin.cubsphere.dlfinished"
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

        //reset downloading or playing status
        //this is necessary in case these tasks were initiated but aborted by terminating the app
        viewModel!!.unsetPlaying()
        viewModel!!.unsetDownloading()

        //set viewmodel and URL to the podcast for the worker to operate with
        DownloadFeedWorker.setViewModelAndUrl(viewModel!!, resourceURL)

        //initiate internet-related tasks
        handleInternet()

        //request foreground permissions
        handleForeground()

        //register listener for episode download completion
        val lbm = LocalBroadcastManager.getInstance(this)
        val filter = IntentFilter(DOWNLOAD_FINISHED_ACTION)
        lbm.registerReceiver(broadcastReceiver, filter)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        //perform necessary operations to register the feed's download
        override fun onReceive(context: Context?, intent: Intent?) {
            val itemFeed = ItemFeed.fromIntent(intent!!)
            val filepath = intent.getStringExtra("filepath")!!
            viewModel!!.updateDownloadFinished(itemFeed, filepath)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
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

    private var hasRequestedForegroundPreviously = false
    private fun handleForeground() {
        if (!hasForeground()) {
            requestForeground()
        } else if (!hasRequestedForegroundPreviously) {
            hasRequestedForegroundPreviously = true
            requestForeground()
        }
    }

    private fun hasInternet() = Manifest.permission.INTERNET.hasPermission()
    private fun hasForeground() = Manifest.permission.FOREGROUND_SERVICE.hasPermission()

    private fun requestInternet() = ActivityCompat.requestPermissions(this, INTERNET_PERMISSIONS, INTERNET_REQUEST)
    private fun requestForeground() = ActivityCompat.requestPermissions(this, FOREGROUND_PERMISSIONS, FOREGROUND_REQUEST)

    private fun downloadData() {
        val duration = getDurationFromPreferences()
        //use a periodic work request to update the feed repeatedly at set intervals
        val periodicWorkRequest = PeriodicWorkRequestBuilder<DownloadFeedWorker>(duration, TimeUnit.MILLISECONDS).build()
        //enqueue the periodic work request
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "br.ufpe.cin.android.podcast.cubsphere.download-feed",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest)
    }

    private fun getDurationFromPreferences(): Long {
        return 10000
    }

    private fun String.hasPermission(): Boolean =
        PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this@MainActivity,this)
}
