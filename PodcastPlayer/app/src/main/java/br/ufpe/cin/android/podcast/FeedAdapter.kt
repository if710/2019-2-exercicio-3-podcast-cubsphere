package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class FeedAdapter (ctx: Context) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        private val button: Button = itemView.findViewById(R.id.item_action)
        private val itemDate: TextView = itemView.findViewById(R.id.item_date)
        var itemFeed: ItemFeed? = null

        init {
            button.text = "Download"

            //send an intent to view the download link
            button.setOnClickListener {
                val intent = Intent()
                intent.action = ACTION_VIEW
                intent.data = Uri.parse(itemFeed!!.downloadLink)
                it.context.startActivity(intent)
            }
            itemView.setOnClickListener(this)
        }

        fun setTexts() {
            //set all text fields
            //cannot be done during init since init runs before the variable itemField is set by the adapter
            itemTitle.text = itemFeed!!.title
            itemDate.text = itemFeed!!.pubDate
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
    }

    fun setFeed(feed: List<ItemFeed>) {
        this.feed = feed
        notifyDataSetChanged()
    }

    override fun getItemCount() = feed.size
}