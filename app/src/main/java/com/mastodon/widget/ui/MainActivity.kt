package com.mastodon.widget.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.service.FeedUpdateService
import com.mastodon.widget.service.StreamingService
import com.mastodon.widget.ui.screen.*
import com.mastodon.widget.ui.theme.MastodonWidgetTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class BottomTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : BottomTab("Home", Icons.Filled.Home, Icons.Filled.Home)
    object Explore : BottomTab("Explore", Icons.Filled.Explore, Icons.Filled.Explore)
    object Notifications : BottomTab("Alerts", Icons.Filled.NotificationsNone, Icons.Filled.Notifications)
    object Profile : BottomTab("Profile", Icons.Filled.PersonOutline, Icons.Filled.Person)
}

private sealed interface ScreenState {
    object Main : ScreenState
    object Post : ScreenState
    data class Profile(val accountId: String) : ScreenState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val prefs = PreferenceManager(this@MainActivity)
            if (!(prefs.isLoggedIn.firstOrNull() ?: false)) {
                startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                finish()
                return@launch
            }
            StreamingService.start(this@MainActivity)
            FeedUpdateService.scheduleFeedSync(this@MainActivity)
        }

        setContent {
            MastodonWidgetTheme {
                val tabs = listOf(
                    BottomTab.Home,
                    BottomTab.Explore,
                    BottomTab.Notifications,
                    BottomTab.Profile
                )
                var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }
                var showPostScreen by remember { mutableStateOf(false) }
                val profileStack = remember { mutableStateListOf<String>() }
                val currentProfileId = profileStack.lastOrNull()

                // Hardware/gesture back press handling
                BackHandler(enabled = showPostScreen) {
                    showPostScreen = false
                }
                BackHandler(enabled = !showPostScreen && profileStack.isNotEmpty()) {
                    profileStack.removeAt(profileStack.lastIndex)
                }

                val currentScreenState: ScreenState = when {
                    showPostScreen -> ScreenState.Post
                    currentProfileId != null -> ScreenState.Profile(currentProfileId)
                    else -> ScreenState.Main
                }

                AnimatedContent(
                    targetState = currentScreenState,
                    transitionSpec = {
                        when {
                            targetState is ScreenState.Post || initialState is ScreenState.Post -> {
                                if (targetState is ScreenState.Post) {
                                    slideInVertically { it } + fadeIn() togetherWith fadeOut()
                                } else {
                                    fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                }
                            }
                            targetState is ScreenState.Profile || initialState is ScreenState.Profile -> {
                                if (targetState is ScreenState.Profile && initialState !is ScreenState.Profile) {
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it / 3 } + fadeOut()
                                } else if (targetState !is ScreenState.Profile && initialState is ScreenState.Profile) {
                                    slideInHorizontally { -it / 3 } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                                } else {
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                }
                            }
                            else -> fadeIn() togetherWith fadeOut()
                        }
                    },
                    label = "screenTransition"
                ) { screenState ->
                    when (screenState) {
                        is ScreenState.Post -> {
                            PostScreen(onNavigateBack = { showPostScreen = false })
                        }
                        is ScreenState.Profile -> {
                            ProfileScreen(
                                accountId = screenState.accountId,
                                onNavigateBack = {
                                    if (profileStack.isNotEmpty()) {
                                        profileStack.removeAt(profileStack.lastIndex)
                                    }
                                },
                                onAccountClick = { nextAccountId ->
                                    profileStack.add(nextAccountId)
                                },
                                onLogout = {
                                    startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                                    finish()
                                }
                            )
                        }
                        is ScreenState.Main -> {
                            Scaffold(
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 0.dp
                                    ) {
                                        tabs.forEach { tab ->
                                            val selected = selectedTab == tab
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { selectedTab = tab },
                                                icon = {
                                                    Icon(
                                                        if (selected) tab.selectedIcon else tab.icon,
                                                        contentDescription = tab.label
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        tab.label,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            ) { paddingValues ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                ) {
                                    when (selectedTab) {
                                        BottomTab.Home -> FeedScreen(
                                            onPostClick = { showPostScreen = true },
                                            onAccountClick = { accountId -> profileStack.add(accountId) }
                                        )
                                        BottomTab.Explore -> ExploreScreen(
                                            onAccountClick = { accountId -> profileStack.add(accountId) }
                                        )
                                        BottomTab.Notifications -> NotificationsScreen()
                                        BottomTab.Profile -> ProfileScreen(
                                            accountId = null,
                                            onAccountClick = { accountId -> profileStack.add(accountId) },
                                            onLogout = {
                                                startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                                                finish()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
