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

package com.samples.storage.playground.docsui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.samples.storage.playground.DeviceManager
import com.samples.storage.playground.DeviceManager.Companion.getDeviceInfo

class DocsUiViewModel : ViewModel() {

    enum class MaxItemsLimit {
        Single, Unlimited
    }

    enum class FileType {
        All, Image, Video, Audio, ImageAndVideo, Pdf, Zip, Text;

        fun getMimeType(): Array<String> {
            return when (this) {
                All -> arrayOf("*/*")
                Image -> arrayOf("image/*")
                Video -> arrayOf("video/*")
                Audio -> arrayOf("audio/*")
                ImageAndVideo -> arrayOf("image/*", "video/*")
                Pdf -> arrayOf("application/pdf")
                Zip -> arrayOf("application/zip")
                Text -> arrayOf("text/*")
            }
        }
    }

    enum class DocsUiIntent {
        GET_CONTENT, OPEN_DOCUMENT
    }

    data class UiState(
        val deviceInfo: DeviceManager.DeviceInfo = getDeviceInfo(),
        val docsUiIntent: DocsUiIntent = DocsUiIntent.GET_CONTENT,
        val fileTypeFilter: FileType = FileType.All,
        val maxItemsLimit: MaxItemsLimit = MaxItemsLimit.Unlimited,
        val selectedItems: List<Uri> = emptyList()
    ) {
        val mimeTypeFilter: Array<String>
            get() = fileTypeFilter.getMimeType()
    }

    var uiState by mutableStateOf(UiState())
        private set

    fun reset() {
        uiState = UiState()
    }

    fun onDocsUiIntentChange(newValue: DocsUiIntent) {
        uiState = uiState.copy(docsUiIntent = newValue)
    }

    fun onFileTypeFilterChange(newValue: FileType) {
        uiState = uiState.copy(fileTypeFilter = newValue)
    }

    fun onMaxItemsLimitChange(newValue: MaxItemsLimit) {
        uiState = uiState.copy(maxItemsLimit = newValue)
    }

    fun onItemSelect(item: Uri?) {
        if (item != null) {
            uiState = uiState.copy(selectedItems = listOf(item))
        }
    }

    fun onMultipleItemsSelect(items: List<Uri>) {
        uiState = uiState.copy(selectedItems = items)
    }
}