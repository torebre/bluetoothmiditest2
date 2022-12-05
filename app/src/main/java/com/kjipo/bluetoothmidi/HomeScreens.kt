package com.kjipo.bluetoothmidi

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun HomeRoute() {
    Text(
        text = "Home screen",
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        style = MaterialTheme.typography.h6,
        color = Color.Black
    )
}