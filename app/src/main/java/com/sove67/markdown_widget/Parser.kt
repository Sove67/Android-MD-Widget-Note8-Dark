package com.sove67.markdown_widget

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import java.util.regex.Matcher
import java.util.regex.Pattern


private const val BREAK_STRING = "\n"

private const val BUNDLE_NAME = "stringList"
class Parser {
    // Taken, with great relief, from https://stackoverflow.com/questions/28695254/kotlin-and-discriminated-unions-sum-types
    sealed class Either<out A, out B>
    class L<A>(val value: A) : Either<A, Nothing>()
    class R<B>(val value: B) : Either<Nothing, B>()

    /* Results from this function have to be separated out when it is called using this format:
     * when (result) {
     *      is L -> {} //For a bundle
     *      is R -> {} //For an array of SpannableStringBuilders
     * }
     *
     * Make sure to throw exceptions if you aren't handling one of the data types.
     */
    fun parse (title: String?, md: String, isBundled: Boolean):Either<Bundle, ArrayList<SpannableStringBuilder>> {

        val lines: ArrayList<String> = md.split(BREAK_STRING).toCollection(ArrayList())
        val spannedLines: ArrayList<Either<ArrayList<String>, SpannableStringBuilder>> = ArrayList()

        // span and add title
        if (title != null) {
            val builder = SpannableStringBuilder(title)
            val line: Either<ArrayList<String>, SpannableStringBuilder> =
                    addStyle(isBundled, arrayListOf(title), builder, -1, 0, title.length)
            spannedLines.add(line)
        }

        for (line:String in lines)
        {
            var value: Either<ArrayList<String>, SpannableStringBuilder> =
                if (isBundled) { L(arrayListOf(line)) }
                else { R(SpannableStringBuilder(line)) }

            val (index: Int, format: String) = findFormatPrefix(line)
            Log.d(null, "Returned index:${index}")
            if (index != (-1))
            {
                // If the key is found in the format map
                formatPrefix[format]?.let {
                    Log.d(null, "Applying format ${it.second} to \"$line\"")

                    // replace it's occurrence in the line with the map's first value (the string)
                    // and update the builder to match
                    val finalLine = line.replace(format, it.first)
                    val builder = SpannableStringBuilder(finalLine)

                    // Add decoration depending on the map's second value (the int)
                    // https://developer.android.com/reference/android/text/style/package-summary

                    val prefixEnd = index + it.first.length

                    value = addStyle(
                        isBundled,
                        arrayListOf(finalLine),
                        builder,
                        it.second,
                        index,
                        prefixEnd)
                }
            }

            // format remaining markdown
            // TO DO

            spannedLines.add(value)
        }

        // Split the either datatype inside the MutableList
        return when (spannedLines[0]) {
            is L -> {
                val list: ArrayList<ArrayList<String>> = ArrayList()
                for (item in spannedLines){
                    //Either<SpannableStringBuilder, ArrayList<String>>
                    val piece = item as L<ArrayList<String>>
                    list.add(piece.value)
                }
                val bundle = Bundle()
                bundle.putSerializable(BUNDLE_NAME, list)
                L(bundle)
            }
            is R -> {
                val list: ArrayList<SpannableStringBuilder> = ArrayList()
                for (item in spannedLines){
                    //Either<SpannableStringBuilder, ArrayList<String>>
                    val piece = item as R<SpannableStringBuilder>
                    list.add(piece.value)
                }
                R(list)
            }
        }
    }

    private val highlight: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#FFC19EFF"))
    private val bold: StyleSpan = StyleSpan(Typeface.BOLD)
    private val large: RelativeSizeSpan = RelativeSizeSpan(2f)
    val flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

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

    // Map of which encoded strings refer to which of these https://developer.android.com/reference/android/text/style/package-summary
    var spanStyleMap: Map<String, Any> = mapOf(
        "checkbox" to checkbox,
        "highlight" to highlight,
        "bold" to bold,
        "large" to large,
    )

    private var formatPrefix: Map<String, Pair<String, Int>> = mapOf(
        "- " to Pair("● ", 0),
        "- [ ]" to Pair("□ ", 1),
        "- [x]" to Pair("▣ ", 1),
        "# " to Pair("", 2),
        "## " to Pair("    ", 2),
        "### " to Pair("        ", 2)
    )

