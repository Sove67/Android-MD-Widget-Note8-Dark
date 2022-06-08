package com.sove67.markdown_widget

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MainService(
    context: Context,
    private val dataSource: ArrayList<SpannableStringBuilder>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int
    { return dataSource.size }

    override fun getItem(position: Int): Any
    { return dataSource[position] }

    override fun getItemId(position: Int): Long
    { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val view: View
        val holder: ViewHolder

        if (convertView == null) {

            view = inflater.inflate(R.layout.list_line, parent, false)

            holder = ViewHolder()
            holder.textView = view.findViewById(R.id.text) as TextView

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val item = getItem(position) as SpannableStringBuilder
        holder.textView.text = item

        return view
    }

    // The data required for a single list item view
    private class ViewHolder
    { lateinit var textView: TextView }
}