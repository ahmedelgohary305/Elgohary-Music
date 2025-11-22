package com.example.elgoharymusic.presentation.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elgoharymusic.R
import kotlinx.coroutines.delay

@Composable
fun RowScope.SearchField(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchDeactivated: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(0.7f),
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .padding(horizontal = 18.dp),
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                letterSpacing = 0.1.sp
            ),
            singleLine = true,
            maxLines = 1,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Clean search icon
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text field container
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_songs),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }

                    // Minimal clear button
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150))
                    ) {
                        IconButton(
                            onClick = { onSearchQueryChanged("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Minimal close button
    IconButton(
        onClick = onSearchDeactivated,
        modifier = Modifier
            .size(48.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = "Close search",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}