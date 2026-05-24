package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Screen Enumerations for robust state-based layout navigation (overlapping gesture items elegantly)
enum class AppScreen {
    SPLASH,
    LOGIN,
    HOME_TABS,
    COURSE_DETAIL,
    VIDEO_PLAYER,
    LIVE_CLASS,
    MOCK_TEST_SOLVER,
    SUPREME_AI_DASHBOARD
}

enum class HomeTab {
    EXPLORE,
    COURSES,
    ASK_DOUBT,
    MOCK_TESTS,
    MY_PROFILE,
    ADMIN_CENTER
}

@Composable
fun StudyNovaApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentThemeState by viewModel.currentTheme.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    // String navigation router stack
    var activeScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var previousScreenStack by remember { mutableStateOf<List<AppScreen>>(emptyList()) }
    var activeTab by remember { mutableStateOf(HomeTab.EXPLORE) }

    // Navigation Helper
    val navigateTo: (AppScreen) -> Unit = { screen ->
        previousScreenStack = previousScreenStack + activeScreen
        activeScreen = screen
    }

    val navigateBack: () -> Unit = {
        if (previousScreenStack.isNotEmpty()) {
            activeScreen = previousScreenStack.last()
            previousScreenStack = previousScreenStack.dropLast(1)
        } else {
            activeScreen = AppScreen.HOME_TABS
        }
    }

    // Edge-to-edge theme wrapper
    StudyNovaTheme(themeStyle = currentThemeState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFF0C0721),
                            Color(0xFF04020F)
                        )
                    )
                )
        ) {
            when (activeScreen) {
                AppScreen.SPLASH -> {
                    SplashScreen(onFinished = {
                        if (currentUser != null) {
                            activeScreen = AppScreen.HOME_TABS
                        } else {
                            activeScreen = AppScreen.LOGIN
                        }
                    })
                }

                AppScreen.LOGIN -> {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            activeScreen = AppScreen.HOME_TABS
                        }
                    )
                }

                AppScreen.HOME_TABS -> {
                    MainTabCabinet(
                        viewModel = viewModel,
                        activeTab = activeTab,
                        onTabChanged = { activeTab = it },
                        onNavigateToScreen = { navigateTo(it) }
                    )
                }

                AppScreen.COURSE_DETAIL -> {
                    CourseDetailScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onPlayLecture = { navigateTo(AppScreen.VIDEO_PLAYER) }
                    )
                }

                AppScreen.VIDEO_PLAYER -> {
                    PremiumVideoPlayerScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }

                AppScreen.LIVE_CLASS -> {
                    LiveClassroomScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }

                AppScreen.MOCK_TEST_SOLVER -> {
                    MockTestSolverScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }

                AppScreen.SUPREME_AI_DASHBOARD -> {
                    OmegaConsoleScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN (Animated Intro)
// ==========================================
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val animScale = remember { Animatable(0.2f) }
    val animRotate = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Dynamic Entry Animations
        coroutineScope.launch {
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        coroutineScope.launch {
            animRotate.animateTo(
                targetValue = 360f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing)
            )
        }
        coroutineScope.launch {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(2200, easing = LinearEasing)
            )
            delay(300)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background futuristic coordinates pattern
                val strokeColor = Color(0x06864AF9)
                val spacing = 45.dp.toPx()
                val safeSpacing = spacing.toInt().coerceAtLeast(1)
                for (x in 0..size.width.toInt() step safeSpacing) {
                    drawLine(strokeColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                }
                for (y in 0..size.height.toInt() step safeSpacing) {
                    drawLine(strokeColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // StudyNova Hologram Logo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(animScale.value)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x33864AF9), Color.Transparent)
                            ),
                            radius = (size.width * 1.2f).coerceAtLeast(1f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer Cyber Ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(CyberPurple, NeonCyan, CyberPink, CyberPurple)
                        ),
                        startAngle = animRotate.value,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Inner pulsing core
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "StudyNova Launch Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(75.dp)
                )
                
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = "Book Core",
                    tint = CyberPink,
                    modifier = Modifier
                        .size(35.dp)
                        .offset(y = 20.dp, x = 15.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Typography Headings
            Text(
                text = "STUDYNOVA",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                letterSpacing = 6.sp,
                modifier = Modifier.drawBehind {
                    // Underline holographic line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, NeonCyan, CyberPurple, Color.Transparent)
                        ),
                        start = Offset(0f, size.height + 8.dp.toPx()),
                        end = Offset(size.width, size.height + 8.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "LEARN • GROW • SUCCEED",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Loading bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressAnim.value.coerceAtLeast(0.01f))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(CyberPurple, NeonCyan)
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SUPREME AI Engine Active...",
                fontSize = 11.sp,
                color = NeonCyan.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==========================================
// 2. AUTHENTICATION & LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    var emailInput by remember { mutableStateOf("student@studynova.com") }
    var passwordInput by remember { mutableStateOf("student123") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background lights
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1F864AF9), Color.Transparent)
                        ),
                        center = Offset(0f, 0f),
                        radius = (size.width * 1.5f).coerceAtLeast(1f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1F00F0FF), Color.Transparent)
                        ),
                        center = Offset(size.width, size.height),
                        radius = (size.width * 1.5f).coerceAtLeast(1f)
                    )
                }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Edu Nova Icon",
                    tint = CyberPurple,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "StudyNova",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Learn is the Ultimate Asset",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "SECURE PORTAL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Outlined Credentials
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email/Gmail Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPurple,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = CyberPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPurple,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = CyberPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (feedbackMessage.isNotEmpty()) {
                            Text(
                                text = feedbackMessage,
                                color = NeonCyan,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    errorMessage = "Please enter valid credentials to access StudyNova."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = ""
                                feedbackMessage = ""
                                viewModel.login(emailInput, passwordInput) { success, message ->
                                    isLoading = false
                                    if (success) {
                                        feedbackMessage = message
                                        coroutineScope.launch {
                                            delay(800)
                                            onLoginSuccess()
                                        }
                                    } else {
                                        errorMessage = message
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Login, contentDescription = "Lock open")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enter Space Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Quick login badges for easier testing sandbox!
                Text(
                    text = "TEST CHANNELS PRESETS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    PresetUserChip("Student Acc", "student@studynova.com", "student123") { e, p ->
                        emailInput = e
                        passwordInput = p
                    }
                    PresetUserChip("Teacher Prof", "alakh@studynova.com", "test123") { e, p ->
                        emailInput = e
                        passwordInput = p
                    }
                    PresetUserChip("Master Admin", "dhinwabrothers125@gmail.com", "himanshu@#9680") { e, p ->
                        emailInput = e
                        passwordInput = p
                    }
                }
            }
        }
    }
}

