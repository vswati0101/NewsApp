package com.example.newsapp.Activities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.contentValuesOf
import com.example.newsapp.Model.ArticleModel
import com.example.newsapp.R
import com.example.newsapp.Utilities.HTMLImageGetter
import com.example.newsapp.Utilities.convertDateFormat
import com.example.newsapp.Utilities.loadImageWithGlide
import com.example.newsapp.databinding.ActivityArticleBinding
import com.zzhoujay.richtext.RichText
import java.net.URLClassLoader


class ArticleActivity : AppCompatActivity() {
    val activity=this
    lateinit var binding: ActivityArticleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            val model=intent.getSerializableExtra("model") as ArticleModel
            loadImageWithGlide(model.image,mPostImage,activity)
            loadImageWithGlide(model.authorPic,mAuthorImage,activity)
            mAuthorName.text=model.authorName
            mArticleTitleCard.text = Html.fromHtml(model.title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mDate.text= convertDateFormat(model.date)
            }
            else
                mDate.text="Unknown"
            mArticle.text=Html.fromHtml(model.content)
            val richText= RichText.fromHtml(model.content)
                .imageGetter(HTMLImageGetter(resources,mArticle,activity))
            richText.autoFix(true)
            richText.urlClick {
                try {
                    val builder=CustomTabsIntent.Builder()
                    val customTabsIntent=builder.build()
                    customTabsIntent.launchUrl(activity,Uri.parse(it))
                   // #posts
                } catch(e:Exception){
                    e.printStackTrace()
                }

                true
            }
            richText.into(mArticle)
        }
    }
}