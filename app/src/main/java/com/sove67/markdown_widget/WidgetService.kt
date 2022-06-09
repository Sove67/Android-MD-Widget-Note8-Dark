package com.sove67.markdown_widget

import android.R.attr.resource
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService


class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetItemFactory(applicationContext, intent)
    }

    internal inner class WidgetItemFactory(context: Context, intent: Intent) :
        RemoteViewsFactory {
        private val context: Context
        private val appWidgetId: Int

        private lateinit var path: String
        private lateinit var file: String
        private lateinit var title: String

        private var imgList: ArrayList<String> = ArrayList()
        private var spanList: ArrayList<SpannableStringBuilder> = ArrayList()

        override fun onCreate() { updateData() }
        override fun onDataSetChanged() { updateData() }
        override fun onDestroy() {}

        override fun getCount(): Int { return spanList.size }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.list_line)
            views.setTextViewText(R.id.text, spanList[position])

            val imgName = imgList[position]
            if (imgName != "") {
                val resID: Int = context.resources.getIdentifier(
                    imgName,
                    "drawable",
                    packageName
                )
                val icon = BitmapFactory.decodeResource(
                    context.resources,
                    resID
                )
                val color = resources.getColor(R.color.purple_200)
                val tintedIcon = useImageAsMask(icon, color)
                views.setImageViewBitmap(R.id.prefix, tintedIcon)

                views.setViewVisibility(R.id.prefix, View.VISIBLE)
            } else { views.setViewVisibility(R.id.prefix, View.GONE) }

            /*
            if (position == 0){
                val listenerIntent = getIntent(context, path)
                views.setOnClickPendingIntent(R.id.text, listenerIntent)
            }*/

            return views
        }

        override fun getLoadingView(): RemoteViews? { return null }

        override fun getViewTypeCount(): Int { return 1 }

        override fun getItemId(position: Int): Long { return position.toLong() }

        override fun hasStableIds(): Boolean { return true }

        init {
            this.context = context
            appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        private fun updateData(){
            path = loadPref(context, appWidgetId, PREF_FILE, "")
            file = loadMarkdown(context, Uri.parse(path))
            title = formatFileName(getFileName(path))

            val output = Parser().parse(title, file)
            imgList = output.first
            spanList = output.second
        }
    }
}

fun useImageAsMask(bitmap: Bitmap, color: Int): Bitmap? {
    val paint = Paint()
    paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmapResult)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return bitmapResult
}