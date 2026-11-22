package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.HabitDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.HabitRepository
import com.example.domain.model.HabitWithProgress
import com.example.ui.components.AddEditHabitSheet
import com.example.ui.navigation.HabitRoutes
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.HabitPulseTheme
import com.example.ui.viewmodel.HabitViewModel
import com.example.ui.viewmodel.HabitViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current.applicationContext
            val viewModel: HabitViewModel = viewModel(
                factory = remember {
                    val db = HabitDatabase.getInstance(context)
                    val repo = HabitRepository(db.habitDao())
                    val prefs = PreferencesManager(context)
                    HabitViewModelFactory(repo, prefs)
                }
            )
            val themeOverride by viewModel.isDarkThemeOverride.collectAsStateWithLifecycle()
            val isDark = themeOverride ?: isSystemInDarkTheme()

            HabitPulseTheme(darkTheme = isDark) {
                HabitPulseApp(
                    viewModel = viewModel,
                    isDarkTheme = isDark
                )
            }
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitPulseApp(
    viewModel: HabitViewModel,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val focusState by viewModel.focusState.collectAsStateWithLifecycle()

    var showHabitSheet by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitWithProgress?>(null) }

    val topDestinations = remember {
        listOf(
            TopLevelDestination(
                route = HabitRoutes.ROUTE_TODAY,
                labelRes = R.string.nav_today,
                selectedIcon = Icons.Rounded.CheckCircle,
                unselectedIcon = Icons.Outlined.CheckCircle,
                testTag = "nav_today"
            ),
            TopLevelDestination(
                route = HabitRoutes.ROUTE_ANALYTICS,
                labelRes = R.string.nav_analytics,
                selectedIcon = Icons.Rounded.BarChart,
                unselectedIcon = Icons.Outlined.BarChart,
                testTag = "nav_analytics"
            ),
            TopLevelDestination(
                route = HabitRoutes.ROUTE_FOCUS,
                labelRes = R.string.nav_focus,
                selectedIcon = Icons.Rounded.Timer,
                unselectedIcon = Icons.Outlined.Timer,
                testTag = "nav_focus"
            )
        )
    }

    val isTopLevelScreen = currentRoute in topDestinations.map { it.route } || currentRoute == null

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (isTopLevelScreen) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.toggleTheme(isDarkTheme) },
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                    contentDescription = stringResource(R.string.action_toggle_theme)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            floatingActionButton = {
                if (currentRoute == HabitRoutes.ROUTE_TODAY || currentRoute == null) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingHabit = null
                            showHabitSheet = true
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.action_add_habit)
                            )
                        },
                        text = { Text(stringResource(R.string.action_add_habit)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.testTag("add_habit_fab")
                    )
                }
            },
            bottomBar = {
                if (isTopLevelScreen && !useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        topDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route ||
                                (currentRoute == null && dest.route == HabitRoutes.ROUTE_TODAY)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelScreen && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        topDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route ||
                                (currentRoute == null && dest.route == HabitRoutes.ROUTE_TODAY)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = HabitRoutes.ROUTE_TODAY,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(HabitRoutes.ROUTE_TODAY) {
                            TodayScreen(
                                state = dashboardState,
                                isDarkTheme = isDarkTheme,
                                onSelectDate = viewModel::selectDate,
                                onSelectTimeFilter = viewModel::selectTimeFilter,
                                onToggleHabit = { viewModel.toggleHabit(it) },
                                onIncrementHabit = { viewModel.adjustHabitCount(it, 1) },
                                onDecrementHabit = { viewModel.adjustHabitCount(it, -1) },
                                onOpenHabitDetail = { id ->
                                    navController.navigate(HabitRoutes.habitDetailRoute(id))
                                },
                                onOpenNewHabit = {
                                    editingHabit = null
                                    showHabitSheet = true
                                },
                                onAddStarterPack = viewModel::addStarterPack
                            )
                        }

                        composable(HabitRoutes.ROUTE_ANALYTICS) {
                            AnalyticsScreen(
                                state = dashboardState,
                                isDarkTheme = isDarkTheme,
                                onOpenHabitDetail = { id ->
                                    navController.navigate(HabitRoutes.habitDetailRoute(id))
                                }
                            )
                        }

                        composable(HabitRoutes.ROUTE_FOCUS) {
                            FocusScreen(
                                dashboardState = dashboardState,
                                focusState = focusState,
                                onSelectPreset = viewModel::selectFocusPreset,
                                onLinkHabit = viewModel::linkHabitToFocus,
                                onToggleRunning = viewModel::toggleFocusRunning,
                                onReset = viewModel::resetFocusTimer,
                                onCompleteNow = viewModel::completeFocusSessionNow,
                                onToggleAutoComplete = viewModel::setAutoCompleteFocusHabit,
                                onDismissBanner = viewModel::dismissFocusBanner
                            )
                        }

                        composable(
                            route = HabitRoutes.ROUTE_HABIT_DETAIL,
                            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                        ) { entry ->
                            val habitId = entry.arguments?.getLong("habitId") ?: -1L
                            HabitDetailScreen(
                                habitId = habitId,
                                state = dashboardState,
                                isDarkTheme = isDarkTheme,
                                onBack = { navController.popBackStack() },
                                onEdit = { habit ->
                                    editingHabit = habit
                                    showHabitSheet = true
                                },
                                onDelete = viewModel::deleteHabit,
                                onToggleDate = { habit, dateKey ->
                                    viewModel.toggleHabit(habit, dateKey)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHabitSheet) {
        AddEditHabitSheet(
            editingHabit = editingHabit,
            isDarkTheme = isDarkTheme,
            onDismiss = {
                showHabitSheet = false
                editingHabit = null
            },
            onSave = { id, title, cue, category, timeOfDay, color, targetCount, unit ->
                viewModel.saveHabit(
                    editingHabitId = id,
                    title = title,
                    cue = cue,
                    category = category,
                    timeOfDay = timeOfDay,
                    color = color,
                    targetCount = targetCount,
                    unit = unit
                )
                showHabitSheet = false
                editingHabit = null
            }
        )
    }
}
