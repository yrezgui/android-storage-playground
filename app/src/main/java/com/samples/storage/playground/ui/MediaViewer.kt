package com.samples.storage.playground.ui

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.MediaColumns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MediaViewer(modifier: Modifier = Modifier, uri: Uri?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enabledFlags by remember { mutableStateOf<Set<IntentFlag>>(emptySet()) }
    var mediaInfo by remember { mutableStateOf<MediaInfo?>(null) }

    fun viewMedia() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(mediaInfo?.uri, mediaInfo?.mimeType)

            if (enabledFlags.contains(IntentFlag.Read)) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (enabledFlags.contains(IntentFlag.Write)) {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }

        context.startActivity(intent)
    }

    fun sendMedia() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mediaInfo!!.mimeType
            putExtra(Intent.EXTRA_STREAM, mediaInfo!!.uri)

            if (enabledFlags.contains(IntentFlag.Read)) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (enabledFlags.contains(IntentFlag.Write)) {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }

        context.startActivity(intent)
    }

    fun editMedia() {
        val intent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(mediaInfo?.uri, mediaInfo?.mimeType)

            if (enabledFlags.contains(IntentFlag.Read)) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (enabledFlags.contains(IntentFlag.Write)) {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }

        context.startActivity(intent)
    }

    LaunchedEffect(uri) {
        uri?.let {
            val projection = arrayOf(
                MediaColumns.DISPLAY_NAME,
                MediaColumns.SIZE,
                MediaColumns.MIME_TYPE,
            )

            coroutineScope.launch(Dispatchers.IO) {
                val cursor = context.contentResolver.query(uri, projection, null, null, null)

                cursor?.use {
                    if (!cursor.moveToFirst()) {
                        throw Exception("Uri $uri could not be found")
                    }

                    val displayNameColumn = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaColumns.SIZE)
                    val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE)

                    mediaInfo = MediaInfo(
                        uri,
                        displayName = cursor.getStringOrNull(displayNameColumn)
                            ?: "NO FILENAME",
                        size = cursor.getIntOrNull(sizeColumn) ?: -1,
                        mimeType = cursor.getStringOrNull(mimeTypeColumn) ?: "NO MIME TYPE",
                    )
                }
            }
        }
    }

    if (mediaInfo == null) {
        LinearProgressIndicator(
            Modifier
                .semantics(mergeDescendants = true) {}
                .padding(10.dp))
    } else {
        Column(modifier) {

            ListItem(
                headlineContent = { Text(mediaInfo!!.displayName) },
                leadingContent = {
                    AsyncImage(
                        model = mediaInfo!!.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp)
                    )
                },
                supportingContent = {
                    Text("${mediaInfo!!.mimeType} || ${mediaInfo!!.size} B")
                }
            )
            Divider()
            ListItem(headlineContent = { Text(mediaInfo!!.uri.toString()) })
            Divider()

            IntentFlag.values().forEach { intentFlag ->
                ListItem(
                    headlineContent = { Text(intentFlag.name) },
                    trailingContent = {
                        Switch(
                            modifier = Modifier.semantics { contentDescription = "Enabled" },
                            checked = enabledFlags.contains(intentFlag),
                            onCheckedChange = { enabled ->
                                enabledFlags = if (enabled) {
                                    enabledFlags + intentFlag
                                } else {
                                    enabledFlags - intentFlag
                                }
                            })
                    }
                )
            }
            Divider()

            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = ::viewMedia) {
                    Text("View media")
                }
                TextButton(onClick = ::sendMedia) {
                    Text("Send media")
                }
                TextButton(onClick = ::editMedia) {
                    Text("Edit media")
                }
            }
        }
    }
}

private enum class IntentFlag {
    Read, Write
}

private data class MediaInfo(
    val uri: Uri,
    val displayName: String,
    val size: Int,
    val mimeType: String
)