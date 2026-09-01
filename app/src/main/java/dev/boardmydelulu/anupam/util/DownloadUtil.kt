package dev.boardmydelulu.anupam.util

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadItem(
    val id: Long,
    val title: String,
    val fileName: String,
    val status: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val uri: Uri?
)

object DownloadUtil {
    private const val PREFS = "boardmydelulu_downloads"
    private const val KEY = "download_ids"

    fun downloadSound(context: Context, mp3Url: String, title: String): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Toast.makeText(context, "Enable notifications for download progress", Toast.LENGTH_SHORT).show()
            }
        }
        return try {
            val sanitized = title.replace(Regex("[^a-zA-Z0-9 ]"), "").trim().ifBlank { "sound" }
            val fileName = "$sanitized.mp3"
            val request = DownloadManager.Request(Uri.parse(mp3Url))
                .setTitle(title)
                .setDescription("BoardMyDelulu")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "BoardMyDelulu/$fileName")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setMimeType("audio/mpeg")
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)
            saveDownloadId(context, downloadId, title, fileName)
            Toast.makeText(context, "Downloading: $title", Toast.LENGTH_SHORT).show()
            downloadId
        } catch (_: Exception) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            -1L
        }
    }

    private fun saveDownloadId(context: Context, id: Long, title: String, fileName: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefs.getString(KEY, "[]")) } catch (_: Exception) { JSONArray() }
        val obj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("fileName", fileName)
        }
        arr.put(obj)
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun getTrackedDownloads(context: Context): List<DownloadItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefs.getString(KEY, "[]")) } catch (_: Exception) { JSONArray() }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val items = mutableListOf<DownloadItem>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.getLong("id")
            val title = obj.getString("title")
            val fileName = obj.getString("fileName")

            val query = DownloadManager.Query().setFilterById(id)
            val cursor = dm.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val uri = try { dm.getUriForDownloadedFile(id) } catch (_: Exception) { null }
                items.add(DownloadItem(id, title, fileName, status, downloaded, total, uri))
                cursor.close()
            } else {
                cursor?.close()
            }
        }
        return items.reversed()
    }

    fun removeDownload(context: Context, downloadId: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.remove(downloadId)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefs.getString(KEY, "[]")) } catch (_: Exception) { JSONArray() }
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getLong("id") != downloadId) newArr.put(obj)
        }
        prefs.edit().putString(KEY, newArr.toString()).apply()
    }

    fun shareFile(context: Context, downloadId: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = try { dm.getUriForDownloadedFile(downloadId) } catch (_: Exception) { null }
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share Audio").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            grantUriToResolvedApps(context, chooser, uri)
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareSoundFile(context: Context, mp3Url: String, title: String) {
        Toast.makeText(context, "Preparing audio...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sanitized = title.replace(Regex("[^a-zA-Z0-9 ]"), "").trim().ifBlank { "sound" }
                val audioDir = File(context.cacheDir, "audio").apply { mkdirs() }
                val destFile = File(audioDir, "$sanitized.mp3")

                if (!destFile.exists() || destFile.length() == 0L) {
                    val url = URL(mp3Url)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.inputStream.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "dev.boardmydelulu.anupam.fileprovider",
                    destFile
                )

                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val chooser = Intent.createChooser(intent, "Share Audio").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    grantUriToResolvedApps(context, chooser, uri)
                    context.startActivity(chooser)
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Could not share audio file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun grantUriToResolvedApps(context: Context, chooser: Intent, uri: Uri) {
        try {
            val resInfoList = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: Exception) { }
    }

    fun shareSoundLink(context: Context, title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Listen to \"$title\" on BoardMyDelulu:\n$url")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Link").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun openDownloadsFolder(context: Context) {
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Location: Downloads/BoardMyDelulu", Toast.LENGTH_LONG).show()
        }
    }
}
