package br.ufpe.cin.android.podcast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        private val INTERNET_PERMISSIONS = arrayOf(Manifest.permission.INTERNET)
        private const val INTERNET_REQUEST = 710

        private val FOREGROUND_PERMISSIONS = arrayOf(Manifest.permission.FOREGROUND_SERVICE)
        private const val FOREGROUND_REQUEST = 711
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
        viewModel!!.unsetPlaying()
        viewModel!!.unsetDownloading()

        //initiate internet-related tasks
        handleInternet()

        //request foreground permissions
        handleForeground()
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
        }
    }

    private fun hasInternet() = Manifest.permission.INTERNET.hasPermission()
    private fun hasForeground() = Manifest.permission.FOREGROUND_SERVICE.hasPermission()

    private fun requestInternet() = ActivityCompat.requestPermissions(this, INTERNET_PERMISSIONS, INTERNET_REQUEST)
    private fun requestForeground() = ActivityCompat.requestPermissions(this, FOREGROUND_PERMISSIONS, FOREGROUND_REQUEST)

    private fun downloadData() = DownloadTask(viewModel!!).execute(resourceURL)

    private fun String.hasPermission(): Boolean =
        PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this@MainActivity,this)
}
