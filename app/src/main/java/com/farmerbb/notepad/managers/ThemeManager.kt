package com.farmerbb.notepad.managers

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.farmerbb.notepad.R
import us.feras.mdv.MarkdownView

object ThemeManager {
    @JvmStatic
    fun setBackgroundColor(context: Context, theme: String, noteViewEdit: View) = when {
        theme.contains("light") -> noteViewEdit.setBackgroundColor(ContextCompat.getColor(context, R.color.window_background))
        else -> noteViewEdit.setBackgroundColor(ContextCompat.getColor(context, R.color.window_background_dark))
    }

    @JvmStatic
    fun setTextColor(context: Context, theme: String, noteContents: TextView) = when {
        theme.contains("light") -> noteContents.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary))
        else -> noteContents.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary_dark))
    }

    @JvmStatic
    fun setTextColorDate(context: Context, theme: String, noteDate: TextView) = when {
        theme.contains("light") -> noteDate.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary))
        else -> noteDate.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary_dark))
    }

    @JvmStatic
    fun setFontSize(pref: SharedPreferences, noteContents: TextView) {
        noteContents.textSize =
                when(pref.getString("font_size", "normal")) {
                    "smallest" -> 12f
                    "small" -> 14f
                    "normal" -> 16f
                    "large" -> 18f
                    else -> 20f
                }
    }

    @JvmStatic
    fun setFontSizeDate(pref: SharedPreferences, noteDate: TextView) {
        noteDate.textSize =
                when(pref.getString("font_size", "normal")) {
                    "smallest"  ->  8f
                    "small"     ->  10f
                    "normal"    ->  12f
                    "large"     ->  14f
                    else        ->  16f
                }
    }

    @JvmStatic
    fun setFont(pref: SharedPreferences, noteContents: TextView) {
        noteContents.typeface =
                with(pref.getString("theme", "light-sans").orEmpty()) {
                    when {
                        contains("sans") -> Typeface.SANS_SERIF
                        contains("serif") -> Typeface.SERIF
                        else -> Typeface.MONOSPACE
                    }
                }
    }

    private fun getFontMarkdown(pref: SharedPreferences) =
            with(pref.getString("theme", "light-sans").orEmpty()) {
                when {
                    contains("sans") -> "sans"
                    contains("serif") -> "serif"
                    else -> "monospace"
                }
            }

    private fun getTextColor(context: Context, theme: String) = with(theme) {
        when {
            contains("light") -> ContextCompat.getColor(context, R.color.text_color_primary)
            else -> ContextCompat.getColor(context, R.color.text_color_primary_dark)
        }
    }

    @JvmStatic
    fun applyTextSettings(activity: FragmentActivity, noteContents: EditText) {
        val pref = activity.getSharedPreferences(activity.packageName + "_preferences", Context.MODE_PRIVATE)
        val scrollView = activity.findViewById<ScrollView>(R.id.scrollView1)
        val theme = pref.getString("theme", "light-sans").orEmpty()

        setBackgroundColor(activity, theme, scrollView)
        setTextColor(activity, theme, noteContents)
        setFont(pref, noteContents)
        setFontSize(pref, noteContents)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @JvmStatic
    fun applyNoteViewTheme(activity: FragmentActivity, noteContents: TextView?, markdownView: MarkdownView?) : String {
        val pref = activity.getSharedPreferences(activity.packageName + "_preferences", Context.MODE_PRIVATE)
        val scrollView = activity.findViewById<ScrollView>(R.id.scrollView)
        val theme = pref.getString("theme", "light-sans").orEmpty()

        var fontFamily = ""
        var textColor = -1
        var textSize = -1F

        noteContents?.let {
            setTextColor(activity, theme, noteContents)
            setBackgroundColor(activity, theme, noteContents)
            setFont(pref, noteContents)
            setFontSize(pref, noteContents)
            textSize = noteContents.textSize
        }

        markdownView?.let {
            setBackgroundColor(activity, theme, markdownView)
            fontFamily = getFontMarkdown(pref)
            textColor = getTextColor(activity, theme)
        }

        setBackgroundColor(activity, theme, scrollView)

        if(markdownView == null) return ""

        val density = activity.resources.displayMetrics.density
        val topBottom = activity.resources.getDimension(R.dimen.padding_top_bottom) / density
        val leftRight = activity.resources.getDimension(R.dimen.padding_left_right) / density

        val fontColor = Integer.toHexString(textColor).replace("ff", "")
        val linkColor = Integer.toHexString(TextView(activity).linkTextColors.defaultColor).replace("ff", "")

        markdownView.apply {
            settings.apply {
                javaScriptEnabled = false
                loadsImagesAutomatically = false
            }

            webViewClient = object: WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    try {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {}

                    return true
                }
            }
        }

        return """body {
                    margin: ${topBottom}px ${topBottom}px ${leftRight}px ${leftRight}px;
                    font-family: $fontFamily;
                    font-size: ${textSize}px;
                    color: #$fontColor;
                  }
                  
                  a {
                    color: #$linkColor;
                  }""".trimIndent()
    }
}