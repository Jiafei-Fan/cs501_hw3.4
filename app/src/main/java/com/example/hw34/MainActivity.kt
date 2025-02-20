package com.example.hw34

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hw34.ui.theme.Hw34Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge layout for immersive UI experience
        enableEdgeToEdge()
        setContent {
            Hw34Theme {
                Scaffold { innerPadding ->
                    // Main container filling the entire screen with proper padding
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Display the infinite scrolling grid of numbers
                        InfiniteScrollingNumberGrid()
                    }
                }
            }
        }
    }
}

@Composable
fun InfiniteScrollingNumberGrid() {
    // Maintain a list of numbers, initially populated with values 1 to 30
    val numbers = remember { mutableStateListOf<Int>().apply { addAll(1..30) } }
    // Flag to indicate whether data is being loaded to prevent duplicate loading
    var isLoading by remember { mutableStateOf(false) }
    // Remember the state of the LazyVerticalGrid to monitor scroll position
    val gridState = rememberLazyGridState()

    // Launch a coroutine to observe scrolling state and load more numbers when reaching the bottom
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }
            .map { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val visibleItems = layoutInfo.visibleItemsInfo
                // Check if the last visible item's index is at or near the end of the list
                if (visibleItems.isNotEmpty()) {
                    visibleItems.last().index >= totalItems - 1
                } else {
                    false
                }
            }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                // When at the bottom and not currently loading, load the next batch of numbers
                if (!isLoading) {
                    isLoading = true
                    delay(1000) // Simulate network/data loading delay
                    val nextStart = numbers.size + 1
                    numbers.addAll(nextStart until (nextStart + 30))
                    isLoading = false
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Reset button to clear and reinitialize the number list
        Button(
            onClick = {
                numbers.clear()
                numbers.addAll(1..30)
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Reset")
        }

        // LazyVerticalGrid with 3 columns to display the numbers in a grid format
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            // Display each number in the grid
            items(numbers) { number ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(60.dp) // Fixed height (not square) to allow more rows visible on screen
                        .fillMaxWidth()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = number.toString())
                }
            }
            // If data is currently loading, display a full-width loading indicator at the bottom
            if (isLoading) {
                item(span = { GridItemSpan(3) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
