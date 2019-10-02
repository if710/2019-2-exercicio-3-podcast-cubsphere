package br.ufpe.cin.android.podcast

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_episode_detail.*
import kotlinx.android.synthetic.main.itemlista.item_date
import kotlinx.android.synthetic.main.itemlista.item_title

class EpisodeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        //parse html in title and description
        val title = Html.fromHtml(intent.getStringExtra("title"), Html.FROM_HTML_MODE_COMPACT)
        val description = Html.fromHtml(intent.getStringExtra("description"), Html.FROM_HTML_MODE_COMPACT)

        //sets all textFields
        item_title.text = title
        item_date.text = intent.getStringExtra("pubDate")
        item_description.text = description
        val downloadLink = intent.getStringExtra("downloadLink")

        item_download.text = "Download"

        //send an intent to view the download link
        item_download.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(downloadLink)
            it.context.startActivity(intent)
        }
    }
}
