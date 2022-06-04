package ch.tiim.markdown_widget

import android.graphics.Paint
import android.os.Bundle
import android.text.method.LinkMovementMethod
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

            textDisplay.paintFlags = textDisplay.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            textDisplay.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}