package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primitivedevicestoic.R

@Composable
fun HomeHeader(
    currentDate: String,
    currentTime: String,
    daysAlive: Long?,
    timeUntilSleep: String?,
    themeFg: Color,
    secondaryText: Color,
    onDateClick: () -> Unit = {},
    onTimeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Levá strana: Datum a Dny naživu
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.clickable { onDateClick() }
        ) {
            Text(
                text = currentDate.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                color = secondaryText,
                textAlign = TextAlign.Start
            )
            daysAlive?.let {
                val daysUnit = pluralStringResource(
                    R.plurals.days_unit,
                    it.toInt(),
                    it.toInt()
                )
                Text(
                    text = stringResource(R.string.days_alive_label).lowercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    color = secondaryText,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "$it $daysUnit",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = themeFg,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }
        }

        // Pravá strana: Čas a Do spánku
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable { onTimeClick() }
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                color = secondaryText,
                textAlign = TextAlign.End
            )
            timeUntilSleep?.let {
                Text(
                    text = stringResource(R.string.until_sleep_label).lowercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    color = secondaryText,
                    textAlign = TextAlign.End
                )
                Text(
                    text = it,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = themeFg,
                    textAlign = TextAlign.End,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }
        }
    }
}
