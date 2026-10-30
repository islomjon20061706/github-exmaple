
package com.example.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

// MAIN NAVIGATION

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableIntStateOf(0) }

    when (currentScreen) {
        0 -> HomeScreen(onSelect = { currentScreen = it })
        1 -> SplashLabScreen(onBack = { currentScreen = 0 })
        2 -> SnackbarLabScreen(onBack = { currentScreen = 0 })
        3 -> LifecycleLabScreen(onBack = { currentScreen = 0 })
        4 -> AnalyticsLabScreen(onBack = { currentScreen = 0 })
        5 -> ScrollLabScreen(onBack = { currentScreen = 0 })
        6 -> ProduceStateScreen(onBack = { currentScreen = 0 })
    }
}

@Composable
fun HomeScreen(onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Side-effects Lab",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Choose a task:")

        for (number in 1..6) {
            Button(
                onClick = { onSelect(number) }
            ) {
                Text("Task $number")
            }
        }
    }
}

// TASK 1: LaunchedEffect + rememberUpdatedState

@Composable
fun SplashLabScreen(onBack: () -> Unit) {
    val version = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            version.intValue++
        }
    }

    val currentVersion = version.intValue

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Button(onClick = onBack) { Text("Back to Home") }

        Text("Task 1: Splash Screen", style = MaterialTheme.typography.headlineSmall)
        Text("Current version: $currentVersion")

        CapturedLandingScreen {
            Log.d("SplashLab", "Captured callback: v$currentVersion")
        }

        UpdatedLandingScreen {
            Log.d("SplashLab", "Updated callback: v$currentVersion")
        }
    }
}

@Composable
fun CapturedLandingScreen(onTimeout: () -> Unit) {
    Text("Direct callback: waiting 2 seconds...")

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }
}

@Composable
fun UpdatedLandingScreen(onTimeout: () -> Unit) {
    val currentOnTimeout = rememberUpdatedState(onTimeout)

    Text("Updated callback: waiting 2 seconds...")

    LaunchedEffect(Unit) {
        delay(2000)
        currentOnTimeout.value()
    }

    // LaunchedEffect(onTimeout) would restart the 2-second delay
    // whenever the callback changes.
}

// TASK 2: Snackbar + rememberCoroutineScope

@Composable
fun SnackbarLabScreen(onBack: () -> Unit) {
    var showOtherScreen by remember { mutableStateOf(false) }

    if (showOtherScreen) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Another Screen", style = MaterialTheme.typography.headlineSmall)
            Text("You are on another screen.")

            Button(onClick = { showOtherScreen = false }) {
                Text("Go Back")
            }
        }
    } else {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(onClick = onBack) { Text("Back to Home") }

                Text("Task 2: Snackbar", style = MaterialTheme.typography.headlineSmall)

                Button(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Hello! This is a Snackbar."
                            )
                        }
                    }
                ) {
                    Text("Show Snackbar")
                }

                Button(onClick = { showOtherScreen = true }) {
                    Text("Go to another screen")
                }
            }
        }
    }
}

// TASK 3: DisposableEffect + Lifecycle

@Composable
fun LifecycleLabScreen(onBack: () -> Unit) {
    var showLogger by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Button(onClick = onBack) { Text("Back to Home") }

        Text("Task 3: Lifecycle Logger", style = MaterialTheme.typography.headlineSmall)

        Text("Show Lifecycle Logger")

        Switch(
            checked = showLogger,
            onCheckedChange = { showLogger = it }
        )

        if (showLogger) {
            LifecycleLogger()
        } else {
            Text("Lifecycle Logger is hidden.")
        }
    }
}

@Composable
fun LifecycleLogger() {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START ->
                    Log.d("LifecycleLab", "ON_START")

                Lifecycle.Event.ON_STOP ->
                    Log.d("LifecycleLab", "ON_STOP")

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        Log.d("LifecycleLab", "Observer added")

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d("LifecycleLab", "Observer removed")
        }
    }
}

