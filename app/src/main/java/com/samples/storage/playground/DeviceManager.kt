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

package com.samples.storage.playground

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.os.ext.SdkExtensions

object DeviceManager {
    private const val MEDIA_PROVIDER_ID = "com.android.providers.media"

    data class DeviceInfo(
        val versionName: String,
        val sdkVersion: Int,
        val sdkExtensionApi30: SdkExtension,
        val mediaProviderVersion: String,
    )

    sealed interface SdkExtension {
        object NotFound : SdkExtension
        class Version(val value: Int) : SdkExtension
    }

    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            versionName = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            sdkExtensionApi30 = getSdkExtensionApi30(),
            mediaProviderVersion = getMediaProviderVersion(context)
        )
    }

    @SuppressLint("NewApi")
    private fun getSdkExtensionApi30(): SdkExtension {
        // SdkExtensions has been on Android R but their visibility is hidden
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SdkExtension.Version(SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R))
        } else {
            SdkExtension.NotFound
        }
    }

    private fun getMediaProviderVersion(context: Context): String {
        @Suppress("DEPRECATION")
        val mediaProviderInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(MEDIA_PROVIDER_ID, PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(MEDIA_PROVIDER_ID, 0)
        }

        return mediaProviderInfo.versionName
    }
}