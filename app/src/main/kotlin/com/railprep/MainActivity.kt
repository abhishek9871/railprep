package com.railprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.railprep.core.design.RailPrepTheme
import com.railprep.navigation.RailPrepNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RailPrepTheme {
                val navController = rememberNavController()
                RailPrepNavGraph(navController = navController)
            }
        }
    }
}
