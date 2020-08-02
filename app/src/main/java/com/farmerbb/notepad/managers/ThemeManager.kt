package com.farmerbb.notepad.managers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.FileUriExposedException
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.farmerbb.notepad.R
import org.apache.commons.lang3.StringUtils
import org.w3c.dom.Text
import us.feras.mdv.MarkdownView

object ThemeManager {

    @JvmStatic
    fun applyTextSettings(activity: FragmentActivity, noteContents: EditText) {
        val pref: SharedPreferences = activity.getSharedPreferences(activity.packageName + "_preferences", Context.MODE_PRIVATE)
        val scrollView: ScrollView = activity.findViewById(R.id.scrollView1)
        val theme: String = pref.getString("theme", "light-sans") ?: "hello"

        if (theme.contains("light")) {
            noteContents.setTextColor(ContextCompat.getColor(activity, R.color.text_color_primary))
            noteContents.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background))
            scrollView.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background))
        }

        if (theme.contains("dark")) {
            noteContents.setTextColor(ContextCompat.getColor(activity, R.color.text_color_primary_dark))
            noteContents.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background_dark))
            scrollView.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background_dark))
        }
        with(theme) {
            when {
                contains("sans") -> noteContents.typeface = Typeface.SANS_SERIF
                contains("serif") -> noteContents.typeface = Typeface.SERIF
                else -> noteContents.typeface = Typeface.MONOSPACE
            }
        }

        when (pref.getString("font_size", "normal")) {
            "smallest" -> noteContents.textSize = 12f
            "small" -> noteContents.textSize = 14f
            "normal" -> noteContents.textSize = 16f
            "large" -> noteContents.textSize = 18f
            "largest" -> noteContents.textSize = 20f
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @JvmStatic
    fun applyNoteViewTheme(activity: FragmentActivity, noteContents: TextView?, markdownView: MarkdownView?, resources: Resources) : String {
        val pref: SharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_PRIVATE)
        val scrollView: ScrollView = activity.findViewById(R.id.scrollView)
        val theme: String? = pref.getString("theme", "light-sans")
        var textSize = -1
        var textColor = -1

        var fontFamily: String? = null

        if (theme!!.contains("light")) {
            if (noteContents != null) {
                noteContents.setTextColor(ContextCompat.getColor(activity, R.color.text_color_primary))
                noteContents.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background))
            }
            if (markdownView != null) {
                markdownView.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background))
                textColor = ContextCompat.getColor(activity, R.color.text_color_primary)
            }
            scrollView.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background))
        }

        if (theme!!.contains("dark")) {
            if (noteContents != null) {
                noteContents.setTextColor(ContextCompat.getColor(activity, R.color.text_color_primary_dark))
                noteContents.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background_dark))
            }
            if (markdownView != null) {
                markdownView.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background_dark))
                textColor = ContextCompat.getColor(activity, R.color.text_color_primary_dark)
            }
            scrollView.setBackgroundColor(ContextCompat.getColor(activity, R.color.window_background_dark))
        }

        if (theme!!.contains("sans")) {
            if (noteContents != null) noteContents.setTypeface(Typeface.SANS_SERIF)
            if (markdownView != null) fontFamily = "sans-serif"
        }

        if (theme!!.contains("serif")) {
            if (noteContents != null) noteContents.setTypeface(Typeface.SERIF)
            if (markdownView != null) fontFamily = "serif"
        }

        if (theme!!.contains("monospace")) {
            if (noteContents != null) noteContents.setTypeface(Typeface.MONOSPACE)
            if (markdownView != null) fontFamily = "monospace"
        }

        when (pref.getString("font_size", "normal")) {
            "smallest" -> textSize = 12
            "small" -> textSize = 14
            "normal" -> textSize = 16
            "large" -> textSize = 18
            "largest" -> textSize = 20
        }

        if (noteContents != null) noteContents.textSize = textSize.toFloat()

        var css = ""
        if (markdownView != null) {
            val topBottom = " " + (resources.getDimension(R.dimen.padding_top_bottom) / resources.getDisplayMetrics().density).toString() + "px"
            val leftRight = " " + (resources.getDimension(R.dimen.padding_left_right) / resources.getDisplayMetrics().density).toString() + "px"
            val fontSize = " " + textSize.toString() + "px"
            val fontColor = " #" + StringUtils.remove(Integer.toHexString(textColor), "ff")
            val linkColor = " #" + StringUtils.remove(Integer.toHexString(TextView(activity).linkTextColors.defaultColor), "ff")
            css = "body { " +
                    "margin:" + topBottom + topBottom + leftRight + leftRight + "; " +
                    "font-family:" + fontFamily + "; " +
                    "font-size:" + fontSize + "; " +
                    "color:" + fontColor + "; " +
                    "}" +
                    "a { " +
                    "color:" + linkColor + "; " +
                    "}"
            markdownView.settings.javaScriptEnabled = false
            markdownView.settings.loadsImagesAutomatically = false
            markdownView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) try {
                        activity.startActivity(intent)
                    } catch (e: ActivityNotFoundException) { /* Gracefully fail */
                    } catch (e: FileUriExposedException) {
                    } else try {
                        activity.startActivity(intent)
                    } catch (e: ActivityNotFoundException) { /* Gracefully fail */
                    }
                    return true
                }
            }
        }
        return css
    }
}