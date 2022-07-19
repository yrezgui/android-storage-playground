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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VisualMediaType
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.samples.storage.playground.photopicker.PhotoPickerViewModel.PlatformMaxItemsLimit
import com.samples.storage.playground.ui.AndroidDetails
import com.samples.storage.playground.ui.BottomNavigationBar
import com.samples.storage.playground.ui.ScreenTitle
import com.samples.storage.playground.ui.SdkExtensionDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerScreen(
    navController: NavHostController,
    viewModel: PhotoPickerViewModel = viewModel()
) {
    val state = viewModel.uiState
    val device = state.deviceInfo

    val maxSelectableItems = if (state.chosenMaxItemsLimit == null) {
        Int.MAX_VALUE
    } else if (state.chosenMaxItemsLimit < 2) {
        2
    } else {
        state.chosenMaxItemsLimit
    }


    val selectItem = rememberLauncherForActivityResult(
        PickVisualMedia(),
        viewModel::onItemSelect
    )

    val selectItems = rememberLauncherForActivityResult(
        PickMultipleVisualMedia(maxSelectableItems),
        viewModel::onMultipleItemsSelect
    )

    fun launchPhotoPicker() {
        if (state.chosenMaxItemsLimit == 1) {
            selectItem.launch(PickVisualMediaRequest(state.fileTypeFilter))
        } else {
            selectItems.launch(PickVisualMediaRequest(state.fileTypeFilter))
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { ScreenTitle("Photo Picker") },
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
            FloatingActionButton(onClick = { launchPhotoPicker() }) {
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
                        chosenMaxItemsLimit = state.chosenMaxItemsLimit,
                        platformMaxItemsLimit = state.platformMaxItemsLimit,
                        onChange = viewModel::onMaxItemsLimitChange
                    )
                },
                trailingContent = {
                    MaxItemsLabel(
                        state.chosenMaxItemsLimit,
                        state.platformMaxItemsLimit
                    )
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

@Composable
fun FileTypeFilterInput(
    value: VisualMediaType,
    onClick: (filter: VisualMediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FileTypeFilterChip(
            filter = PickVisualMedia.ImageAndVideo,
            label = "Image & Video",
            value = value,
            onClick = onClick
        )
        FileTypeFilterChip(
            filter = PickVisualMedia.ImageOnly,
            label = "Image",
            value = value,
            onClick = onClick
        )
        FileTypeFilterChip(
            filter = PickVisualMedia.VideoOnly,
            label = "Video",
            value = value,
            onClick = onClick
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTypeFilterChip(
    filter: VisualMediaType,
    label: String,
    value: VisualMediaType,
    onClick: (filter: VisualMediaType) -> Unit
) {
    FilterChip(
        selected = filter == value,
        onClick = { onClick(filter) },
        label = { Text(label) },
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
    chosenMaxItemsLimit: Int?,
    platformMaxItemsLimit: PlatformMaxItemsLimit,
    onChange: (newValue: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (platformMaxItemsLimit is PlatformMaxItemsLimit.Limit) {
        Slider(
            value = chosenMaxItemsLimit!!.toFloat(),
            onValueChange = onChange,
            valueRange = 1f..platformMaxItemsLimit.value.toFloat(),
            modifier = modifier
                .padding(horizontal = 10.dp)
                .width(200.dp)
        )
    } else {
        Slider(
            enabled = false,
            value = 0f,
            onValueChange = onChange,
            valueRange = 1f..100f,
            modifier = modifier
                .padding(horizontal = 10.dp)
                .width(200.dp)
        )
    }
}

@Composable
fun MaxItemsLabel(chosenMaxItemsLimit: Int?, platformMaxItemsLimit: PlatformMaxItemsLimit) {
    val label = if (platformMaxItemsLimit is PlatformMaxItemsLimit.Limit) {
        chosenMaxItemsLimit.toString()
    } else {
        "∞"
    }

    Text(label)
}