package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.domain.model.AppInfo
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverlay(
    searchQuery: String,
    apps: ImmutableList<AppInfo>,
    selectedPackageNames: Set<String>,
    themeBg: Color,
    themeFg: Color,
    subtleColor: Color,
    secondaryText: Color,
    hintText: Color,
    isSystemBarHidden: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onToggleAppSelection: (AppInfo) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = themeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSystemBarHidden) {
                        Modifier.padding(top = 16.dp)
                    } else {
                        Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    }
                )
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { focusManager.clearFocus() },
                active = true,
                onActiveChange = { if (!it) onCloseSearch() },
                placeholder = {
                    Text(
                        stringResource(R.string.what_looking_for),
                        color = hintText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onCloseSearch) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = secondaryText
                        )
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = secondaryText
                            )
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = themeBg,
                    dividerColor = subtleColor,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = themeFg,
                        unfocusedTextColor = themeFg,
                        cursorColor = themeFg,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isSystemBarHidden) 16.dp else 0.dp)
                    .focusRequester(focusRequester)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = apps,
                        key = { app -> app.packageName }
                    ) { app ->
                        val isSelected = selectedPackageNames.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppClick(app) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
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

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}
