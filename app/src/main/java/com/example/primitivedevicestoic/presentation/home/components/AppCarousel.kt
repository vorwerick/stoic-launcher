package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.domain.model.AppInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.AppCarousel(
    selectedApps: List<AppInfo>,
    isMottoEnabled: Boolean,
    listMotto: String,
    currentMottos: List<String>,
    themeFg: Color,
    secondaryText: Color,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onAddAppClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        if (selectedApps.isEmpty()) {
            if (isMottoEnabled) {
                Text(
                    text = listMotto.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic
                    ),
                    color = themeFg.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.add_app).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = secondaryText,
                    modifier = Modifier
                        .clickable { onAddAppClick() }
                        .padding(16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val itemsWithMotto = remember(selectedApps, currentMottos, isMottoEnabled) {
                    val list = mutableListOf<Any>()
                    if (selectedApps.isNotEmpty()) {
                        selectedApps.forEachIndexed { index, app ->
                            list.add(app)
                            if (isMottoEnabled && index < currentMottos.size) {
                                list.add(currentMottos[index])
                            }
                        }
                    }
                    list
                }

                itemsWithMotto.forEach { item ->
                    if (item is AppInfo) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .combinedClickable(
                                    onClick = { onAppClick(item) },
                                    onLongClick = { onAppLongClick(item) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.label.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    letterSpacing = 3.sp,
                                    fontWeight = FontWeight.ExtraLight
                                ),
                                color = themeFg.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (item is String) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    letterSpacing = 3.sp,
                                    fontWeight = FontWeight.ExtraLight
                                ),
                                color = themeFg.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
