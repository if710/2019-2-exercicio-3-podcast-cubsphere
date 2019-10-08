package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class FeedAdapter (ctx: Context) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        private val downloadButton: Button = itemView.findViewById(R.id.item_action)
        private val downloadingIcon: ProgressBar = itemView.findViewById(R.id.item_loading)
        private val playButton: ImageButton = itemView.findViewById(R.id.item_play)
        private val pauseButton: ImageButton = itemView.findViewById(R.id.item_pause)
        private val itemDate: TextView = itemView.findViewById(R.id.item_date)
        var itemFeed: ItemFeed? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun setTexts() {
            //set all text fields
            //cannot be done during init since init runs before the variable itemField is set by the adapter
            itemTitle.text = itemFeed!!.title
            itemDate.text = itemFeed!!.pubDate
        }

        //set a specific view as visible, and all others as gone
        private fun setVisible(view: View) {
            downloadButton.visibility = View.GONE
            downloadingIcon.visibility = View.GONE
            pauseButton.visibility = View.GONE
            playButton.visibility = View.GONE
            view.visibility = View.VISIBLE
        }

        private fun setDownloadButton() {
            setVisible(downloadButton)
            downloadButton.setOnClickListener {
                //send an intent to download the file
                val intent = Intent(downloadButton.context.applicationContext, DownloadEpisodeService::class.java)
                //send all required information via extras
                itemFeed!!.placeIntoIntent(intent)
                downloadButton.context.applicationContext.startService(intent)
            }
        }

        //show only a spinning download indicator
        private fun setDownloading() = setVisible(downloadingIcon)

        private fun setPlayButton() {
            //show only the play button
            setVisible(playButton)
            //send an intent requesting a service to play this audio
            playButton.setOnClickListener {
                val intent = Intent(playButton.context.applicationContext, MediaControllerService::class.java)
                itemFeed!!.placeIntoIntent(intent)
                intent.action = MediaControllerService.ACTION_PLAY
                playButton.context.applicationContext.startForegroundService(intent)
            }
        }

        private fun setPauseButton() {
            //show only the pause button
            setVisible(pauseButton)
            //send an intent requesting a service to pause this audio
            pauseButton.setOnClickListener {
                val intent = Intent(playButton.context.applicationContext, MediaControllerService::class.java)
                itemFeed!!.placeIntoIntent(intent)
                intent.action = MediaControllerService.ACTION_PAUSE
                playButton.context.applicationContext.startForegroundService(intent)
            }
        }

        fun setButtons() {
            //set buttons for this ItemFeed according to its status
            when (itemFeed!!.downloadStatus) {
                ItemFeed.DEFAULT -> setDownloadButton()
                ItemFeed.DOWNLOADING -> setDownloading()
                ItemFeed.READY -> setPlayButton()
                ItemFeed.PLAYING -> setPauseButton()
            }
        }

        override fun onClick(p0: View?) {
            if (p0 != null) {
                //send an intent to open an EpisodeDetailActivity
                val intent = Intent(p0.context.applicationContext, EpisodeDetailActivity::class.java)

                //send all required information via extras
                intent.putExtra("title", itemFeed!!.title)
                intent.putExtra("description", itemFeed!!.description)
                intent.putExtra("downloadLink", itemFeed!!.downloadLink)
                intent.putExtra("pubDate", itemFeed!!.pubDate)

                p0.context.startActivity(intent)
            }
        }
    }

    private val inflater = LayoutInflater.from(ctx)
    private var feed = emptyList<ItemFeed>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.itemlista, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        vh.itemFeed = feed[position]
        //set viewHolder's textFields
        vh.setTexts()
        //set viewHolder's download button action
        vh.setButtons()
    }

    fun setFeed(feed: List<ItemFeed>) {
        this.feed = feed
        notifyDataSetChanged()
    }

    override fun getItemCount() = feed.size
}