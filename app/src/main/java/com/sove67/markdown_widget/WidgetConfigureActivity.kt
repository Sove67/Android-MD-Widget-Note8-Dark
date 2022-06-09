package com.sove67.markdown_widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.sove67.markdown_widget.databinding.MarkdownFileWidgetConfigureBinding

/**
 * The configuration screen for the [WidgetProvider] AppWidget.
 */

internal const val PREF_FILE = "filepath"
internal const val PREF_BEHAVIOUR = "behaviour"

class WidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var inputFilePath: EditText
    private lateinit var inputFileName: String

    // Browse the Android Filesystem for files of type: Any
    private val onBrowse = View.OnClickListener {
        // https://developer.android.com/reference/android/content/Intent#ACTION_OPEN_DOCUMENT
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION.or(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select a markdown file"),
            0
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data?.data != null) {
            val uri: Uri = data.data!!
            inputFileName = uri.toString()

            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val context = this@WidgetConfigureActivity
            val text = uri.toString()
            inputFilePath.setText(text.toCharArray(), 0, text.length)

            savePref(context, appWidgetId, PREF_FILE, text)
        }
    }

    private val onAddWidget = View.OnClickListener {
        val context = this@WidgetConfigureActivity

        // Update the app widget via the configuration activity
        getUpdatePendingIntent(context, appWidgetId).send()

        val widgetText = inputFilePath.text.toString()
        savePref(context, appWidgetId, PREF_FILE, widgetText)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private lateinit var binding: MarkdownFileWidgetConfigureBinding

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.markdown_file_widget_configure)

        // Find the widget id from the intent.
        val configIntent = intent
        val extras = configIntent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        // Open the Configuration Menu
        binding = MarkdownFileWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inputFilePath = binding.inputFile
        binding.btnBrowse.setOnClickListener(onBrowse)
        binding.saveButton.setOnClickListener(onAddWidget)
    }
}

private const val PREFS_NAME = "com.sove67.markdown_widget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun savePref(context: Context, appWidgetId: Int, prefName: String, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString("$PREF_PREFIX_KEY$appWidgetId--$prefName", text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, use default
internal fun loadPref(context: Context, appWidgetId: Int, prefName: String, default: String): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString("$PREF_PREFIX_KEY$appWidgetId--$prefName", null)

    return titleValue ?: default
}

internal fun deletePrefs(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove("$PREF_PREFIX_KEY$appWidgetId--$PREF_BEHAVIOUR")
    prefs.remove("$PREF_PREFIX_KEY$appWidgetId--$PREF_FILE")
    prefs.apply()
}