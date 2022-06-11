package com.sove67.markdown_widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.*
import java.util.regex.Matcher
import java.util.regex.Pattern


private const val BREAK_STRING = "\n"
private const val TAB_REPLACEMENT = "    "

class Parser {
    fun parse (title: String?, md: String)
    :Pair<ArrayList<String>, ArrayList<SpannableStringBuilder>> {
        val lines: ArrayList<String> = md
            .replace("\t", TAB_REPLACEMENT)
            .split(BREAK_STRING)
            .toCollection(ArrayList())
        val imgLines: ArrayList<String> = ArrayList()
        val spannedLines: ArrayList<SpannableStringBuilder> = ArrayList()

        // span and add title
        if (title != null) {
            val builder = SpannableStringBuilder(title)
            spannedLines.add(addLineStyle(builder, 0))
            imgLines.add("")
        }

        for (line:String in lines)
        {
            val (newString: String, indentLevel:Int, format:Prefix?) = cutPrefix(line)
            var value = SpannableStringBuilder(newString)
            if (format != null)
            {
                if (format.styleKey != -1)
                {
                    val builder = SpannableStringBuilder(newString)
                    value = addLineStyle(builder, format.styleKey)
                }

                imgLines.add(format.imgName)
            } else {
                imgLines.add("")
            }


            // format remaining markdown
            // TO DO

            spannedLines.add(value)
        }

        return Pair(imgLines, spannedLines)
    }

    private val highlight: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#FFB07FFF"))
    private val bold: StyleSpan = StyleSpan(Typeface.BOLD)
    private val strikethrough = StrikethroughSpan()
    private val huge: RelativeSizeSpan = RelativeSizeSpan(1.75f)
    private val big: RelativeSizeSpan = RelativeSizeSpan(1.25f)
    private val flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    //①②③④⑤⑥⑦⑧⑨
    //。◉◎
    //◌○◔◑◕●
    // https://stackoverflow.com/questions/17142331/convert-truetype-glyphs-to-png-image
    private val prefixFormats: Array<Prefix> = arrayOf(
        Prefix("1. ", -1, "circled_digit_one"),
        Prefix("2. ", -1, "circled_digit_two"),
        Prefix("3. ", -1, "circled_digit_three"),
        Prefix("4. ", -1, "circled_digit_four"),
        Prefix("5. ", -1, "circled_digit_five"),
        Prefix("6. ", -1, "circled_digit_six"),
        Prefix("7. ", -1, "circled_digit_seven"),
        Prefix("8. ", -1, "circled_digit_eight"),
        Prefix("9. ", -1, "circled_digit_nine"),
        Prefix("- ", -1, "white_bullet"),
        Prefix("- [ ] ", -1, "bullseye"),
        // Skip styleKey 0, reserved for title formatting
        Prefix("# ", 1, ""),
        Prefix("## ", 1, ""),
        Prefix("### ", 1, ""),
        Prefix("- [x] ", 2, "fisheye")
    )

    data class Prefix(val literal:String, val styleKey:Int, val imgName:String)

    // For styling the entire line
    private fun addLineStyle(builder: SpannableStringBuilder, format: Int): SpannableStringBuilder
    {
        when (format) {
            // Title
            0 -> {
                builder.setSpan(highlight, 0, builder.length, flag) //Purple
                builder.setSpan(bold, 0, builder.length, flag) //Bold
                builder.setSpan(huge, 0, builder.length, flag) //Size
            }
            // Headers
            1 -> {
                builder.setSpan(highlight, 0, builder.length, flag) //Purple
                builder.setSpan(bold, 0, builder.length, flag) //Bold
                builder.setSpan(big, 0, builder.length, flag) //Size
            }
            // Checkbox (Done)
            2 -> {
                builder.setSpan(strikethrough, 0, builder.length, flag)
            }
            // Call inline styling on the entire line
            else -> return addInlineStyle(builder, format, 0, builder.length)
        }
        return builder
    }

    // For Inline styles
    private fun addInlineStyle(builder: SpannableStringBuilder, format: Int, start: Int, end: Int)
        : SpannableStringBuilder {
        when (format) {
            else -> throw Exception("Invalid format range in 'setBuilder'")
        }
        return builder
    }

    private fun cutPrefix(string: String): Triple<String, Int, Prefix?> {
        // Set defaults
        var mapIndex = 0
        var indent = 0
        var endIndex = 0
        var style: Prefix? = null
        var newString = string

        // Check string for key
        do {
            // Find index of key
            val literal: String = prefixFormats[mapIndex].literal

            // Check Whitespace
            val whiteSpace: Pattern = Pattern.compile("\\s*")
            val whiteSpaceMatcher: Matcher = whiteSpace.matcher(string)
            if (string.isNotEmpty()
                && whiteSpaceMatcher.find()
                && whiteSpaceMatcher.start() == 0){
                indent = whiteSpaceMatcher.end() / TAB_REPLACEMENT.length
            }

            // Check Prefix
            val prefix: Pattern = Pattern.compile("\\s*${Pattern.quote(literal)}")
            val prefixMatcher: Matcher = prefix.matcher(string)
            if (string.isNotEmpty()
                && prefixMatcher.find()
                && prefixMatcher.start() == 0){
                endIndex = prefixMatcher.end()
                style = prefixFormats[mapIndex]
            }
            mapIndex++
        }
        while (mapIndex < prefixFormats.size)

        // If a prefix was found, remove it
        if (endIndex != 0)
        { newString = string.substring(endIndex, string.length) }

        // Invalidate index if no key is found
        return Triple(newString, indent, style)
    }
}

fun getFileName(path: String): String
{ return path.split("%2F").last() }

fun formatFileName(fileName: String): String
{ return fileName.replace("%20", " ").removeSuffix(".md") }

fun getObsidianURI(context: Context, path: String): PendingIntent {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("obsidian://open?file=" + getFileName(path)))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}