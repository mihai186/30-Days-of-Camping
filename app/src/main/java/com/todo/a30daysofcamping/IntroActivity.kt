package com.todo.a30daysofcamping

import android.R.attr.maxHeight
import android.R.attr.onClick
import android.R.attr.text
import android.R.id.primary
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todo.a30daysofcamping.ui.theme.AppTheme
import java.nio.file.WatchEvent

class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IntroPage()
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun IntroPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var imageTopPx by remember { mutableFloatStateOf(0f) }
    var imageBottomPx by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val gradient = Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF497178),
                (imageTopPx / screenHeightPx).coerceIn(0f, 1f) to Color(0xFFa1b6b2),
                (imageBottomPx / screenHeightPx).coerceIn(0f, 1f) to Color(0xFF2E4A23),
                1.0f to Color(0xFF1f2916)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
                .padding(dimensionResource(R.dimen.padding_large)),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier)
            Image(
                painter = painterResource(R.drawable.intro),
                contentDescription = "Camping Intro",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInRoot()
                        imageTopPx = position.y
                        imageBottomPx = position.y + coordinates.size.height
                    }
            )
            Column {
                Text(
                    text = "Find a new rhythm in nature and become part of a growing outdoor community.\n" +
                            "Thousands have completed the challenge — the forest is waiting for you.",
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 30.sp,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.onSurface,
                            offset = Offset(3f, 3f),
                            blurRadius = 8f
                        )
                    )
                )
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        if (context is Activity) context.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = dimensionResource(R.dimen.padding_xlarge)),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = "Begin your journey!",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun IntroPreview(){
    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            IntroPage()
        }
    }
}