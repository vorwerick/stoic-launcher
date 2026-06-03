package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.domain.model.AppInfo

@Composable
fun SearchOverlay(
    searchQuery: String,
    apps: List<AppInfo>,
    selectedPackageNames: Set<String>,
    themeBg: Color,
    themeFg: Color,
    subtleColor: Color,
    secondaryText: Color,
    hintText: Color,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onToggleAppSelection: (AppInfo) -> Unit,
    removeDiacritics: String.() -> String
) {
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = themeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            stringResource(R.string.what_looking_for),
                            color = hintText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onCloseSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = secondaryText
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = themeFg,
                        unfocusedTextColor = themeFg,
                        cursorColor = themeFg,
                        focusedIndicatorColor = subtleColor,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }

            val filteredApps by remember(apps, searchQuery) {
                derivedStateOf {
                    val normalizedQuery = searchQuery.removeDiacritics()
                    apps.filter {
                        it.label.removeDiacritics().contains(normalizedQuery, ignoreCase = true)
                    }
                        .sortedBy { it.label.lowercase() }
                }
            }

            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                items(
                    items = filteredApps,
                    key = { app -> "${app.packageName}_${app.label}" }
                ) { app ->
                    val isSelected = selectedPackageNames.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppClick(app) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                            color = themeFg.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onToggleAppSelection(app) }) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (isSelected) "Remove from home" else "Add to home",
                                tint = hintText
                            )
                        }
                    }
                }
            }
        }
    }
}
