package com.mastodon.widget.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mastodon.widget.api.ApiClient
import com.mastodon.widget.api.model.CreateAppRequest
import com.mastodon.widget.data.PreferenceManager
import com.mastodon.widget.ui.theme.MastodonWidgetTheme
import kotlinx.coroutines.launch

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MastodonWidgetTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AuthScreen(
                        onAuthSuccess = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

private const val REDIRECT_URI = "mastodonwidget://oauth/callback"
private const val SCOPES = "read write follow push"

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()

    var serverUrlInput by remember { mutableStateOf("mastodon.social") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var oauthUrl by remember { mutableStateOf<String?>(null) }

    if (oauthUrl != null) {
        OAuthWebView(
            url = oauthUrl!!,
            onCodeReceived = { code ->
                oauthUrl = null
                isLoading = true
                scope.launch {
                    try {
                        val serverUrl = preferenceManager.getServerUrl()
                        val clientId = preferenceManager.getClientId() ?: error("No client_id")
                        val clientSecret = preferenceManager.getClientSecret() ?: error("No client_secret")

                        ApiClient.setBaseUrl("$serverUrl/api/v1/")
                        val api = ApiClient.getInstance()
                        val token = api.getAccessToken(
                            url = "$serverUrl/oauth/token",
                            clientId = clientId,
                            clientSecret = clientSecret,
                            redirectUri = REDIRECT_URI,
                            grantType = "authorization_code",
                            code = code,
                            scope = SCOPES
                        )

                        preferenceManager.setAccessToken(token.access_token)
                        preferenceManager.setLoggedIn(true)

                        val account = api.verifyCredentials("Bearer ${token.access_token}")
                        preferenceManager.setUserInfo(account.username, account.displayName, account.avatar)

                        isLoading = false
                        onAuthSuccess()
                    } catch (e: Exception) {
                        isLoading = false
                        error = "Authentication failed: ${e.message}"
                    }
                }
            },
            onError = { msg ->
                oauthUrl = null
                error = msg
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Mastodon Widget",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Sign in with your Mastodon account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = serverUrlInput,
            onValueChange = {
                serverUrlInput = it
                error = null
            },
            label = { Text("Instance URL") },
            placeholder = { Text("mastodon.social") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = {
                val rawUrl = serverUrlInput.trim()
                val serverUrl = (if (rawUrl.startsWith("http")) rawUrl else "https://$rawUrl").trimEnd('/')
                isLoading = true
                error = null
                scope.launch {
                    try {
                        preferenceManager.setServerUrl(serverUrl)
                        ApiClient.setBaseUrl("$serverUrl/api/v1/")
                        val api = ApiClient.getInstance()

                        val app = api.createApplication(
                            CreateAppRequest(clientName = "Mastodon Widget", redirectUris = REDIRECT_URI)
                        )
                        preferenceManager.setClientCredentials(app.client_id, app.client_secret)

                        val authUrl = "$serverUrl/oauth/authorize?" +
                            "client_id=${Uri.encode(app.client_id)}&" +
                            "redirect_uri=${Uri.encode(REDIRECT_URI)}&" +
                            "response_type=code&" +
                            "scope=${Uri.encode(SCOPES)}"

                        isLoading = false
                        oauthUrl = authUrl
                    } catch (e: Exception) {
                        isLoading = false
                        error = "Could not reach server: ${e.message}"
                    }
                }
            },
            enabled = !isLoading && serverUrlInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OAuthWebView(
    url: String,
    onCodeReceived: (String) -> Unit,
    onError: (String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val reqUrl = request.url.toString()
                        if (reqUrl.startsWith(REDIRECT_URI)) {
                            val code = Uri.parse(reqUrl).getQueryParameter("code")
                            if (code != null) {
                                onCodeReceived(code)
                            } else {
                                onError("No authorization code in response")
                            }
                            return true
                        }
                        return false
                    }
                }
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
