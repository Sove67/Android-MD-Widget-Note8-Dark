package com.sove67.markdown_widget

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat

//const val TEST_FILE = "To%20Do"
class MainService(
    context: Context,
    private val dataSource: Pair<ArrayList<String>, ArrayList<SpannableStringBuilder>>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int
    { return dataSource.first.size }

    override fun getItem(position: Int): Pair<String, SpannableStringBuilder>
    { return Pair(dataSource.first[position], dataSource.second[position]) }

    override fun getItemId(position: Int): Long
    { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_line, parent, false)

            holder = ViewHolder()
            holder.imageButton = view.findViewById(R.id.prefix) as ImageButton
            holder.button = view.findViewById(R.id.text) as Button

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val item = getItem(position)
        holder.button.text = item.second
        /* For testing the header link.
        if (position == 0){
            holder.button.setOnClickListener {
                val pendingIntent = getObsidianURI(parent.context, TEST_FILE)
                pendingIntent.send()
            }
        }*/

        val imgName = item.first
        if (imgName != "") {
            val resID: Int = parent.context.resources.getIdentifier(
                imgName,
                "drawable",
                parent.context.packageName
            )
            holder.imageButton.setImageResource(resID)
            holder.imageButton.visibility = View.VISIBLE
            holder.imageButton.setColorFilter(
                ContextCompat.getColor(parent.context, R.color.purple_200),
                android.graphics.PorterDuff.Mode.MULTIPLY
                )
        } else {  holder.imageButton.visibility = View.GONE }


        return view
    }

    // The data required for a single list item view
    private class ViewHolder
    {
        lateinit var imageButton: ImageButton
        lateinit var button: Button
    }
}