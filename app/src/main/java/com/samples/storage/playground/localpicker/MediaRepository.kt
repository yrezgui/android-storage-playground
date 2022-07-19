/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samples.storage.playground.localpicker

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.DATE_ADDED
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.provider.MediaStore.Files.FileColumns._ID
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.flow

class MediaRepository(private val context: Context) {
    private val contentResolver: ContentResolver
        get() = context.contentResolver

    val requiredPermissions by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        } else {
            listOf(READ_EXTERNAL_STORAGE)
        }
    }

    enum class FileType {
        Image, Video, ImageAndVideo
    }

    fun hasStorageAccess(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun fetchVisualMediaUris(fileType: FileType) = flow {
        val externalContentUri = MediaStore.Files.getContentUri("external")

        val imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            _ID,
            MEDIA_TYPE
        )

        val selectionArgs = when (fileType) {
            FileType.Image -> arrayOf(MEDIA_TYPE_IMAGE.toString())
            FileType.Video -> arrayOf(MEDIA_TYPE_VIDEO.toString())
            FileType.ImageAndVideo -> arrayOf(
                MEDIA_TYPE_IMAGE.toString(),
                MEDIA_TYPE_VIDEO.toString()
            )
        }

        val selection = List(selectionArgs.size) { "$MEDIA_TYPE = ?" }.joinToString(" OR ")

        val cursor = contentResolver.query(
            externalContentUri,
            projection,
            selection,
            selectionArgs,
            "$DATE_ADDED DESC"
        ) ?: throw Exception("Query could not be executed")

        cursor.use {
            val idColumn = cursor.getColumnIndexOrThrow(_ID)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val mediaType = cursor.getInt(mediaTypeColumn)
                val collection = if (mediaType == MEDIA_TYPE_IMAGE) {
                    imageCollection
                } else {
                    videoCollection
                }

                val contentUri = ContentUris.withAppendedId(collection, cursor.getLong(idColumn))
                emit(contentUri)
            }
        }
    }
}