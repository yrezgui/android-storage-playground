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

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPickerGrid(
    modifier: Modifier = Modifier,
    entries: List<Uri>,
    limit: Int,
    onSelect: (uris: List<Uri>) -> Unit,
) {
    var selectedItems by remember { mutableStateOf(emptySet<Uri>()) }

    fun onClick(uri: Uri) {
        if (selectedItems.contains(uri)) {
            selectedItems = selectedItems - uri
        } else if (selectedItems.size < limit) {
            selectedItems = selectedItems + uri
        }
    }

    fun onSubmit() {
        onSelect(selectedItems.toList())
        selectedItems = emptySet()
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = modifier.weight(1f),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(entries) { uri ->
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clickable { onClick(uri) }) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    if (selectedItems.contains(uri)) {
                        FilledIconButton(
                            onClick = { onClick(uri) },
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected file",
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    } else {
                        OutlinedIconButton(
                            onClick = { onClick(uri) },
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            border = BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.surface.copy(0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.Transparent
                            )
                        }
                    }
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { onSubmit() }) {
                if (selectedItems.isEmpty()) {
                    Text("Add")
                } else {
                    Text("Add (${selectedItems.size})")
                }
            }
        }
    }
}