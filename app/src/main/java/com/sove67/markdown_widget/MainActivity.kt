package com.sove67.markdown_widget

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

private const val TEST_TITLE = "Test"
private const val TEST_TEXT = """# Header 1
## Header 2
testing ~~strikethrough~~, **bolded**, and *italicized* texts
### Header 3
Is it only the first entry that loses it's mark?
- bullet point - with dash in it.
    - bullet point child
- [ ] Incomplete checkbox
- [x] Complete checkbox
    - [ ] Incomplete checkbox child
    - [x] Complete checkbox child
3 Blank Lines:



[Test Link to Google](https://www.google.com/)
What happens if I have so much text in one line that it expands past the maximum width? Does it get cut off or does it overflow nicely?
Final Entry"""

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(null, TEST_TEXT)
        val spanList = when (val parserOutput = Parser().parse(TEST_TITLE, TEST_TEXT, false)) {
            is Parser.L -> {throw Exception("Invalid datatype")}
            is Parser.R -> {parserOutput.value}
        }
        val scrollable = findViewById<ListView>(R.id.scrollable)
        scrollable.adapter = MainService(baseContext, spanList)

        /*
        scrollable.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (position == 0){
                val uri: Uri = Uri.parse(NOTE_URI)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }*/
    }
}