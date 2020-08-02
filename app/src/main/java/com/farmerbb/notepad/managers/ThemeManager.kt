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
import android.util.Log
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
import org.apache.commons.lang3.StringUtils
import us.feras.mdv.MarkdownView
import org.w3c.dom.Text

object ThemeManager {
    private const val TAG = "ThemeManager"
    @JvmStatic
    fun setBackgroundColor(context: Context, theme: String, noteViewEdit: View) {
        when {
            theme.contains("light") -> noteViewEdit.setBackgroundColor(ContextCompat.getColor(context, R.color.window_background))
            else -> noteViewEdit.setBackgroundColor(ContextCompat.getColor(context, R.color.window_background_dark))
        }
    }

    @JvmStatic
    fun setTextColor(context: Context, theme: String, noteContents: TextView) {
        when {
            theme.contains("light") -> noteContents.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary))
            else -> noteContents.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary_dark))
        }
    }

    @JvmStatic
    fun setTextColorDate(context: Context, theme: String, noteDate: TextView) {
        when {
            theme.contains("light") -> noteDate.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary))
            else -> noteDate.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary_dark))
        }
    }

    @JvmStatic
    fun setFontSize(pref: SharedPreferences, noteContents: TextView) {
        noteContents.textSize =
            when (pref.getString("font_size", "normal")) {
                "smallest"  ->  12f
                "small"     ->  14f
                "normal"    ->  16f
                "large"     ->  18f
                else        ->  20f
            }
    }

    @JvmStatic
    fun setFontSizeDate(pref: SharedPreferences, noteDate: TextView) {
        noteDate.textSize =
                when (pref.getString("font_size", "normal")) {
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
           with((pref.getString("theme", "light-sans")!!)) { // double bang (!) operator indicates that pref.getString will never return null (since we have a default value)
               when {
                   contains("sans") -> Typeface.SANS_SERIF
                   contains("serif") -> Typeface.SERIF
                   else -> Typeface.MONOSPACE
               }
       }
    }

    @JvmStatic
    fun getFontMarkdown(pref: SharedPreferences) : String {
       return with((pref.getString("theme", "light-sans")!!)) { // double bang (!) operator indicates that pref.getString will never return null (since we have a default value)
               when {
                   contains("sans") -> "sans"
                   contains("serif") -> "serif"
                   else -> "monospace"
               }
       }
    }
    @JvmStatic
    fun getTextColor(context: Context, theme: String) : Int {
        return with(theme) {
            when {
                contains("light") -> ContextCompat.getColor(context, R.color.text_color_primary)
                else -> ContextCompat.getColor(context, R.color.text_color_primary_dark)
            }
        }
    }

    @JvmStatic
    fun applyTextSettings(activity: FragmentActivity, noteContents: EditText) {
        val pref: SharedPreferences = activity.getSharedPreferences(activity.packageName + "_preferences", Context.MODE_PRIVATE)
        val scrollView: ScrollView = activity.findViewById(R.id.scrollView1)
        val theme: String = pref.getString("theme", "light-sans")!! // theme will never be null

        setBackgroundColor(activity, theme, scrollView)
        setTextColor(activity, theme, noteContents)
        setFont(pref, noteContents)
        setFontSize(pref, noteContents)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @JvmStatic
    fun applyNoteViewTheme(activity: FragmentActivity, noteContents: TextView?, markdownView: MarkdownView?, resources: Resources) : String {
        val pref: SharedPreferences = activity.getSharedPreferences(activity.packageName + "_preferences", Context.MODE_PRIVATE)
        val scrollView: ScrollView = activity.findViewById(R.id.scrollView)
        val theme: String = pref.getString("theme", "light-sans")!!
        Log.d(TAG, "current theme: $theme")
        Log.d(TAG, "noteContents are null: ${noteContents == null}")
        var fontFamily :String? = null
        var textColor = -1
        var textSize = -1F

        if (noteContents != null) {
            setTextColor(activity, theme, noteContents)
            setBackgroundColor(activity, theme, noteContents)
            setFont(pref, noteContents)
            setFontSize(pref, noteContents)
            textSize = noteContents.textSize
        }
        if (markdownView != null) {
            Log.d(TAG, "markdownView is not null")
            setBackgroundColor(activity, theme, markdownView)
            fontFamily = getFontMarkdown(pref)
            textColor = getTextColor(activity, theme)
        }
        setBackgroundColor(activity, theme, scrollView)
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