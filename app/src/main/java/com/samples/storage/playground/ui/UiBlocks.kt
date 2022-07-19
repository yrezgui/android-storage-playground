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

package com.samples.storage.playground.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavHostController
import com.samples.storage.playground.Routes
import com.samples.storage.playground.DeviceManager

@Composable
fun ScreenTitle(title: String) {
    Text(title, fontFamily = FontFamily.Serif)
}

@Composable
fun AndroidDetails(device: DeviceManager.DeviceInfo) {
    Text("Android Version ${device.versionName} (API ${device.sdkVersion})")
}

@Composable
fun SdkExtensionDetails(device: DeviceManager.DeviceInfo) {
    when (device.sdkExtensionApi30) {
        DeviceManager.SdkExtension.NotFound -> Text("SDK Extension (R) not found")
        is DeviceManager.SdkExtension.Version -> Text("SDK Extension (R) version ${device.sdkExtensionApi30.value}")
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        Routes.forEach {
            NavigationBarItem(
                icon = { Icon(it.icon, contentDescription = null) },
                label = { Text(it.label) },
                selected = navController.currentDestination?.route == it.route,
                onClick = {
                    navController.navigate(it.route)
                }
            )
        }
    }
}