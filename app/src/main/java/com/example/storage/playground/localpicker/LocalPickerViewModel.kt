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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.storage.playground.DeviceManager
import com.example.storage.playground.DeviceManager.Companion.getDeviceInfo
import com.example.storage.playground.localpicker.MediaRepository.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class LocalPickerViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val PICKER_MAX_ITEMS_LIMIT = 100
    }

    private val context: Context
        get() = getApplication()

    private val mediaRepository = MediaRepository(context)
    val requiredPermissions = mediaRepository.requiredPermissions.toTypedArray()

    data class UiState(
        val deviceInfo: DeviceManager.DeviceInfo = getDeviceInfo(),
        val fileTypeFilter: FileType = FileType.ImageAndVideo,
        val maxItemsLimit: Int = PICKER_MAX_ITEMS_LIMIT,
        val localMediaUris: List<Uri> = emptyList(),
        val selectedItems: List<Uri> = emptyList()
    )

    var uiState by mutableStateOf(UiState())
        private set

    fun reset() {
        uiState = UiState()
    }

    fun hasStorageAccess(): Boolean {
        return mediaRepository.hasStorageAccess()
    }

    fun createSettingsIntent(): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", context.packageName, null)
        }

        return intent
    }

    fun onMaxItemsLimitChange(newValue: Float) {
        uiState = uiState.copy(maxItemsLimit = newValue.toInt())
    }

    fun onFileTypeFilterChange(newValue: FileType) {
        uiState = uiState.copy(fileTypeFilter = newValue)
    }

    fun onMultipleItemsSelect(items: List<Uri>) {
        uiState = uiState.copy(selectedItems = items, localMediaUris = emptyList())
    }

    fun loadLocalPickerPictures() {
        viewModelScope.launch(Dispatchers.IO) {
            val localMediaUris =
                mediaRepository.fetchVisualMediaUris(uiState.fileTypeFilter).toList()
            uiState = uiState.copy(selectedItems = emptyList(), localMediaUris = localMediaUris)
        }
    }
}