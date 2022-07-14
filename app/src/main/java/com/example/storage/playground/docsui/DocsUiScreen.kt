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

package com.example.storage.playground.docsui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.storage.playground.GetContent
import com.example.storage.playground.GetMultipleContents
import com.example.storage.playground.docsui.DocsUiViewModel.DocsUiIntent
import com.example.storage.playground.docsui.DocsUiViewModel.FileType
import com.example.storage.playground.docsui.DocsUiViewModel.MaxItemsLimit
import com.example.storage.playground.ui.AndroidDetails
import com.example.storage.playground.ui.BottomNavigationBar
import com.example.storage.playground.ui.ScreenTitle
import com.example.storage.playground.ui.SdkExtensionDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocsUiScreen(navController: NavHostController, viewModel: DocsUiViewModel = viewModel()) {
    val state = viewModel.uiState
    val device = state.deviceInfo

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
        viewModel::onItemSelect
    )

    val openMultipleDocuments = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
        viewModel::onMultipleItemsSelect
    )

    val getContent = rememberLauncherForActivityResult(
        GetContent(),
        viewModel::onItemSelect
    )

    val getMultipleContents = rememberLauncherForActivityResult(
        GetMultipleContents(),
        viewModel::onMultipleItemsSelect
    )

    fun launchDocsUiPicker() {
        when (state.docsUiIntent) {
            DocsUiIntent.GET_CONTENT -> {
                when (state.maxItemsLimit) {
                    MaxItemsLimit.Single -> getContent.launch(state.mimeTypeFilter)
                    MaxItemsLimit.Unlimited -> getMultipleContents.launch(state.mimeTypeFilter)
                }
            }
            DocsUiIntent.OPEN_DOCUMENT -> {
                when (state.maxItemsLimit) {
                    MaxItemsLimit.Single -> openDocument.launch(state.mimeTypeFilter)
                    MaxItemsLimit.Unlimited -> openMultipleDocuments.launch(state.mimeTypeFilter)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { ScreenTitle("Docs UI") },
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
            FloatingActionButton(onClick = { launchDocsUiPicker() }) {
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
                headlineText = { Text("DocsUI Intent") },
                supportingText = {
                    DocsUiIntentInput(
                        state.docsUiIntent,
                        viewModel::onDocsUiIntentChange
                    )
                },
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
                headlineText = { Text("Multiple items") },
                trailingContent = {
                    MultipleItemsSwitch(
                        state.maxItemsLimit,
                        viewModel::onMaxItemsLimitChange
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
                        error = rememberVectorPainter(Icons.Outlined.Description),
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
fun DocsUiIntentInput(
    value: DocsUiIntent,
    onClick: (intent: DocsUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DocsUiIntentChip(
            intent = DocsUiIntent.GET_CONTENT,
            value = value,
            onClick = onClick
        )
        DocsUiIntentChip(
            intent = DocsUiIntent.OPEN_DOCUMENT,
            value = value,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocsUiIntentChip(
    intent: DocsUiIntent,
    value: DocsUiIntent,
    onClick: (filter: DocsUiIntent) -> Unit
) {
    FilterChip(
        selected = intent == value,
        onClick = { onClick(intent) },
        label = { Text(intent.toString()) },
        selectedIcon = {
            if (intent == value) {
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
fun FileTypeFilterInput(
    value: FileType,
    onClick: (filter: FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FileTypeFilterChip(
            filter = FileType.All,
            value = value,
            onClick = onClick
        )
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
fun MultipleItemsSwitch(value: MaxItemsLimit, onChange: (limit: MaxItemsLimit) -> Unit) {
    val checked = value == MaxItemsLimit.Unlimited
    val icon: (@Composable () -> Unit)? = if (checked) {
        {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }

    Switch(
        checked = checked,
        thumbContent = icon,
        onCheckedChange = { newValue ->
            onChange(if (newValue) MaxItemsLimit.Unlimited else MaxItemsLimit.Single)
        }
    )
}