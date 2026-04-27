package com.mastodon.widget.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mastodon.widget.ui.viewmodel.PostViewModel

private data class VisibilityOption(val key: String, val label: String, val icon: ImageVector)

private val visibilityOptions = listOf(
    VisibilityOption("public",   "Public",    Icons.Filled.Public),
    VisibilityOption("unlisted", "Unlisted",  Icons.Outlined.VisibilityOff),
    VisibilityOption("private",  "Followers", Icons.Filled.Lock),
    VisibilityOption("direct",   "Direct",    Icons.Filled.Mail)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: PostViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var spoilerText by remember { mutableStateOf("") }
    var showCw by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf("public") }
    var showVisibilityMenu by remember { mutableStateOf(false) }

    val isPosting by viewModel.isPosting.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    LaunchedEffect(success) { if (success) onNavigateBack() }

    val charCount = content.length
    val charRemaining = 500 - charCount
    val overLimit = charCount > 500
    val nearLimit = charCount > 450
    val currentVisibility = visibilityOptions.find { it.key == visibility } ?: visibilityOptions[0]

    val charColor = when {
        overLimit  -> MaterialTheme.colorScheme.error
        nearLimit  -> MaterialTheme.colorScheme.tertiary
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, "Cancel")
                    }
                },
                actions = {
                    AnimatedVisibility(visible = nearLimit || overLimit) {
                        Text(
                            "$charRemaining",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = charColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.postStatus(
                                content = content,
                                visibility = visibility,
                                spoilerText = spoilerText.takeIf { showCw && it.isNotBlank() }
                            )
                        },
                        enabled = !isPosting && content.isNotBlank() && !overLimit,
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Post", fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content warning field
            AnimatedVisibility(
                visible = showCw,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = spoilerText,
                    onValueChange = { spoilerText = it },
                    label = { Text("Content warning") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    )
                )
            }

            // Main text area
            TextField(
                value = content,
                onValueChange = { if (it.length <= 520) content = it },
                placeholder = {
                    Text(
                        "What's on your mind?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                )
            )

            // Error message
            AnimatedVisibility(visible = error != null) {
                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Bottom toolbar
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CW toggle
                    IconButton(onClick = { showCw = !showCw }) {
                        Icon(
                            Icons.Filled.Warning,
                            "Content warning",
                            tint = if (showCw) MaterialTheme.colorScheme.tertiary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Visibility picker
                    Box {
                        FilledTonalIconButton(
                            onClick = { showVisibilityMenu = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(currentVisibility.icon, "Visibility: ${currentVisibility.label}")
                        }
                        DropdownMenu(
                            expanded = showVisibilityMenu,
                            onDismissRequest = { showVisibilityMenu = false }
                        ) {
                            visibilityOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option.label,
                                            fontWeight = if (option.key == visibility) FontWeight.Bold
                                                         else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            option.icon, null,
                                            tint = if (option.key == visibility) MaterialTheme.colorScheme.primary
                                                   else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    trailingIcon = {
                                        if (option.key == visibility) {
                                            Icon(
                                                Icons.Filled.Check, null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        visibility = option.key
                                        showVisibilityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Character arc indicator
                    val fraction = (charCount / 500f).coerceIn(0f, 1f)
                    CircularProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = charColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
