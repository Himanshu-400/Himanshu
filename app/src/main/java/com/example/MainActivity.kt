package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.StudyNovaApp
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    // Simple constructor injection state manager tracking user sessions, mock tests, and logs
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to Edge immersive visual rendering support
        enableEdgeToEdge()
        
        setContent {
            StudyNovaApp(viewModel = viewModel)
        }
    }
}
