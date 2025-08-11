package com.todo.a30daysofcamping

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.todo.a30daysofcamping.ui.theme.AppTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.collections.all

class MainActivity : BaseFullscreenActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(){
    val context = LocalContext.current
    val checked = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { MutableList(30) { false }.toMutableStateList() }

    val saved by ProgressStore.allAsFlow(context, 1..30)
        .collectAsState(initial = emptyMap())

    LaunchedEffect(saved) {
        if (saved.isNotEmpty()) {
            for (day in 1..30) {
                val v = saved[day] ?: false
                if (checked[day - 1] != v) checked[day - 1] = v
            }
        }
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val allItemsChecked by remember {
        derivedStateOf {
            checked.all { it }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        if(isSystemInDarkTheme()){
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f))
            )
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.image_banner_height))
                )
                BannerBar()
            },
            bottomBar = {
                DayCarousel(
                    isChecked = { day -> checked[day - 1] },
                    onItemClick = { index ->
                        coroutineScope.launch { listState.animateScrollToItem(index) }
                    }
                )
            }

        ) { innerPadding ->
            FadedList(modifier = Modifier.padding(innerPadding)) {
                DayItemsList(
                    listState = listState,
                    isChecked = { day -> checked[day - 1] },
                    onCheckedChange = { day, value ->
                        checked[day - 1] = value
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            ProgressStore.setChecked(context, day, value)
                        }
                    }
                )
            }
        }
    }
    if (showDialog) {
        CompletionDialog(onDismiss = { showDialog = false })
    }

    LaunchedEffect(allItemsChecked) {
        if (allItemsChecked) showDialog = true
    }
}

@Composable
fun DayItemsList(
    listState: LazyListState,
    isChecked: (Int) -> Boolean,
    onCheckedChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
    ) {
        items(
            items = DataSet.dataSet,
            key = { item -> item.day },
            contentType = {"dayItem"}
        ) { item ->
            DayItem(
                item = item,
                checked = isChecked(item.day),
                onCheckedChange = { onCheckedChange(item.day, it) }
            )
        }
    }
}


@Composable
fun FadedList(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.05f to Color.Black,
                            0.95f to Color.Black,
                            1.0f to Color.Transparent
                        )
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
    ) {
        content()
    }
}

@Composable
fun DayItem(
    item: ItemData,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var clicked by remember { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue = if (!checked) {
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        },
        animationSpec = tween(durationMillis = 500)
    )

    val contentColor by animateColorAsState(
        targetValue = if (!checked) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(durationMillis = 500)
    )

    val colors = CardDefaults.elevatedCardColors(
        containerColor = containerColor,
        contentColor = contentColor
    )

    Card(
        onClick = { clicked = !clicked },
        colors = colors,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .padding(vertical = dimensionResource(R.dimen.padding_medium))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Column(Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))) {
                    Text("Day ${item.day}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                    )
                    Text(stringResource(item.title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                    )
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.primary)
                )
            }

            Image(
                painter = painterResource(item.image),
                contentDescription = stringResource(item.imageDescription),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.image_large))
                    .clip(MaterialTheme.shapes.extraLarge)
            )

            AnimatedVisibility(
                visible = clicked,
                enter = expandVertically(spring(0.8f, Spring.StiffnessLow)) + fadeIn(),
                exit  = shrinkVertically(spring(1.0f, Spring.StiffnessMedium)) + fadeOut()
            ) {
                Column {
                    Text(
                        text = stringResource(item.description),
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                        lineHeight = 26.sp
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        onClick = {
                            val intent = Intent(context, DetailActivity::class.java)
                            intent.putExtra("day", item.day)
                            context.startActivity(intent)
                        }
                    ) { Text("View details") }
                }
            }
        }
    }
}



@Composable
fun BannerBar(modifier: Modifier = Modifier) {
    Surface(
        color = Color.Transparent
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.padding_small))
                .height(dimensionResource(R.dimen.image_banner_height)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.banner),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayCarousel(
    //modifier: Modifier = Modifier,
    isChecked: (Int) -> Boolean,
    onItemClick: (Int) -> Unit
) {
    Surface(color = Color.Transparent) {
        HorizontalUncontainedCarousel(
            state = rememberCarouselState { DataSet.dataSet.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            itemWidth = 106.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) { i ->
            val item = DataSet.dataSet[i]
            val checked = isChecked(item.day)

            Box(
                modifier = Modifier
                    .height(70.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable { onItemClick(i) }
            ) {
                Image(
                    painter = painterResource(id = item.image),
                    contentDescription = stringResource(item.imageDescription),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                val overlayColor by animateColorAsState(
                    targetValue = if (!checked)
                        Color.Black.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    animationSpec = tween(durationMillis = 250),
                    label = "overlayColor"
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(overlayColor)
                )

                AnimatedContent(
                    targetState = checked,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(initialScale = 0.5f, animationSpec = tween(180))) togetherWith
                                fadeOut(tween(150))
                    },
                    label = "centerSwap",
                    modifier = Modifier.matchParentSize()
                ) { isChecked ->
                    Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        if (!isChecked) {
                            Text(
                                text = "Day ${item.day}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.checkmark),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                                modifier = Modifier.height(dimensionResource(R.dimen.image_avatar_small))
                            )
                        }
                    }
                }
            }

        }
    }
}



@Composable
fun CompletionDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_large)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 Congratulations!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .heightIn(max = dimensionResource(R.dimen.image_large))
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.completionMessage),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return")
                }
            }
        }
    }
}


@Preview
@Composable
fun AppPreview() {
    AppTheme(darkTheme = true) {
        MainScreen()
    }
}