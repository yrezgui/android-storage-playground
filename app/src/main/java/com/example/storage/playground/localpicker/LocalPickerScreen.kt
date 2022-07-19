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

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.storage.playground.localpicker.LocalPickerViewModel.Companion.PICKER_MAX_ITEMS_LIMIT
import com.example.storage.playground.localpicker.MediaRepository.FileType
import com.example.storage.playground.ui.AndroidDetails
import com.example.storage.playground.ui.BottomNavigationBar
import com.example.storage.playground.ui.ScreenTitle
import com.example.storage.playground.ui.SdkExtensionDetails
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LocalPickerScreen(
    navController: NavHostController,
    viewModel: LocalPickerViewModel = viewModel()
) {
    val state = viewModel.uiState
    val device = state.deviceInfo
    var showStorageRationaleModal by remember { mutableStateOf(false) }
    var showDeniedStoragePermissionModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val activity = (LocalView.current.context as Activity)
    val internalPhotoPickerState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    fun launchLocalPicker() {
        viewModel.loadLocalPickerPictures()
        coroutineScope.launch {
            internalPhotoPickerState.animateTo(ModalBottomSheetValue.Expanded)
        }
    }

    val requestPermissions = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
        val permission = viewModel.requiredPermissions.first()

        if (viewModel.hasStorageAccess()) {
            launchLocalPicker()
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showDeniedStoragePermissionModal = true
            }
        }
    }

    fun canLaunchLocalPicker() {
        val permission = viewModel.requiredPermissions.first()

        if (viewModel.hasStorageAccess()) {
            launchLocalPicker()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showStorageRationaleModal = true
        } else {
            requestPermissions.launch(viewModel.requiredPermissions)
        }
    }

    fun onPickerSelection(uris: List<Uri>) {
        coroutineScope.launch {
            internalPhotoPickerState.hide()
            viewModel.onMultipleItemsSelect(uris)
        }
    }

    if (showStorageRationaleModal) {
        StoragePermissionExplanationDialog(
            onConfirm = {
                showStorageRationaleModal = false
                requestPermissions.launch(viewModel.requiredPermissions)
            },
            onDismiss = { showStorageRationaleModal = false }
        )
    }

    if (showDeniedStoragePermissionModal) {
        DeniedStoragePermissionSettingsDialog(
            onConfirm = {
                showDeniedStoragePermissionModal = false
                activity.startActivity(viewModel.createSettingsIntent())
            },
            onDismiss = { showDeniedStoragePermissionModal = false }
        )
    }

    ModalBottomSheetLayout(
        sheetState = internalPhotoPickerState,
        sheetContent = {
            LocalPickerGrid(
                modifier = Modifier.fillMaxSize(),
                entries = state.localMediaUris,
                limit = state.maxItemsLimit,
                onSelect = ::onPickerSelection
            )
        }
    ) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { ScreenTitle("Local Picker") },
                    actions = {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Reset picker configuration"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(navController)
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { canLaunchLocalPicker() }) {
                    Icon(Icons.Filled.Add, "Select from gallery")
                }
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
            ) {
                ListItem(
                    headlineText = { AndroidDetails(device) },
                    supportingText = { SdkExtensionDetails(device) },
                )
                Divider()
                ListItem(
                    headlineText = { Text("File type filter") },
                    supportingText = {
                        FileTypeFilterInput(
                            state.fileTypeFilter,
                            viewModel::onFileTypeFilterChange
                        )
                    },
                )
                Divider()
                ListItem(
                    headlineText = { Text("Max item(s)") },
                    supportingText = {
                        MaxSelectableItemsSlider(
                            maxItemsLimit = state.maxItemsLimit,
                            onChange = viewModel::onMaxItemsLimitChange
                        )
                    },
                    trailingContent = {
                        Text(state.maxItemsLimit.toString())
                    }
                )
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(state.selectedItems) {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileTypeFilterInput(
    value: FileType,
    onClick: (filter: FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FileTypeFilterChip(
            filter = FileType.ImageAndVideo,
            value = value,
            onClick = onClick
        )
        FileTypeFilterChip(
            filter = FileType.Image,
            value = value,
            onClick = onClick
        )
        FileTypeFilterChip(
            filter = FileType.Video,
            value = value,
            onClick = onClick
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTypeFilterChip(
    filter: FileType,
    value: FileType,
    onClick: (filter: FileType) -> Unit
) {
    FilterChip(
        selected = filter == value,
        onClick = { onClick(filter) },
        label = { Text(filter.toString()) },
        selectedIcon = {
            if (filter == value) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Filter selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        }
    )
}

@Composable
fun MaxSelectableItemsSlider(
    maxItemsLimit: Int?,
    onChange: (newValue: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = maxItemsLimit!!.toFloat(),
        onValueChange = onChange,
        valueRange = 1f..PICKER_MAX_ITEMS_LIMIT.toFloat(),
        modifier = modifier
            .padding(horizontal = 10.dp)
            .width(200.dp)
    )
}

@Composable
fun StoragePermissionExplanationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage access") },
        text = { Text("This playground would like access to your media files to be able select them in the local picker") },
        icon = {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceTint
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun DeniedStoragePermissionSettingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage access") },
        text = { Text("This playground would like access to your media files to be able select them in the local picker") },
        icon = {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceTint
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}