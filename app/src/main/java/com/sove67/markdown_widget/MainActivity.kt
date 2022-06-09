package com.sove67.markdown_widget

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

private const val TEST_TITLE = "Test"
const val TEST_TEXT = """# Header 1
## Header 2
testing ~~strikethrough~~, **bolded**, and *italicized* texts
### Header 3
Testing plain text before a list
- bullet point - with dash in it.
    - bullet point child
- [ ] Incomplete checkbox
- [x] Complete checkbox
    - [ ] Incomplete checkbox child
    - [x] Complete checkbox child
1. item 1
2. item 2
3. item 3
4. item 4
51. item 51
52. item 52
53. item 53
54. item 54

3 Blank Lines:



[Test Link to Google](https://www.google.com/)
What happens if I have so much text in one line that it expands past the maximum width? Does it get cut off or does it overflow nicely?
Final Entry"""

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val output = Parser().parse(TEST_TITLE, TEST_TEXT)
        val scrollable = findViewById<ListView>(R.id.scrollable)
        scrollable.adapter = MainService(baseContext, output)

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