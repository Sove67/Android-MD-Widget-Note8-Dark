package ch.tiim.markdown_widget

import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private const val DEBUG = true

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (DEBUG) {
            val testTxt = """
                # Test
                
                - this is a list
                - list entry 2
                - [ ] Checkmark Entry
                [Testing Links](https://www.google.com/)
            """.trimIndent()

            val text = MarkdownParser().parse(testTxt)

            val textDisplay = findViewById<TextView>(R.id.test_display)

            textDisplay.text = text

            textDisplay.removeLinksUnderline()
            textDisplay.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}

fun TextView.removeLinksUnderline() {
    val spannable = SpannableString(text)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(object : URLSpan(u.url) {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
    }
    text = spannable
}