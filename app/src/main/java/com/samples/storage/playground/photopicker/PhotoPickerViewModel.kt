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

package com.samples.storage.playground.photopicker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VisualMediaType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.samples.storage.playground.DeviceManager
import com.samples.storage.playground.DeviceManager.getDeviceInfo

class PhotoPickerViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context
        get() = getApplication()

    sealed interface PlatformMaxItemsLimit {
        @JvmInline
        value class Limit(val value: Int) : PlatformMaxItemsLimit

        object NoLimit : PlatformMaxItemsLimit
    }

    data class UiState(
        val deviceInfo: DeviceManager.DeviceInfo,
        val fileTypeFilter: VisualMediaType = PickVisualMedia.ImageAndVideo,
        val platformMaxItemsLimit: PlatformMaxItemsLimit,
        val isMultiSelectEnabled: Boolean = true,
        val chosenMaxItemsLimit: Int?,
        val selectedItems: List<Uri> = emptyList()
    )

    var uiState by mutableStateOf(initializeState())
        private set

    private fun initializeState(): UiState {
        val platformMaxItemsLimit = getPlatformMaxItems()
        val maxItemsLimit = if (platformMaxItemsLimit is PlatformMaxItemsLimit.Limit) {
            platformMaxItemsLimit.value
        } else {
            null
        }

        return UiState(
            deviceInfo = getDeviceInfo(context),
            platformMaxItemsLimit = getPlatformMaxItems(),
            chosenMaxItemsLimit = maxItemsLimit
        )
    }

    fun reset() {
        uiState = initializeState()
    }

    @SuppressLint("NewApi")
    private fun getPlatformMaxItems(): PlatformMaxItemsLimit {
        // If the photo picker is available on the device, getPickImagesMaxLimit is there but not
        // always visible on the SDK (only from Android 13+)
        return if (PickVisualMedia.isPhotoPickerAvailable()) {
            PlatformMaxItemsLimit.Limit(MediaStore.getPickImagesMaxLimit())
        } else {
            PlatformMaxItemsLimit.NoLimit
        }
    }

    fun onMultiSelectChange(newValue: Boolean) {
        uiState = uiState.copy(isMultiSelectEnabled = newValue)
    }

    fun onMaxItemsLimitChange(newValue: Float) {
        if (uiState.platformMaxItemsLimit is PlatformMaxItemsLimit.Limit) {
            uiState = uiState.copy(chosenMaxItemsLimit = newValue.toInt())
        }
    }

    fun onFileTypeFilterChange(newValue: VisualMediaType) {
        uiState = uiState.copy(fileTypeFilter = newValue)
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