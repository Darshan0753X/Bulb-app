package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullColorStudioSection
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.ScenesScreen
import com.example.ui.screens.SettingsDiagnosticsScreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SmitchTheme
import com.example.viewmodel.BulbViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    COLOURS("Colours", Icons.Filled.ColorLens, Icons.Outlined.ColorLens),
    SCENES("Scenes", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    MUSIC("Music", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: BulbViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            SmitchTheme(isDarkTheme = uiState.isDarkTheme) {
                var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("main_bottom_nav_bar")
                        ) {
                            NavigationTab.values().forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 10.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = NeonCyan,
                                        indicatorColor = NeonCyan,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val activeBulbColor = uiState.previewColor
                    val isLit = uiState.previewPower
                    val currentBrightness = uiState.previewBrightness

                    val animatedAmbientColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isLit) {
                            activeBulbColor.copy(alpha = (0.22f * (currentBrightness / 100f)).coerceIn(0.08f, 0.35f))
                        } else {
                            Color.Transparent
                        },
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
                        label = "appBackgroundTint"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(
                                        animatedAmbientColor,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.HOME -> HomeDashboardScreen(viewModel = viewModel)
                            NavigationTab.COLOURS -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(18.dp)
                                ) {
                                    FullColorStudioSection(
                                        currentColor = uiState.previewColor,
                                        isPending = uiState.isColorPending,
                                        favoriteColors = uiState.favoriteColors,
                                        recentColors = uiState.recentColors,
                                        onColorChanged = { viewModel.onPreviewColorChanged(it) },
                                        onToggleFavorite = { viewModel.toggleFavoriteColor(it) },
                                        onApplyColor = { viewModel.applyColor() }
                                    )
                                }
                            }
                            NavigationTab.SCENES -> ScenesScreen(viewModel = viewModel)
                            NavigationTab.MUSIC -> MusicScreen(viewModel = viewModel)
                            NavigationTab.SETTINGS -> SettingsDiagnosticsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