    // Find the first key in spanStyleMap that matches the given value
    private fun spanString(value: Any): String{
        return spanStyleMap.filterValues { it == value }.keys.first()
    }

    // Data required to add a Span to a SpannedStringBuilder
    class Span(var spanType:String="", var start:Int=0, var end:Int=0) {

        override fun toString(): String
        { return "$spanType,$start,$end" }

        fun parse(string:String): Span
        {
            val values = string.split(',')
            return Span(values[0], values[1].toInt(), values[2].toInt())
        }
    }

    private fun addStyle(isBundled: Boolean, list:ArrayList<String>, builder: SpannableStringBuilder, format: Int, index: Int, prefixEnd: Int)
        : Either<ArrayList<String>, SpannableStringBuilder> {
        if (isBundled) {
            val line = list[0]
            when (format) {
                -1 -> {
                    list.add(Span(spanString(highlight), index, prefixEnd).toString())
                    list.add(Span(spanString(bold), 0, line.length).toString())
                    list.add(Span(spanString(large), 0, line.length).toString())
                }
                // Bullet
                0 -> list.add(Span(spanString(highlight), index, prefixEnd).toString())
                // Checkbox (Done or Not Done)
                1 -> list.add(Span(spanString(checkbox), index, prefixEnd).toString())
                // Headers
                2 -> {
                    list.add(Span(spanString(highlight), 0, line.length).toString())
                    list.add(Span(spanString(bold), 0, line.length).toString())
                }
                else -> throw Exception("Invalid format range in 'getSpanList'")
            }
            return L(list.toCollection(ArrayList()))
        } else {
            when (format) {
                -1 -> {
                    builder.setSpan(highlight, 0, builder.length, flag) //Purple
                    builder.setSpan(bold, 0, builder.length, flag) //Bold
                    builder.setSpan(large, 0, builder.length, flag) //Size
                }
                // Bullet
                0 -> builder.setSpan(highlight, index, prefixEnd, flag)
                // Checkbox (Done or Not Done)
                1 -> builder.setSpan(checkbox, index, prefixEnd, flag)
                // Headers
                2 -> {
                    builder.setSpan(highlight, 0, builder.length, flag) //Purple
                    builder.setSpan(bold, 0, builder.length, flag) //Bold
                }
                else -> throw Exception("Invalid format range in 'setBuilder'")
            }
            return R(builder)
        }
    }

    // Generate a list of Spans as strings in an array, with the first entry being the text.
    private fun getSpanList(line:String, format: Int, index: Int, prefixEnd: Int): ArrayList<String> {
        val list:ArrayList<String> = ArrayList()
        list.add(line)
        when (format) {
            -1 -> {
                list.add(Span(spanString(highlight), index, prefixEnd).toString())
                list.add(Span(spanString(bold), 0, line.length).toString())
                list.add(Span(spanString(large), 0, line.length).toString())
            }
            // Bullet
            0 -> list.add(Span(spanString(highlight), index, prefixEnd).toString())
            // Checkbox (Done or Not Done)
            1 -> list.add(Span(spanString(checkbox), index, prefixEnd).toString())
            // Headers
            2 -> {
                list.add(Span(spanString(highlight), 0, line.length).toString())
                list.add(Span(spanString(bold), 0, line.length).toString())
            }
            else -> throw Exception("Invalid format range in 'getSpanList'")
        }
        return list.toCollection(ArrayList())
    }

    private fun createLine(string:String, spanList:List<Span>): ArrayList<String> {
        val line: ArrayList<String> = ArrayList()
        line.add(string)
        for (span in spanList)
        { line.add(span.spanType + "," + span.start + "," + span.end) }
        return line.toCollection(ArrayList())
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

            if (bool) Log.d(null, "Prefix Matcher Start: ${matcher.start()}")
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
        return if (string.isNotEmpty() && string[0] == ' '){

            val p: Pattern = Pattern.compile("\\s+")
            val matcher: Matcher = p.matcher(string)
            matcher.find()
            if (matcher.start() == 0) {
                Log.d(null, "Whitespace Matcher Start: ${matcher.start()}")
                matcher.end()
            } else 0
        } else { 0 } //Default end index to 0
    }
}

fun getObsidianFileNameFromPath(path: String): String {
    val file = path.split("%2F").last()
    return file.replace("%20", " ").removeSuffix(".md")
}
