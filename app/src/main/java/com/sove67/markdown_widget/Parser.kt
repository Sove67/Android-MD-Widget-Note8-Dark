package com.sove67.markdown_widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.*
import android.util.Log
import android.view.View
import java.util.regex.Matcher
import java.util.regex.Pattern


private const val BREAK_STRING = "\n"

class Parser(context: Context) {

    fun parse (title: String?, md: String):ArrayList<SpannableStringBuilder> {
        val lines: ArrayList<String> = md
            .replace("\t", "    ")
            .split(BREAK_STRING)
            .toCollection(ArrayList())
        val spannedLines: ArrayList<SpannableStringBuilder> = ArrayList()

        // span and add title
        if (title != null) {
            val builder = SpannableStringBuilder(title)
            spannedLines.add(addStyle(builder, -1, 0, title.length))
        }

        for (line:String in lines)
        {
            Log.d(null, "Checking line: \"$line\"")
            var value = SpannableStringBuilder(line)
            val (index: Int, format: String) = findFormatPrefix(line)
            if (index != (-1))
            {
                // If the key is found in the format map
                formatPrefix[format]?.let {
                    Log.d(null, "Applying format ${it.second}.")

                    // replace it's occurrence in the line with the map's first value (the string)
                    // and update the builder to match
                    val finalLine = line.replaceFirst(format, it.first)
                    val builder = SpannableStringBuilder(finalLine)

                    // Add decoration depending on the map's second value (the int)
                    // https://developer.android.com/reference/android/text/style/package-summary

                    val prefixEnd = index + it.first.length

                    value = addStyle(builder, it.second, index, prefixEnd)
                }
            }

            // format remaining markdown
            // TO DO

            spannedLines.add(value)
        }

        return spannedLines
    }

    private val highlight: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#FFB07FFF"))
    private val bold: StyleSpan = StyleSpan(Typeface.BOLD)
    private val strikethrough = StrikethroughSpan()
    private val huge: RelativeSizeSpan = RelativeSizeSpan(1.75f)
    private val big: RelativeSizeSpan = RelativeSizeSpan(1.25f)
    private val specialCharacter = CustomTypefaceSpan(Typeface.createFromAsset(context.assets, "sawarabi_mincho_regular.ttf"))
    private val flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    // The OnClick listener is empty, as it doesn't seem to function in remote views.
    private val checkbox: ClickableSpan = object : ClickableSpan() {
        override fun onClick(textView: View) {
            //startActivity(Intent(this@MyActivity, NextActivity::class.java))
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }

    //①②③④⑤⑥⑦⑧⑨
    //。◉◎
    //◌○◔◑◕●
    private var formatPrefix: Map<String, Pair<String, Int>> = mapOf(
        "1. " to Pair("① ", 0),
        "2. " to Pair("② ", 0),
        "3. " to Pair("③ ", 0),
        "4. " to Pair("④ ", 0),
        "5. " to Pair("⑤ ", 0),
        "6. " to Pair("⑥ ", 0),
        "7. " to Pair("⑦ ", 0),
        "8. " to Pair("⑧ ", 0),
        "9. " to Pair("⑨ ", 0),
        "- " to Pair("◦ ", 0),
        "- [ ] " to Pair("◎ ", 1),
        "- [x] " to Pair("◉ ", 2),
        "# " to Pair("", 3),
        "## " to Pair("    ", 3),
        "### " to Pair("        ", 3)
    )

    private fun addStyle(builder: SpannableStringBuilder, format: Int, index: Int, prefixEnd: Int)
        : SpannableStringBuilder {
        when (format) {
            -1 -> {
                builder.setSpan(highlight, 0, builder.length, flag) //Purple
                builder.setSpan(bold, 0, builder.length, flag) //Bold
                builder.setSpan(huge, 0, builder.length, flag) //Size
            }
            // Bullet & Numbers
            0 -> {
                builder.setSpan(highlight, index, prefixEnd, flag)
                builder.setSpan(specialCharacter, index, prefixEnd, flag)
            }
            // Checkbox (Not Done)
            1 -> {
                builder.setSpan(checkbox, index, prefixEnd, flag)
                builder.setSpan(specialCharacter, index, prefixEnd, flag)
            }
            // Checkbox (Done)
            2 -> {
                builder.setSpan(checkbox, index, prefixEnd, flag)
                builder.setSpan(specialCharacter, index, prefixEnd, flag)
                builder.setSpan(strikethrough, prefixEnd, builder.length, flag)
            }
            // Headers
            3 -> {
                builder.setSpan(highlight, 0, builder.length, flag) //Purple
                builder.setSpan(bold, 0, builder.length, flag) //Bold
                builder.setSpan(big, 0, builder.length, flag) //Size
            }
            else -> throw Exception("Invalid format range in 'setBuilder'")
        }
        return builder
    }

    private fun findFormatPrefix(string: String): Pair<Int, String> {
        var foundKey = ""
        val endWhitespace: Int = getFirstNonWhitespace(string)
        // Check string for key
        var mapIndex = 0
        do {
            // Find index of key
            val key: String = formatPrefix.keys.toTypedArray()[mapIndex]
            val p: Pattern = Pattern.compile(Pattern.quote(key))
            val matcher: Matcher = p.matcher(string)
            val bool = matcher.find()

            // Check Against Whitespace Index
            if (bool && (endWhitespace == matcher.start()))
            {

                foundKey = key
            }
            mapIndex++
        }
        while (mapIndex < formatPrefix.keys.size)

        // Invalidate index if no key is found
        val start: Int =
            if (foundKey != "") endWhitespace else -1

        return Pair(start, foundKey)
    }

    private fun getFirstNonWhitespace(string:String): Int{

        val p: Pattern = Pattern.compile("\\s+")
        val matcher: Matcher = p.matcher(string)

        return if (string.isNotEmpty()
            && matcher.find()
            && string[0] == ' '
            && matcher.start() == 0){
            matcher.end()
        } else { 0 } //Default end index to 0
    }
}

fun getObsidianFileNameFromPath(path: String): String {
    val file = path.split("%2F").last()
    return file.replace("%20", " ").removeSuffix(".md")
}

class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, typeface)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, typeface)
    }

    companion object {
        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            paint.typeface = tf
        }
    }
}