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
import android.os.Build
import android.os.ext.SdkExtensions

class DeviceManager {
    data class DeviceInfo(
        val versionName: String,
        val sdkVersion: Int,
        val sdkExtensionApi30: SdkExtension
    )

    sealed interface SdkExtension {
        object NotFound : SdkExtension
        class Version(val value: Int) : SdkExtension
    }

    companion object {
        @SuppressLint("NewApi")
        private fun getSdkExtensionApi30(): SdkExtension {
            // SdkExtensions has been on Android R but their visibility is hidden
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                SdkExtension.Version(SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R))
            } else {
                SdkExtension.NotFound
            }
        }

        fun getDeviceInfo(): DeviceInfo {
            return DeviceInfo(
                versionName = Build.VERSION.RELEASE,
                sdkVersion = Build.VERSION.SDK_INT,
                sdkExtensionApi30 = getSdkExtensionApi30()
            )
        }
    }
}