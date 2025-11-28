package com.example.linkedinapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.R

/**
 * Telegram-style Top Bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onBackClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            actions()
        }
    }
}

/**
 * Telegram-style Input Field
 */
@Composable
fun TelegramTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 15.sp
            )
        },
        singleLine = singleLine,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
    )
}

/**
 * Telegram-style Button
 */
@Composable
fun TelegramButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Telegram-style Card
 */
@Composable
fun TelegramCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Telegram-style List Item
 */
@Composable
fun TelegramListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        
        if (leadingIcon != null) {
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = it,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        trailing?.invoke()
    }
}

/**
 * Telegram-style Divider
 */
@Composable
fun TelegramDivider(
    modifier: Modifier = Modifier,
    startIndent: androidx.compose.ui.unit.Dp = 0.dp
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startIndent),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        thickness = 0.5.dp
    )
}

