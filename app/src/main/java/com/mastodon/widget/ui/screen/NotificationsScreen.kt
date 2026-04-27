package com.mastodon.widget.ui.screen

import android.text.Html
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mastodon.widget.api.model.Notification
import com.mastodon.widget.ui.theme.FavouriteRed
import com.mastodon.widget.ui.theme.MastodonPurple
import com.mastodon.widget.ui.theme.ReblogGreen
import com.mastodon.widget.ui.viewmodel.NotificationsViewModel

private data class NotifMeta(val icon: ImageVector, val tint: Color, val label: String)

private fun notifMeta(type: String) = when (type) {
    "follow"         -> NotifMeta(Icons.Filled.PersonAdd, Color(0xFF6364FF), "followed you")
    "favourite"      -> NotifMeta(Icons.Filled.Favorite, FavouriteRed, "favourited your post")
    "reblog"         -> NotifMeta(Icons.Filled.Repeat, ReblogGreen, "boosted your post")
    "mention"        -> NotifMeta(Icons.Filled.AlternateEmail, MastodonPurple, "mentioned you")
    "poll"           -> NotifMeta(Icons.Filled.Poll, Color(0xFFFFA500), "poll ended")
    "follow_request" -> NotifMeta(Icons.Filled.PersonAdd, Color(0xFFFFA500), "wants to follow you")
    else             -> NotifMeta(Icons.Filled.Notifications, MastodonPurple, type)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel = viewModel()) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.load() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                isLoading && notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null && notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.load() }) { Text("Retry") }
                        }
                    }
                }
                notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Notifications, null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No notifications yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            NotificationItem(notif)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notif: Notification) {
    val meta = notifMeta(notif.type)

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    notif.account.displayName.takeIf { it.isNotBlank() } ?: notif.account.username,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    " ${meta.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        supportingContent = notif.status?.let { status ->
            val preview = Html.fromHtml(status.content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
            if (preview.isNotBlank()) {
                {
                    Text(
                        preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            } else null
        },
        leadingContent = {
            Box(modifier = Modifier.size(48.dp)) {
                AsyncImage(
                    model = notif.account.avatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                Surface(
                    color = meta.tint,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        meta.icon, null,
                        tint = Color.White,
                        modifier = Modifier.padding(3.dp)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 72.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}
