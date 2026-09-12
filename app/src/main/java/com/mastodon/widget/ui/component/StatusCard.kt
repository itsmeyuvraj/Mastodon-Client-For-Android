package com.mastodon.widget.ui.component

import android.text.Html
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mastodon.widget.api.model.Status
import com.mastodon.widget.ui.theme.FavouriteRed
import com.mastodon.widget.ui.theme.MastodonPurple
import com.mastodon.widget.ui.theme.ReblogGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusCard(
    status: Status,
    onFavouriteClick: () -> Unit,
    onReblogClick: () -> Unit,
    onAccountClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val displayStatus = status.reblog ?: status
    val account = displayStatus.account
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // Reblog banner
            if (status.reblog != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable(
                            enabled = status.account != null && onAccountClick != null,
                            onClick = { status.account?.id?.let { onAccountClick?.invoke(it) } }
                        )
                ) {
                    Icon(
                        Icons.Filled.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "${status.account?.displayName?.takeIf { it.isNotBlank() } ?: status.account?.username ?: ""} boosted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.small)
                        .clickable(
                            enabled = account != null && onAccountClick != null,
                            onClick = { account?.id?.let { onAccountClick?.invoke(it) } }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MastodonPurple.copy(alpha = 0.3f), MaterialTheme.colorScheme.primaryContainer)
                                )
                            )
                    ) {
                        AsyncImage(
                            model = account?.avatar,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = account?.displayName?.takeIf { it.isNotBlank() } ?: account?.username ?: "Unknown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@${account?.acct ?: account?.username ?: "unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatTime(displayStatus.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Content warning chip
            if (!displayStatus.spoilerText.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CW: ${displayStatus.spoilerText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Post body
            val plainText = Html.fromHtml(displayStatus.content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
            if (plainText.isNotBlank()) {
                Text(
                    text = plainText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 6,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
                )
                if (!expanded && plainText.length > 300) {
                    TextButton(
                        onClick = { expanded = true },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Show more",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Media grid
            val media = displayStatus.media_attachments.take(4)
            if (media.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                val gridHeight = if (media.size == 1) 200.dp else 150.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                        .clip(MaterialTheme.shapes.medium),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    media.forEach { attachment ->
                        AsyncImage(
                            model = attachment.previewUrl.takeIf { it.isNotBlank() } ?: attachment.url,
                            contentDescription = attachment.description,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(Modifier.height(4.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reply (non-interactive)
                ActionButton(
                    icon = { Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(18.dp)) },
                    count = displayStatus.repliesCount,
                    activeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    isActive = false
                )

                // Boost
                val boostColor by animateColorAsState(
                    targetValue = if (displayStatus.reblogged) ReblogGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "boostColor"
                )
                ActionButton(
                    icon = {
                        Icon(
                            if (displayStatus.reblogged) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    count = displayStatus.reblogsCount,
                    activeColor = boostColor,
                    isActive = displayStatus.reblogged,
                    onClick = onReblogClick
                )

                // Favourite
                val favColor by animateColorAsState(
                    targetValue = if (displayStatus.favourited) FavouriteRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "favColor"
                )
                ActionButton(
                    icon = {
                        Icon(
                            if (displayStatus.favourited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    count = displayStatus.favouritesCount,
                    activeColor = favColor,
                    isActive = displayStatus.favourited,
                    onClick = onFavouriteClick
                )

                // Spacer to balance layout
                Spacer(Modifier.width(40.dp))
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: @Composable () -> Unit,
    count: Int,
    activeColor: Color,
    isActive: Boolean,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        if (onClick != null) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(contentColor = activeColor)
            ) {
                icon()
            }
        } else {
            CompositionLocalProvider(LocalContentColor provides activeColor) {
                Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { icon() }
            }
        }
        if (count > 0) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = activeColor,
                modifier = Modifier.padding(start = 1.dp)
            )
        }
    }
}

private fun formatTime(iso: String): String {
    if (iso.isBlank()) return ""
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
    )
    return try {
        val date = formats.firstNotNullOfOrNull { runCatching { it.parse(iso) }.getOrNull() } ?: return ""
        val diffSec = (Date().time - date.time) / 1000
        when {
            diffSec < 60 -> "${diffSec}s"
            diffSec < 3600 -> "${diffSec / 60}m"
            diffSec < 86400 -> "${diffSec / 3600}h"
            diffSec < 86400 * 7 -> "${diffSec / 86400}d"
            else -> SimpleDateFormat("MMM d", Locale.US).format(date)
        }
    } catch (_: Exception) { "" }
}
