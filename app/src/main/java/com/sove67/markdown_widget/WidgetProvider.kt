package com.sove67.markdown_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.RemoteViews
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WidgetConfigureActivity]
 */
class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        if (context != null && appWidgetManager != null) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deletePrefs(context, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(null, "Loading Widget with ID \"$appWidgetId\"")
        // Load File
        val path = loadPref(context, appWidgetId, PREF_FILE, "")
        val fileUri = Uri.parse(path)
        val file = loadMarkdown(context, fileUri)
        val title = getObsidianFileNameFromPath(path)
        val bundle = when (val parserOutput = Parser().parse(title, file,true)) {
            is Parser.L -> {parserOutput.value}
            is Parser.R -> {throw Exception("Invalid datatype")}
        }

        // Create Remote View
        val views = RemoteViews(context.packageName, R.layout.markdown_file_widget)

        // Set Intent to pass to Remote View
        val serviceIntent = Intent(context, WidgetService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
        serviceIntent.putExtra("stringListBundle", bundle)

        // Set Adapter
        views.setRemoteAdapter(R.id.scrollable, serviceIntent)

        // Tap Handling
        views.setOnClickPendingIntent(
            R.id.markdown_display,
            getIntent(context, fileUri, context.contentResolver))

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

internal fun getUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
    val intentUpdate = Intent(context, WidgetProvider::class.java)
    intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val idArray = intArrayOf(appWidgetId)
    intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray)
    return PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intentUpdate,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun getIntent(context: Context, uri: Uri, c: ContentResolver): PendingIntent {
    val intent = Intent(Intent.ACTION_EDIT)
    intent.data = Uri.parse("obsidian://open?file=" + Uri.encode(getFileName(uri, c)))
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}

fun getFileName(uri: Uri, c: ContentResolver): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = c.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val i = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                result = cursor.getString(i)
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != (-1)) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun loadMarkdown(context: Context, uri: Uri): String {
    return try {
        val ins: InputStream = context.contentResolver.openInputStream(uri)!!
        val reader = BufferedReader(InputStreamReader(ins, "utf-8"))
        val string = reader.lines().collect(Collectors.joining("\n"))
        string
    } catch (err: FileNotFoundException) {
        ""
    }
}


