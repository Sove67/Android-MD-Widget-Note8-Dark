package com.sove67.markdown_widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.widget.RemoteViews
import android.widget.RemoteViewsService


class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetItemFactory(applicationContext, intent)
    }

    // Intent handler, see this: https://medium.com/@workingkills/you-wont-believe-this-one-weird-trick-to-handle-android-intent-extras-with-kotlin-845ecf09e0e9


    internal inner class WidgetItemFactory(context: Context, intent: Intent) :
        RemoteViewsFactory {
        private val context: Context
        private val appWidgetId: Int

        private var stringList: ArrayList<ArrayList<String>> = ArrayList()
        private var spanList: ArrayList<SpannableStringBuilder> = ArrayList()

        override fun onCreate() { updateData() }
        override fun onDataSetChanged() { updateData() }
        override fun onDestroy() {}

        override fun getCount(): Int { return spanList.size }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.list_line)
            views.setTextViewText(R.id.text, spanList[position])
            return views
        }

        override fun getLoadingView(): RemoteViews? { return null }

        override fun getViewTypeCount(): Int { return 1 }

        override fun getItemId(position: Int): Long { return position.toLong() }

        override fun hasStableIds(): Boolean { return true }

        init {
            this.context = context
            val objectBundle = intent.getBundleExtra("stringListBundle")
            stringList = objectBundle!!.getSerializable("stringList") as ArrayList<ArrayList<String>>
            appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        private fun updateData(){
            val parser = Parser()
            val lineList: ArrayList<SpannableStringBuilder> = ArrayList()
            for (entry in stringList){
                val builder = SpannableStringBuilder(entry[0])

                for (i in 1 until entry.size){
                    val span = Parser.Span().parse(entry[i])
                    builder.setSpan(
                        parser.spanStyleMap[span.spanType],
                        span.start,
                        span.end,
                        parser.flag
                    )
                }
                lineList.add(builder)
            }

            spanList = lineList
        }
    }
}