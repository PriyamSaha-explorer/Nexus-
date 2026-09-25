package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentIntelligenceScreen
import com.example.ui.screens.ExperimentScreen
import com.example.ui.screens.ImageUnderstandingScreen
import com.example.ui.screens.KnowledgeGraphScreen
import com.example.ui.screens.LearningPathScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val nexusViewModel: NexusViewModel = viewModel()
                NexusApp(viewModel = nexusViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(viewModel: NexusViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back gesture to return to previous screen or dashboard
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        if (!viewModel.navigateBack()) {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentScreen == AppScreen.DASHBOARD) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF00E5FF), Color(0xFFA855F7))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NEXUS",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        } else {
                            Text(
                                text = when (currentScreen) {
                                    AppScreen.LEARNING_PATH -> "Adaptive Learning Path"
                                    AppScreen.AI_TUTOR -> "NEXUS AI Navigator"
                                    AppScreen.KNOWLEDGE_GRAPH -> "Personal Knowledge Graph"
                                    AppScreen.SIMULATOR -> "NEXUS Simulator"
                                    AppScreen.PRACTICE -> "Adaptive Practice"
                                    AppScreen.IMAGE_ANALYSIS -> "Image Understanding"
                                    AppScreen.DOCUMENTS -> "Document Intelligence"
                                    AppScreen.EXPERIMENTS -> "AI Experiment Laboratory"
                                    AppScreen.SEARCH -> "Global Search"
                                    AppScreen.SETTINGS -> "Settings"
                                    else -> "NEXUS"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentScreen != AppScreen.DASHBOARD) {
                        IconButton(
                            onClick = {
                                if (!viewModel.navigateBack()) {
                                    viewModel.navigateTo(AppScreen.DASHBOARD)
                                }
                            },
                            modifier = Modifier.testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (currentScreen != AppScreen.SEARCH) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SEARCH) },
                            modifier = Modifier.testTag("top_bar_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                    if (currentScreen != AppScreen.SETTINGS) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("top_bar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090E1B)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF090E1B),
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF131F38),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("bottom_nav_dashboard")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.LEARNING_PATH,
                    onClick = { viewModel.navigateTo(AppScreen.LEARNING_PATH) },
                    icon = { Icon(Icons.Default.Route, contentDescription = "Path") },
                    label = { Text("Path", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF131F38),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("bottom_nav_learning_path")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.AI_TUTOR,
                    onClick = { viewModel.navigateTo(AppScreen.AI_TUTOR) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                    label = { Text("AI Tutor", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF131F38),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("bottom_nav_ai_tutor")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.KNOWLEDGE_GRAPH,
                    onClick = { viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH) },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Graph") },
                    label = { Text("Graph", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF131F38),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("bottom_nav_knowledge_graph")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SIMULATOR,
                    onClick = { viewModel.navigateTo(AppScreen.SIMULATOR) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Simulator") },
                    label = { Text("Simulate", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF131F38),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("bottom_nav_simulator")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.PRACTICE,
                    onClick = { viewModel.navigateTo(AppScreen.PRACTICE) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Practice") },
                    label = { Text("Practice", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        selectedTextColor = Color(0xFF00E5FF),
                        indicatorColor = Color(0xFF131F38),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("bottom_nav_practice")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.LEARNING_PATH -> LearningPathScreen(viewModel = viewModel)
                AppScreen.AI_TUTOR -> AiTutorScreen(viewModel = viewModel)
                AppScreen.KNOWLEDGE_GRAPH -> KnowledgeGraphScreen(viewModel = viewModel)
                AppScreen.SIMULATOR -> SimulatorScreen(viewModel = viewModel)
                AppScreen.PRACTICE -> PracticeScreen(viewModel = viewModel)
                AppScreen.IMAGE_ANALYSIS -> ImageUnderstandingScreen(viewModel = viewModel)
                AppScreen.DOCUMENTS -> DocumentIntelligenceScreen(viewModel = viewModel)
                AppScreen.EXPERIMENTS -> ExperimentScreen(viewModel = viewModel)
                AppScreen.SEARCH -> SearchScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