// TASK 4: SideEffect + Analytics

object Analytics {
    var userType: String = "Unknown"
    var sideEffectCount: Int = 0
}

@Composable
fun AnalyticsLabScreen(onBack: () -> Unit) {
    var userType by remember { mutableStateOf("Guest") }

    SideEffect {
        Analytics.userType = userType
        Analytics.sideEffectCount++

        Log.d(
            "AnalyticsLab",
            "SideEffect count: ${Analytics.sideEffectCount}, userType: ${Analytics.userType}"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Button(onClick = onBack) { Text("Back to Home") }

        Text("Task 4: Analytics", style = MaterialTheme.typography.headlineSmall)

        Text("Current user type: $userType")
        Text("Analytics user type: ${Analytics.userType}")

        Button(onClick = { userType = "Student" }) {
            Text("Set Student")
        }

        Button(onClick = { userType = "Premium" }) {
            Text("Set Premium")
        }

        Button(onClick = { userType = "Guest" }) {
            Text("Set Guest")
        }
    }
}

// TASK 5: derivedStateOf + snapshotFlow

object ScrollAnalytics {
    var recompositionCount: Int = 0
}

@Composable
fun ScrollLabScreen(onBack: () -> Unit) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    SideEffect {
        ScrollAnalytics.recompositionCount++

        Log.d(
            "ScrollLab",
            "Recomposition count: ${ScrollAnalytics.recompositionCount}"
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }
            .map { index -> index > 0 }
            .distinctUntilChanged()
            .filter { hasScrolled -> hasScrolled }
            .collect {
                Log.d("ScrollLab", "User scrolled past the first item")
            }
    }

    val itemsList = remember {
        (1..100).map { "Item $it" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Button(onClick = onBack) { Text("Back to Home") }

            Text("Task 5: Scroll Lab", style = MaterialTheme.typography.headlineSmall)

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(itemsList) { item ->
                    Text(item, modifier = Modifier.padding(12.dp))
                }
            }
        }

        if (showButton) {
            Button(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Text("Back to Top")
            }
        }
    }
}

// TASK 6: produceState

sealed class LoadResult {
    data object Loading : LoadResult()
    data class Success(val fileName: String) : LoadResult()
    data class Error(val message: String) : LoadResult()
}

object FakeImageRepository {
    suspend fun loadImage(fileName: String): LoadResult {
        delay(500)

        return if (fileName.endsWith(".png", ignoreCase = true)) {
            LoadResult.Success(fileName)
        } else {
            LoadResult.Error("Only PNG files are supported!")
        }
    }
}

@Composable
fun ProduceStateScreen(onBack: () -> Unit) {
    var fileName by remember { mutableStateOf("photo.png") }

    val result by produceState<LoadResult>(
        initialValue = LoadResult.Loading,
        key1 = fileName
    ) {
        value = LoadResult.Loading

        Log.d("ProduceStateLab", "Loading started: $fileName")

        try {
            value = FakeImageRepository.loadImage(fileName)
            Log.d("ProduceStateLab", "Loading finished: $fileName")
        } catch (e: CancellationException) {
            Log.d("ProduceStateLab", "Loading cancelled: $fileName")
            throw e
        } finally {
            Log.d("ProduceStateLab", "Finally executed: $fileName")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Button(onClick = onBack) { Text("Back to Home") }

        Text("Task 6: produceState", style = MaterialTheme.typography.headlineSmall)

        Text("File name: $fileName")

        when (val currentResult = result) {
            is LoadResult.Loading -> Text("Loading...")
            is LoadResult.Success -> Text("Success: ${currentResult.fileName}")
            is LoadResult.Error -> Text("Error: ${currentResult.message}")
        }

        Button(onClick = { fileName = "photo.png" }) {
            Text("Load PNG")
        }

        Button(onClick = { fileName = "document.pdf" }) {
            Text("Load PDF")
        }
    }
}
