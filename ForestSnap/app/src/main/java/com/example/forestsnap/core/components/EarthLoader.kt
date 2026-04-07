package com.example.forestsnap.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EarthLoader(modifier: Modifier = Modifier) {
    val waterColor = Color(0xFF3344C1)
    val landColor = Color(0xFF7CC133)

    // Infinite transition for our continuous animation
    val infiniteTransition = rememberInfiniteTransition(label = "earth_spin")

    // We animate a float from 0 to 1 over 5 seconds (matching your 5s CSS animation)
    val spinProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(120.dp) // Equivalent to your 7.5em
                .clip(CircleShape)
                .background(waterColor)
                .border(3.dp, Color.White, CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Parse the exact SVG paths you provided
                val path1 = PathParser().parsePathString("M29.4,-17.4C33.1,1.8,27.6,16.1,11.5,31.6C-4.7,47,-31.5,63.6,-43,56C-54.5,48.4,-50.7,16.6,-41,-10.9C-31.3,-38.4,-15.6,-61.5,-1.4,-61C12.8,-60.5,25.7,-36.5,29.4,-17.4Z").toPath()
                val path2 = PathParser().parsePathString("M31.7,-55.8C40.3,-50,45.9,-39.9,49.7,-29.8C53.5,-19.8,55.5,-9.9,53.1,-1.4C50.6,7.1,43.6,14.1,41.8,27.6C40.1,41.1,43.4,61.1,37.3,67C31.2,72.9,15.6,64.8,1.5,62.2C-12.5,59.5,-25,62.3,-31.8,56.7C-38.5,51.1,-39.4,37.2,-49.3,26.3C-59.1,15.5,-78,7.7,-77.6,0.2C-77.2,-7.2,-57.4,-14.5,-49.3,-28.4C-41.2,-42.4,-44.7,-63,-38.5,-70.1C-32.2,-77.2,-16.1,-70.8,-2.3,-66.9C11.6,-63,23.1,-61.5,31.7,-55.8Z").toPath()
                val path3 = PathParser().parsePathString("M30.6,-49.2C42.5,-46.1,57.1,-43.7,67.6,-35.7C78.1,-27.6,84.6,-13.8,80.3,-2.4C76.1,8.9,61.2,17.8,52.5,29.1C43.8,40.3,41.4,53.9,33.7,64C26,74.1,13,80.6,2.2,76.9C-8.6,73.1,-17.3,59,-30.6,52.1C-43.9,45.3,-61.9,45.7,-74.1,38.2C-86.4,30.7,-92.9,15.4,-88.6,2.5C-84.4,-10.5,-69.4,-20.9,-60.7,-34.6C-52.1,-48.3,-49.8,-65.3,-40.7,-70C-31.6,-74.8,-15.8,-67.4,-3.2,-61.8C9.3,-56.1,18.6,-52.3,30.6,-49.2Z").toPath()
                val path4 = PathParser().parsePathString("M39.4,-66C48.6,-62.9,51.9,-47.4,52.9,-34.3C53.8,-21.3,52.4,-10.6,54.4,1.1C56.3,12.9,61.7,25.8,57.5,33.2C53.2,40.5,39.3,42.3,28.2,46C17,49.6,8.5,55.1,1.3,52.8C-5.9,50.5,-11.7,40.5,-23.6,37.2C-35.4,34,-53.3,37.5,-62,32.4C-70.7,27.4,-70.4,13.7,-72.4,-1.1C-74.3,-15.9,-78.6,-31.9,-73.3,-43C-68.1,-54.2,-53.3,-60.5,-39.5,-60.9C-25.7,-61.4,-12.9,-56,1.1,-58C15.1,-59.9,30.2,-69.2,39.4,-66Z").toPath()

                // Calculate the wrapping translation (moving from right to left)
                // We use spinProgress to sweep the paths across the circle
                val offsetX1 = (1f - spinProgress) * (canvasWidth * 2) - (canvasWidth / 2)
                val offsetX2 = ((1f - spinProgress + 0.5f) % 1f) * (canvasWidth * 2) - (canvasWidth / 2)

                // Scale the paths slightly down to fit beautifully in our circle
                val scaleFactor = 0.8f 

                // Draw Group 1 (round1 equivalents)
                translate(left = offsetX1, top = canvasHeight / 2) {
                    drawPath(path = path1, color = landColor)
                    drawPath(path = path2, color = landColor)
                }

                // Draw Group 2 (round2 equivalents offset by half the cycle)
                translate(left = offsetX2, top = canvasHeight / 2) {
                    drawPath(path = path3, color = landColor)
                    drawPath(path = path4, color = landColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Updated text to "Loading..." as requested
        Text(
            text = "Loading...",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
        )
    }
}