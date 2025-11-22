package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun ComponentImage(
    data: Uri,
    iconId: Int,
    crossfadeDuration: Int,
    context: Context,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(data)
            .crossfade(crossfadeDuration)
            .build()
    )
    when (painter.state) {
        is AsyncImagePainter.State.Error,
        is AsyncImagePainter.State.Empty -> {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                modifier = modifier,
                tint = Color.White
            )
        }
        else -> {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}