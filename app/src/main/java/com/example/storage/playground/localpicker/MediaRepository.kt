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

package com.example.storage.playground.localpicker

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.DATE_ADDED
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.provider.MediaStore.Files.FileColumns._ID
import kotlinx.coroutines.flow.flow

class MediaRepository(context: Context) {
    private val contentResolver = context.contentResolver

    enum class FileType {
        Image, Video, ImageAndVideo
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