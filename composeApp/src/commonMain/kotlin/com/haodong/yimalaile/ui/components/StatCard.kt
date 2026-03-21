package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCardNew(
    title: String,
    value: String,
    unit: String,
    backgroundColor: Color,
    textColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(shape)
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = TextStyle(
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = TextStyle(
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}