@Composable
fun PresetUserChip(label: String, email: String, pass: String, onClick: (String, String) -> Unit) {
    AssistChip(
        onClick = { onClick(email, pass) },
        label = { Text(label, color = Color.White, fontSize = 12.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            labelColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

// ==========================================
// 3. MAIN TAB FLOW (Home Bottom Navigation Cabinet)
// ==========================================
@Composable
fun MainTabCabinet(
    viewModel: MainViewModel,
    activeTab: HomeTab,
    onTabChanged: (HomeTab) -> Unit,
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isUserAdmin = currentUser?.role == "admin"
    val isUserTeacher = currentUser?.role == "teacher"

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF070B1D),
                windowInsets = WindowInsets.navigationBars // Ensure edge-to-edge navigation bottom padding
            ) {
                NavigationBarItem(
                    selected = activeTab == HomeTab.EXPLORE,
                    onClick = { onTabChanged(HomeTab.EXPLORE) },
                    icon = { Icon(imageVector = Icons.Default.TravelExplore, contentDescription = "Explore space") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = Color(0x3300F0FF)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == HomeTab.COURSES,
                    onClick = { onTabChanged(HomeTab.COURSES) },
                    icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = "Courses catalog") },
                    label = { Text("Courses", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPurple,
                        selectedTextColor = CyberPurple,
                        indicatorColor = Color(0x33864AF9)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == HomeTab.ASK_DOUBT,
                    onClick = { onTabChanged(HomeTab.ASK_DOUBT) },
                    icon = { Icon(imageVector = Icons.Default.Face, contentDescription = "Supreme AI doubt solver") },
                    label = { Text("Supreme AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPink,
                        selectedTextColor = CyberPink,
                        indicatorColor = Color(0x33FF52A2)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == HomeTab.MOCK_TESTS,
                    onClick = { onTabChanged(HomeTab.MOCK_TESTS) },
                    icon = { Icon(imageVector = Icons.Default.Quiz, contentDescription = "Tests mock tests") },
                    label = { Text("Tests", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = Color(0x3300F0FF)
                    )
                )

                NavigationBarItem(
                    selected = activeTab == HomeTab.MY_PROFILE,
                    onClick = { onTabChanged(HomeTab.MY_PROFILE) },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Achievements portal") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPurple,
                        selectedTextColor = CyberPurple,
                        indicatorColor = Color(0x33864AF9)
                    )
                )

                // Master Administrative Control Option
                if (isUserAdmin || isUserTeacher) {
                    NavigationBarItem(
                        selected = activeTab == HomeTab.ADMIN_CENTER,
                        onClick = { onTabChanged(HomeTab.ADMIN_CENTER) },
                        icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin core control") },
                        label = { Text(if (isUserAdmin) "SUPREME" else "Faculty", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPink,
                            selectedTextColor = CyberPink,
                            indicatorColor = Color(0x33FF52A2)
                        )
                    )
                }
            }
        },
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (activeTab != HomeTab.ADMIN_CENTER) {
                FloatingActionButton(
                    onClick = { onTabChanged(HomeTab.ASK_DOUBT) },
                    containerColor = CyberPurple,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Adb, 
                        contentDescription = "Supreme Voice AI Orb",
                        modifier = Modifier.size(28.dp),
                        tint = NeonCyan
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                HomeTab.EXPLORE -> ExploreHubView(viewModel = viewModel, onNavigateToScreen = onNavigateToScreen)
                HomeTab.COURSES -> CoursesCatalogView(viewModel = viewModel, onNavigateToScreen = onNavigateToScreen)
                HomeTab.ASK_DOUBT -> AIDoubtsView(viewModel = viewModel)
                HomeTab.MOCK_TESTS -> MockTestsPortalView(viewModel = viewModel, onNavigateToScreen = onNavigateToScreen)
                HomeTab.MY_PROFILE -> StudentDashboardView(viewModel = viewModel, onLogout = { onNavigateToScreen(AppScreen.LOGIN) })
                HomeTab.ADMIN_CENTER -> AdminCenterView(viewModel = viewModel, onNavigateToScreen = onNavigateToScreen)
            }
        }
    }
}

// ==========================================
// 3A. EXPLORE HUB VIEW (HOME SCREEN)
// ==========================================
@Composable
fun ExploreHubView(
    viewModel: MainViewModel,
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val allLiveClasses by viewModel.allLiveClasses.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    
    // Category pills data
    val categoryList = listOf("JEE", "NEET", "Coding Courses", "Class 11", "Class 12", "UPSC", "Spoken English", "CUET", "Class 10", "Class 9")
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome and Streak Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Hello, ${currentUser?.name ?: "Scholar"}! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Level ${currentUser?.level ?: 1} Core Cadet",
                        fontSize = 14.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Hot Flame Streak Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2E1541))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Active Streaks",
                        tint = Color(0xFFFF5223),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${currentUser?.streak ?: 1} Days",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Search Bar Outlined
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Physics, Java Coding, Genetics...", color = Color.White.copy(0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Core Seek", tint = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color(0xFF0F122B),
                    unfocusedContainerColor = Color(0xFF070B1E)
                ),
                shape = RoundedCornerShape(26.dp),
                singleLine = true
            )
        }

        // Promotional Sliding Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF1F0D3D), Color(0xFF092949))
                        )
                    )
                    .clickable {
                        // AI-Assisted Mock Banner click logic
                    }
            ) {
                // Glow circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 240.dp, y = (-20).dp)
                        .background(brush = Brush.radialGradient(listOf(NeonCyan.copy(0.2f), Color.Transparent)))
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF2E93))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("MEGA EVENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("National Mock JEE Scholarship", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text("Win Rs 10 Lakh cash rewards & direct Unacademy / PW coaching waiver.", fontSize = 12.sp, color = Color.LightGray)
                    }

                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Prizes",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        // Course Filters (Categories horizontal scroll)
        item {
            Column {
                Text(
                    text = "ACADEMIC STREAMS",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Stream") },
                            colors = FilterChipDefaults.filterChipColors(labelColor = Color.White)
                        )
                    }
                    items(categoryList) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(labelColor = Color.White)
                        )
                    }
                }
            }
        }

        // Live Class Sessions Widget (Blinking Indicator)
        item {
            val lives = allLiveClasses.filter { it.status == "live" }
            if (lives.isNotEmpty()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE CLASSES UNDERWAY",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lives) { cls ->
                            LiveStreamingHubCard(liveClass = cls) {
                                viewModel.joinLiveClass(cls)
                                onNavigateToScreen(AppScreen.LIVE_CLASS)
                            }
                        }
                    }
                }
            }
        }

        // Recommended / Trending Courses Section
        item {
            Text(
                text = "TRENDING ON STUDYNOVA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            val filtered = allCourses.filter {
                (selectedCategoryFilter == null || it.category == selectedCategoryFilter) &&
                (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true))
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No direct courses match filter settings. Search again!", color = Color.White.copy(0.4f))
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered) { crs ->
                        CourseNetflixCard(course = crs) {
                            viewModel.selectCourseAndOpen(crs)
                            onNavigateToScreen(AppScreen.COURSE_DETAIL)
                        }
                    }
                }
            }
        }

        // Broadcast Logs / Notifications Feed popup
        item {
            if (allNotifications.isNotEmpty()) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "OFFICIAL BULLETINS",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allNotifications.take(2).forEach { notif ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val notifIcon = when (notif.category) {
                                        "achievement" -> Icons.Default.EmojiEvents
                                        "live_class" -> Icons.Default.Mic
                                        "exam_alert" -> Icons.Default.Assignment
                                        else -> Icons.Default.NotificationsActive
                                    }
                                    Icon(
                                        imageVector = notifIcon,
                                        contentDescription = "Alert type",
                                        tint = if (notif.category == "achievement") Color.Yellow else CyberPink,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text(notif.message, color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LiveStreamingHubCard(liveClass: LiveClass, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0x3E1C1F3D)),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.People, contentDescription = "", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${liveClass.watchers} viewers", color = Color.LightGray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(liveClass.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Instructor: ${liveClass.teacherName}", color = NeonCyan, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Videocam, contentDescription = "", tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Join Interactive Room", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CourseNetflixCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F122C)),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = course.bannerUrl,
                    contentDescription = course.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Stream Tag bubble
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberPurple)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(course.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.instructorName,
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Standard Path", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 3B. COURSES CATALOG VIEW
// ==========================================
@Composable
fun CoursesCatalogView(
    viewModel: MainViewModel,
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    var selectedTabCategory by remember { mutableStateOf<String?>("All") }
    val categories = listOf("All", "JEE", "NEET", "Coding Courses", "Class 11", "Class 12", "UPSC", "Spoken English")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("STUDYNOVA ACADEMY", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Structured curriculum designed by India's top educators", fontSize = 13.sp, color = Color.LightGray)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Categories Scroll
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedTabCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyberPurple else Color.White.copy(0.05f))
                        .clickable { selectedTabCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(cat, color = if (isSelected) Color.White else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayList = allCourses.filter {
            selectedTabCategory == "All" || it.category == selectedTabCategory
        }

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No designated courses in this category yet.", color = Color.White.copy(0.4f))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayList) { course ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCourseAndOpen(course)
                                onNavigateToScreen(AppScreen.COURSE_DETAIL)
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            // Banner
                            AsyncImage(
                                model = course.bannerUrl,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonCyan.copy(0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(course.category, color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(course.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("Educator: ${course.instructorName}", color = Color.White.copy(0.6f), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap to view full content folder", color = CyberPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3C. AI DOUBTS PORTAL (Direct REST GEMINI-3.5-FLASH Integration)
// ==========================================
@Composable
fun AIDoubtsView(viewModel: MainViewModel) {
    val doubtConversation by viewModel.doubtConversation.collectAsStateWithLifecycle()
    val isAILoading by viewModel.isAILoading.collectAsStateWithLifecycle()
    var userQuestion by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    LaunchedEffect(doubtConversation.size) {
        listState.animateScrollTo(listState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Hologram header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.Adb, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("NOVA AUTONOMOUS AI", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                Text("Powered by Gemini 3.5 Flash | Instant Doubt Resolution", fontSize = 11.sp, color = NeonCyan)
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

        // Chat Bubble Logs
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(listState)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (doubtConversation.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Forum, contentDescription = "", tint = Color.LightGray.copy(0.2f), modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ask any JEE concept, Code bug, or Spoken English tips. Nova AI solves doubts with structured derivations instantly!",
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                doubtConversation.forEach { msg ->
                    val isAi = msg.sender == "ai"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAi) 4.dp else 16.dp,
                                        bottomEnd = if (isAi) 16.dp else 4.dp
                                    )
                                )
                                .background(if (isAi) Color(0xFF14122C) else CyberPurple)
                                .border(
                                    1.dp,
                                    if (isAi) NeonCyan.copy(0.15f) else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                                .widthIn(max = 290.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isAi) "NOVA AI" else "YOU",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAi) NeonCyan else Color.White.copy(0.7f),
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            if (isAILoading) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nova AI resolving logical mechanics...", fontSize = 12.sp, color = NeonCyan, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Input Console Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = userQuestion,
                onValueChange = { userQuestion = it },
                placeholder = { Text("Ask doubt (e.g. derive Moment of Inertia of a cone)", color = Color.White.copy(0.4f)) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPurple,
                    unfocusedBorderColor = Color.White.copy(0.1f),
                    focusedContainerColor = Color(0xFF0F122B),
                    unfocusedContainerColor = Color(0xFF060913)
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (userQuestion.isNotBlank() && !isAILoading) {
                        viewModel.sendDoubt(userQuestion)
                        userQuestion = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberPurple)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Query AI", tint = Color.White)
            }
        }
    }
}

// ==========================================
// 3D. MOCK TESTS PORTAL
// ==========================================
@Composable
fun MockTestsPortalView(
    viewModel: MainViewModel,
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val allMockTests by viewModel.allMockTests.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.HelpCenter, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("TEST & ASSESSMENT CELLS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("JEE Mains, NEET Genetics, Spoken English quizzes with deep analytics", fontSize = 11.sp, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allMockTests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No assessment files active currently.", color = Color.White.copy(0.4f))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allMockTests) { test ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberPink.copy(0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(test.category, color = CyberPink, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = "", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${test.durationMinutes} mins", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(test.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Standard Marking: Correct +4 | Error -1. Deep post-test explanation sheets verified.", color = Color.LightGray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.startMockTest(test)
                                    onNavigateToScreen(AppScreen.MOCK_TEST_SOLVER)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch Examination Engine", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3E. STUDENT DASHBOARD & GAMIFICATION VIEW
// ==========================================
@Composable
fun StudentDashboardView(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Upper Profile Hologram Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.sweepGradient(
                                    listOf(CyberPurple, NeonCyan, CyberPink, CyberPurple)
                                )
                            )
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0F122B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Avatar",
                                tint = NeonCyan,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(currentUser?.name ?: "Cadet Scholar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                    Text(currentUser?.email ?: "student@studynova.com", color = Color.White.copy(0.5f), fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.05f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("CADET RANK: CLASS LEVEL ${currentUser?.level ?: 1}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Streak Progress, XP, Coins Tracker Panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // XP Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x32864AF9))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "", tint = Color.Yellow)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("TOTAL XP", fontSize = 12.sp, color = Color.LightGray)
                        Text("${currentUser?.xp ?: 0}", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                    }
                }

                // Coins Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3E1C1F3D))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "", tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("NOVA COINS", fontSize = 12.sp, color = Color.LightGray)
                        Text("${currentUser?.coins ?: 0}", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }

        // Native Canvas-Drawn Weekly Graph as per design guidelines!
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WEEKLY CADET METRICS (XP GROWTH)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        // Drawing professional coordinate lines
                        val strokeColor = Color.White.copy(0.1f)
                        val points = listOf(10f, 40f, 25f, 65f, 50f, 95f, 120f) // Simulated levels
                        val spacingX = size.width / (points.size - 1)
                        val maxVal = 140f

                        // Draw grids
                        for (i in 0..4) {
                            val y = size.height * (i / 4f)
                            drawLine(strokeColor, Offset(0f, y), Offset(size.width, y))
                        }

                        // Plotting points paths
                        val linePath = Path()
                        val fillPath = Path()

                        points.forEachIndexed { index, xp ->
                            val xPos = index * spacingX
                            // Invert y because (0,0) is top-left in canvas
                            val yPos = size.height - (xp / maxVal * size.height)

                            if (index == 0) {
                                linePath.moveTo(xPos, yPos)
                                fillPath.moveTo(xPos, size.height)
                                fillPath.lineTo(xPos, yPos)
                            } else {
                                linePath.lineTo(xPos, yPos)
                                fillPath.lineTo(xPos, yPos)
                            }

                            if (index == points.size - 1) {
                                fillPath.lineTo(xPos, size.height)
                                fillPath.close()
                            }

                            // Glowing vertex dots
                            drawCircle(CyberPink, radius = 4.dp.toPx(), center = Offset(xPos, yPos))
                        }

                        // Drawing line path and gradient filling path
                        drawPath(linePath, CyberPurple, style = Stroke(width = 3.dp.toPx()))
                        drawPath(
                            fillPath,
                            Brush.verticalGradient(listOf(CyberPurple.copy(alpha = 0.3f), Color.Transparent))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        days.forEach { Text(it, fontSize = 11.sp, color = Color.LightGray) }
                    }
                }
            }
        }

        // Active theme customization settings
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "THEME STYLING CABIN",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberPink,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item { ThemeStyleSelectButton("Cosmic", StudyNovaThemeStyle.COSMIC_DARK, viewModel) }
                        item { ThemeStyleSelectButton("AMOLED", StudyNovaThemeStyle.AMOLED, viewModel) }
                        item { ThemeStyleSelectButton("Blue", StudyNovaThemeStyle.BLUE_PREMIUM, viewModel) }
                        item { ThemeStyleSelectButton("Purple", StudyNovaThemeStyle.PURPLE_FUTURISTIC, viewModel) }
                        item { ThemeStyleSelectButton("Cyber Yellow", StudyNovaThemeStyle.CYBER_YELLOW, viewModel) }
                        item { ThemeStyleSelectButton("Neon Green", StudyNovaThemeStyle.NEON_GREEN, viewModel) }
                        item { ThemeStyleSelectButton("Crimson Red", StudyNovaThemeStyle.CRIMSON_RED, viewModel) }
                        item { ThemeStyleSelectButton("Solar Eclipse", StudyNovaThemeStyle.SOLAR_ECLIPSE, viewModel) }
                        item { ThemeStyleSelectButton("Quantum Silver", StudyNovaThemeStyle.QUANTUM_SILVER, viewModel) }
                        item { ThemeStyleSelectButton("Holographic", StudyNovaThemeStyle.HOLOGRAPHIC_TEAL, viewModel) }
                        item { ThemeStyleSelectButton("Virtual Violet", StudyNovaThemeStyle.VIRTUAL_VIOLET, viewModel) }
                        item { ThemeStyleSelectButton("Midnight Gold", StudyNovaThemeStyle.MIDNIGHT_GOLD, viewModel) }
                        item { ThemeStyleSelectButton("Toxic Punk", StudyNovaThemeStyle.TOXIC_PUNK, viewModel) }
                        item { ThemeStyleSelectButton("Galaxy Orbit", StudyNovaThemeStyle.GALAXY_ORBIT, viewModel) }
                    }
                }
            }
        }

        // Logout exit door
        item {
            Button(
                onClick = { onLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "", tint = Color.Red)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deactivate Cadet Session", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ThemeStyleSelectButton(label: String, style: StudyNovaThemeStyle, viewModel: MainViewModel) {
    val activeS by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isSel = activeS == style

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSel) androidx.compose.material3.MaterialTheme.colorScheme.primary else Color.White.copy(0.04f))
            .clickable { viewModel.changeTheme(style) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (isSel) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ==========================================
// 4. COURSE DETAILED STRUCTURE WITH DOWNLOAD NOTES & LESSON CHOICES
// ==========================================
@Composable
fun CourseDetailScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPlayLecture: () -> Unit
) {
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val activeLesson by viewModel.activeLesson.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val bookmarkedLessons by viewModel.bookmarkedLessons.collectAsStateWithLifecycle()

    val lessons = selectedCourse?.let { viewModel.deserializeLessons(it.lessonsJson) } ?: emptyList()

    Scaffold(
        topBar = {
            OptTopAppBar(title = selectedCourse?.title ?: "Course Syllabus", onBack = onBack)
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner Poster
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = selectedCourse?.bannerUrl,
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    )
                }
            }

            // Description details
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberPurple)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(selectedCourse?.category ?: "Core", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedCourse?.title ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Educator: ${selectedCourse?.instructorName ?: ""}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedCourse?.description ?: "Highly comprehensive educational path structured by standard faculty boards.", color = Color.LightGray, fontSize = 13.sp)
                }
            }

            // Chapter playlist title
            item {
                Divider(color = Color.White.copy(0.1f))
                Spacer(modifier = Modifier.height(4.dp))
                Text("COURSE CURRICULUM (${lessons.size} Chapters)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }

            // Dynamic Chapters List
            items(lessons) { lesson ->
                val isActive = activeLesson?.id == lesson.id
                val progress = downloadProgress[selectedCourse?.id ?: 0] ?: 0
                val isBookmarked = bookmarkedLessons.contains(lesson.id)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isActive) CyberPurple else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    viewModel.changeActiveLesson(lesson)
                                    onPlayLecture()
                                },
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) CyberPurple else Color.White.copy(0.08f))
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "", tint = Color.White)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lesson.title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) NeonCyan else Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = lesson.description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Bookmark toggle
                            IconButton(onClick = { viewModel.toggleBookmark(lesson.id) }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "",
                                    tint = if (isBookmarked) CyberPink else Color.LightGray
                                )
                            }
                        }

                        // Notes Download Panel if lesson contains PDF Url
                        if (lesson.pdfName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "", tint = CyberPink, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(lesson.pdfName, fontSize = 12.sp, color = Color.White)
                                }

                                if (progress == 100) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.DoneOutline, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Saved", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box {
                                        if (progress in 1..99) {
                                            CircularProgressIndicator(
                                                progress = progress / 100f,
                                                color = CyberPurple,
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    viewModel.downloadPdfNotes(
                                                        courseId = selectedCourse?.id ?: 0,
                                                        fileUrl = lesson.pdfUrl,
                                                        fileName = lesson.pdfName
                                                    )
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Download, contentDescription = "Get PDF", tint = NeonCyan)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ==========================================
// 5. CUSTOM PREMIUM PLAYBACK VIEW WITH GESTURES
// ==========================================
@Composable
fun PremiumVideoPlayerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val activeLesson by viewModel.activeLesson.collectAsStateWithLifecycle()
    
    val speed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val volume by viewModel.playerVolume.collectAsStateWithLifecycle()
    val brightness by viewModel.playerBrightness.collectAsStateWithLifecycle()
    val fullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()

    var showControls by remember { mutableStateOf(true) }

    // Toggle controls off automatically after inactivity
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // App header bar in player
        if (!fullscreen) {
            OptTopAppBar(title = activeLesson?.title ?: "HD Lecture Player", onBack = onBack)
        }

        // Primary Player Video Box Frame with touch triggers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (fullscreen) 1f else 0.45f)
                .background(Color.DarkGray)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Sliding gesture on left screen slice maps strictly to Brightness control
                        if (change.position.x < size.width / 2) {
                            val newBrightness = (brightness + (dragAmount.y / (-400f))).coerceIn(0f, 1f)
                            viewModel.playerBrightness.value = newBrightness
                        } else {
                            // Sliding gesture on right screen slice maps strictly to Volume control
                            val newVolume = (volume + (dragAmount.y / (-400f))).coerceIn(0f, 1f)
                            viewModel.playerVolume.value = newVolume
                        }
                        showControls = true
                    }
                }
                .clickable { showControls = !showControls }
        ) {
            // Simulated Educational Video Visual Content
            Image(
                imageVector = Icons.Default.Tv,
                contentDescription = "",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center),
                alpha = 0.2f
            )

            // Dynamic Lecture title overlay
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "STUDYNOVA HIGH DEFINITION STREAM",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Playing: ${activeLesson?.title}",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Speed: ${speed}x | Audio Vol: ${(volume * 100).toInt()}% | Light: ${(brightness * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            // Quick Floating sliders overlay revealing volume/brightness changes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 50.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brightness Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.WbSunny, contentDescription = "", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${(brightness * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                }

                // Volume Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${(volume * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                }
            }

            // Controls Overlay GUI
            androidx.compose.animation.AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.4f))
                ) {
                    // Back out option in full screen
                    if (fullscreen) {
                        IconButton(
                            onClick = { viewModel.isFullscreen.value = false },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "", tint = Color.White)
                        }
                    }

                    // Centered big pause/play button
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(CyberPurple)
                            .align(Alignment.Center)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    // Speed controller, Fullscreen option
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playback Speed Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("SPEED: ", color = Color.LightGray, fontSize = 11.sp)
                            val speedsList = listOf(0.75f, 1.0f, 1.5f, 2.0f)
                            speedsList.forEach { s ->
                                Text(
                                    text = "${s}x",
                                    color = if (speed == s) NeonCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (speed == s) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .clickable { viewModel.playbackSpeed.value = s }
                                        .padding(horizontal = 6.dp)
                                )
                            }
                        }

                        // Full Screen Toggle button
                        IconButton(onClick = { viewModel.isFullscreen.value = !fullscreen }) {
                            Icon(
                                imageVector = if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Lessons playlist folder below player for compact screen layouts
        if (!fullscreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("NOW WATCHING: ${activeLesson?.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Course: ${selectedCourse?.title}", color = Color.LightGray, fontSize = 13.sp)
                    Divider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 12.dp))
                }

                selectedCourse?.let {
                    val list = viewModel.deserializeLessons(it.lessonsJson)
                    items(list) { les ->
                        val isActive = activeLesson?.id == les.id
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isActive) NeonCyan else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.changeActiveLesson(les) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.PlayCircleFilled else Icons.Default.PlayArrow,
                                    contentDescription = "",
                                    tint = if (isActive) NeonCyan else Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(les.title, color = if (isActive) NeonCyan else Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. INTERACTIVE SIMULATED LIVE CLASSROOM VIEW WITH EMITTED REACTIONS
// ==========================================
@Composable
fun LiveClassroomScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeLiveClass by viewModel.activeLiveClass.collectAsStateWithLifecycle()
    val chatMessages by viewModel.liveClassChat.collectAsStateWithLifecycle()
    val raisedHands by viewModel.raisedHands.collectAsStateWithLifecycle()
    
    val pollA by viewModel.pollVotesA.collectAsStateWithLifecycle()
    val pollB by viewModel.pollVotesB.collectAsStateWithLifecycle()
    val hasVoted by viewModel.hasVotedCurrentPoll.collectAsStateWithLifecycle()

    var chatTextInput by remember { mutableStateOf("") }
    val chatScroll = rememberScrollState()

    LaunchedEffect(chatMessages.size) {
        chatScroll.animateScrollTo(chatScroll.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060913))
    ) {
        OptTopAppBar(title = "LIVE CLASSROOM SCREEN", onBack = onBack)

        // Web streaming box mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Red)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIVE FEED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Stream, contentDescription = "", tint = Color.Red, modifier = Modifier.size(45.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(activeLiveClass?.title ?: "Academic Broadcast", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Teacher: ${activeLiveClass?.teacherName}", color = NeonCyan, fontSize = 12.sp)
            }
        }

        // Interaction overlays (Raise hand notification, active live poll trigger)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            // Interactive Classroom Panel (Poll simulator)
            Text(
                text = "INTERACTIVE CLASSROOM POLL",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = CyberPink,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CONCEPT-CHECK-01: Is Moment of Inertia dependent on mass density structure?", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.votePoll("A") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (hasVoted) Color.Gray else CyberPurple)
                        ) {
                            Text("Option A: Yes ($pollA votes)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.votePoll("B") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (hasVoted) Color.Gray else CyberPink)
                        ) {
                            Text("Option B: No ($pollB votes)", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live streaming chats row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "STUDENTS BROADCAST OVERLAY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                // Raise hand button
                val currentUserMail = viewModel.currentUser.value?.email ?: ""
                val didIRaiseHand = raisedHands.contains(currentUserMail)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (didIRaiseHand) Color.Yellow else Color.White.copy(0.1f))
                        .clickable { viewModel.raiseHand() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PanTool, contentDescription = "", tint = if (didIRaiseHand) Color.Black else Color.LightGray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (didIRaiseHand) "Submitting Hand Raised..." else "Raise Hand", fontSize = 11.sp, color = if (didIRaiseHand) Color.Black else Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat lists in scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(chatScroll)
                    .background(Color.White.copy(0.02f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chatMessages.forEach { log ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.senderName,
                                fontWeight = FontWeight.Bold,
                                color = if (log.senderRole == "teacher") CyberPink else NeonCyan,
                                fontSize = 12.sp
                            )
                            if (log.senderRole == "teacher") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CyberPink)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("FACULTY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                }
                            }
                        }
                        Text(log.message, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = chatTextInput,
                    onValueChange = { chatTextInput = it },
                    placeholder = { Text("Ask teacher...", color = Color.White.copy(0.4f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = Color.White.copy(0.1f),
                        unfocusedContainerColor = Color(0xFF0F122B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (chatTextInput.isNotBlank()) {
                            viewModel.sendLiveMessage(chatTextInput)
                            chatTextInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberPurple)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "", tint = Color.White)
                }
            }
        }
    }
}

// ==========================================
// 7. REALTIME MOCK TEST SOLVER INTERFACE
// ==========================================
@Composable
fun MockTestSolverScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeTest by viewModel.activeTest.collectAsStateWithLifecycle()
    val questions by viewModel.testQuestions.collectAsStateWithLifecycle()
    val currentIdx by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val answers by viewModel.testAnswers.collectAsStateWithLifecycle()
    val secRemaining by viewModel.testSecondsRemaining.collectAsStateWithLifecycle()
    val resultState by viewModel.testResult.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F122B))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(activeTest?.title ?: "Examination Screen", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                // Timer countdown format MM:SS
                val mins = secRemaining / 60
                val secs = secRemaining % 60
                val timerString = String.format("%02d:%02d", mins, secs)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = "", tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Time: $timerString",
                        color = if (secRemaining < 60) Color.Red else NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (resultState != null) {
                // Test result summary view
                TestResultSummaryView(result = resultState!!, onDone = {
                    viewModel.closeTestSession()
                    onBack()
                })
            } else {
                if (questions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Reading examination files...", color = Color.White)
                    }
                } else {
                    val q = questions[currentIdx]
                    val selectedAns = answers[currentIdx]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Progress bar
                            val progress = (currentIdx + 1).toFloat() / questions.size
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clip(CircleShape),
                                color = CyberPurple,
                                trackColor = Color.White.copy(0.1f)
                            )

                            Text(
                                text = "QUESTION ${currentIdx + 1} OF ${questions.size}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(q.questionText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                            Spacer(modifier = Modifier.height(24.dp))

                            // Options Checklist
                            OptionChoiceRow("A", q.optionA, selectedAns == 0) { viewModel.answerQuestion(currentIdx, 0) }
                            OptionChoiceRow("B", q.optionB, selectedAns == 1) { viewModel.answerQuestion(currentIdx, 1) }
                            OptionChoiceRow("C", q.optionC, selectedAns == 2) { viewModel.answerQuestion(currentIdx, 2) }
                            OptionChoiceRow("D", q.optionD, selectedAns == 3) { viewModel.answerQuestion(currentIdx, 3) }
                        }

                        // Bottom Actions row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.prevQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                                enabled = currentIdx > 0
                            ) {
                                Text("Previous")
                            }

                            if (currentIdx == questions.size - 1) {
                                Button(
                                    onClick = { viewModel.submitMockTest() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Submit Assessment", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.nextQuestion() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                                ) {
                                    Text("Next Question")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionChoiceRow(symbol: String, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x3E864AF9) else Color.White.copy(0.04f))
            .border(
                1.dp,
                if (isSelected) CyberPurple else Color.White.copy(0.1f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CyberPurple else Color.White.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun TestResultSummaryView(result: TestResult, onDone: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(60.dp))
            Text(result.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Assessment evaluated with precision", fontSize = 13.sp, color = Color.LightGray)
            
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color.White.copy(0.1f))
        }

        // Percentage Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL MARKS EARNED", fontSize = 12.sp, color = Color.LightGray)
                        Text("${result.score} / ${result.totalScore}", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 24.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14122C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${result.percentage}%", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        // Split stats
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3100F0FF))
                        .padding(10.dp)
                ) {
                    Text("Correct: ${result.correctCount}", color = Color.White, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3DFF52A2))
                        .padding(10.dp)
                ) {
                    Text("Wrong: ${result.incorrectCount}", color = Color.White, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(0.08f))
                        .padding(10.dp)
                ) {
                    Text("Skipped: ${result.unattemptedCount}", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Review Explanations lists
        item {
            Text("SATELLITE ANSWER SHEET EXPLANATION", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        items(result.details) { m ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(m.questionText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Selected: ${if (m.chosenOptionIndex == -1) "Skipped" else ('A' + m.chosenOptionIndex)}", color = if (m.chosenOptionIndex == m.correctOptionIndex) NeonCyan else CyberPink, fontSize = 12.sp)
                    Text("Correct Choice: ${('A' + m.correctOptionIndex)}", color = NeonCyan, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("System Explanation: " + m.explanation, color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        item {
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exit Summary View", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 8. OMEGA AI CONTROL PANEL & EXECUTOR
// ==========================================
@Composable
fun AdminCenterView(
    viewModel: MainViewModel,
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSuperAdmin = currentUser?.email == "dhinwabrothers125@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = "", tint = CyberPink, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(if (isSuperAdmin) "SUPREME AI CONTROL COMPLEX" else "FACULTY MANAGEMENT BOARD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                Text(if (isSuperAdmin) "Secured bypass | Globals broadcast authorized" else "Upload courses and review performance index", fontSize = 11.sp, color = Color.LightGray)
            }
        }

        Divider(color = Color.White.copy(0.1f))
        
        Spacer(modifier = Modifier.height(16.dp))

        if (isSuperAdmin) {
            // Large futuristic dashboard showing OMEGA AI Command trigger
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToScreen(AppScreen.SUPREME_AI_DASHBOARD) }
                    .border(1.dp, NeonCyan, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Adb, contentDescription = "", tint = NeonCyan, modifier = Modifier.size(45.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SUPREME AI AUTOPILOT COMMAND CENTER", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Deploy AI courses, MCQ question bundles, clean cache buffers via Natural Language models.", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap to activate Master Console >>", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Standard Management controls form
        Text("MANUAL CONTROLS", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                // Course Injection form
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Manual Course Injector", fontWeight = FontWeight.Bold, color = Color.White)
                        var courseTitleInput by remember { mutableStateOf("") }
                        var courseCatInput by remember { mutableStateOf("JEE") }
                        var courseDescInput by remember { mutableStateOf("") }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = courseTitleInput,
                            onValueChange = { courseTitleInput = it },
                            label = { Text("Course Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = courseCatInput,
                            onValueChange = { courseCatInput = it },
                            label = { Text("Stream Section (JEE, NEET, CUET)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = courseDescInput,
                            onValueChange = { courseDescInput = it },
                            label = { Text("Syllabus brief description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (courseTitleInput.isNotBlank()) {
                                    viewModel.adminCreateCourse(courseTitleInput, courseDescInput, courseCatInput, "", currentUser?.name ?: "Admin")
                                    courseTitleInput = ""
                                    courseDescInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Inject Course & Core Syllabus", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isSuperAdmin) {
                // Global Bulletins broadcaster
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Satellite Global Broadcaster", fontWeight = FontWeight.Bold, color = Color.White)
                            var titleInp by remember { mutableStateOf("") }
                            var bodyInp by remember { mutableStateOf("") }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = titleInp,
                                onValueChange = { titleInp = it },
                                label = { Text("Bulletin Brief Title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = bodyInp,
                                onValueChange = { bodyInp = it },
                                label = { Text("Detailed Bulletin message content") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (titleInp.isNotEmpty()) {
                                        viewModel.broadcastGlobalAlert(titleInp, bodyInp)
                                        titleInp = ""
                                        bodyInp = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dispatch Satellite Warning", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Add Faculty bypass account creator
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Add Custom Faculty credentials", fontWeight = FontWeight.Bold, color = Color.White)
                            var tName by remember { mutableStateOf("") }
                            var tMail by remember { mutableStateOf("") }
                            var tPass by remember { mutableStateOf("") }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = tName, onValueChange = { tName = it }, label = { Text("Instructor Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = tMail, onValueChange = { tMail = it }, label = { Text("Faculty Registered Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = tPass, onValueChange = { tPass = it }, label = { Text("Temporary Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (tMail.contains("@")) {
                                        viewModel.adminCreateTeacherRequest(tName, tMail, tPass)
                                        tName = ""
                                        tMail = ""
                                        tPass = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Insert Certified Instructor", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8A. OMEGA AI CONSOLE TERMINAL
// ==========================================
@Composable
fun OmegaConsoleScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val logs by viewModel.omegaConsoleOutput.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isOmegaProcessing.collectAsStateWithLifecycle()
    var rawTextCommand by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    LaunchedEffect(logs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        topBar = {
            OptTopAppBar(title = "SUPREME AI OS V5.0", onBack = onBack)
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Hacker terminal scroll window
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .background(Color(0xFF02040A), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonCyan.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                logs.forEach { line ->
                    Text(
                        text = line,
                        color = if (line.startsWith(">>")) Color.Green else if (line.startsWith("✅")) NeonCyan else if (line.startsWith("🤖")) CyberPink else Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                if (isProcessing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUPREME AI autopilot calculations compiler writing room...", color = NeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Presets recommendations panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TerminalCommandChip("Generate JEE Test", "Create test for JEE Mains Kinematics") { rawTextCommand = it }
                TerminalCommandChip("Alert: Class live", "Send announcement: JEE Rotational mechanics class is live now!") { rawTextCommand = it }
                TerminalCommandChip("Create UPSC Path", "Generate custom Course for UPSC history syllabus details") { rawTextCommand = it }
                TerminalCommandChip("Run Diagnostics", "SUPREME MODE ON") { rawTextCommand = it }
                TerminalCommandChip("Cleanup Cache", "AI CLEANUP") { rawTextCommand = it }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = rawTextCommand,
                    onValueChange = { rawTextCommand = it },
                    placeholder = { Text("Command (e.g. Generate UPSC Class test)", color = Color.White.copy(0.4f), fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.White.copy(0.2f),
                        focusedContainerColor = Color(0xFF030612)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (rawTextCommand.isNotBlank() && !isProcessing) {
                            viewModel.runOmegaCommand(rawTextCommand)
                            rawTextCommand = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CyberPurple)
                ) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = "", tint = Color.Green)
                }
            }
        }
    }
}

@Composable
fun TerminalCommandChip(label: String, cmdValue: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(0.06f))
            .clickable { onClick(cmdValue) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = NeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

// ==========================================
// CENTRAL REUSABLE COMPONENTS
// ==========================================
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(0.07f),
                        Color.White.copy(0.01f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(0.12f),
                            Color.White.copy(0.02f)
                        )
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptTopAppBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 17.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Leave Screen",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF060913),
            titleContentColor = Color.White
        )
    )
}
