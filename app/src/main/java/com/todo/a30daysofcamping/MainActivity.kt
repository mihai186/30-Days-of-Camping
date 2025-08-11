package com.todo.a30daysofcamping

import android.R.attr.onClick
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.todo.a30daysofcamping.DetailActivity
import com.todo.a30daysofcamping.R
import kotlinx.coroutines.launch
import kotlin.collections.all

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme() {
                val context = LocalContext.current
                val saved by ProgressStore.allAsFlow(context, 1..30)
                    .collectAsState(initial = emptyMap())

                LaunchedEffect(saved) {
                    if (saved.isNotEmpty()) {
                        DataSet.dataSet.forEach { item ->
                            item.checked.value = saved[item.day] ?: false
                        }
                    }
                }
                val listState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()
                val allItemsChecked by remember {
                    derivedStateOf {
                        var res = true
                        for (item in DataSet.dataSet) {
                            if (!item.checked.value) {
                                res = false
                                break
                            }
                        }
                        res
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
                                onItemClick = { index ->
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                })
                        }

                    ) { innerPadding ->
                        FadedList(
                            modifier = Modifier.padding(innerPadding),
                            content =
                                {
                                    DayItemsList(listState = listState)
                                }
                        )
                    }
                }
                if (showDialog) {
                    CompletionDialog(onDismiss = { showDialog = false })
                }

                LaunchedEffect(allItemsChecked) {
                    if (allItemsChecked) showDialog = true
                }
            }
        }
    }
}

@Composable
fun DayItemsList(modifier: Modifier = Modifier, listState: LazyListState) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
    ) {
        itemsIndexed(items = DataSet.dataSet) { index, item ->
            DayItem(item)
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
fun DayItem(item: ItemData = DataSet.dataSet.first()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var clicked by remember { mutableStateOf(false) }

    Card(
        onClick = { clicked = !clicked },
        colors = if (!item.checked.value) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        } else {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
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
                    Text(
                        text = "Day ${item.day}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                    )
                    Text(
                        text = stringResource(item.title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                    )
                }
                Checkbox(
                    checked = item.checked.value,
                    onCheckedChange = { isChecked ->
                        item.checked.value = isChecked
                        scope.launch { ProgressStore.setChecked(context, item.day, isChecked) }
                    },
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
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = 1.0f,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut()
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
                    ) {
                        Text("View details")
                    }
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
    modifier: Modifier = Modifier, onItemClick: (Int) -> Unit
) {
    Surface(
        color = Color.Transparent
    ) {
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
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable { onItemClick(i) }) {
                Image(
                    painter = painterResource(id = item.image),
                    contentDescription = stringResource(item.imageDescription),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (!item.checked.value) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                    Text(
                        text = "Day ${item.day}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    )
                    Box(
                        modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center
                    ) {
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
    AppTheme(darkTheme = false) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val allItemsChecked by remember {
            derivedStateOf {
                var res = true
                for (item in DataSet.dataSet) {
                    if (!item.checked.value) {
                        res = false
                        break
                    }
                }
                res
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
                        onItemClick = { index ->
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        })
                }

            ) { innerPadding ->
                FadedList(
                    modifier = Modifier.padding(innerPadding),
                    content =
                        {
                            DayItemsList(listState = listState)
                        }
                )
            }
        }
        if (showDialog) {
            CompletionDialog(onDismiss = { showDialog = false })
        }

        LaunchedEffect(allItemsChecked) {
            if (allItemsChecked) showDialog = true
        }
    }
}

data class ItemData(
    val image: Int,
    val day: Int,
    val imageDescription: Int,
    val title: Int,
    val description: Int,
    var checked: MutableState<Boolean> = mutableStateOf(false)
)

data class ItemContent(
    val day: Int,
    val fullDescription: Int,
    val image: Int? = null,
    val videoLink: String? = null
)

object DataSet {
    val dataSet: List<ItemData> = listOf(
        //partea 1
        ItemData(R.drawable.image1, 1, R.string.imgDesc1, R.string.title1, R.string.description1),
        ItemData(R.drawable.image2, 2, R.string.imgDesc2, R.string.title2, R.string.description2),
        ItemData(R.drawable.image3, 3, R.string.imgDesc3, R.string.title3, R.string.description3),
        ItemData(R.drawable.image4, 4, R.string.imgDesc4, R.string.title4, R.string.description4),
        ItemData(R.drawable.image5, 5, R.string.imgDesc5, R.string.title5, R.string.description5),
        ItemData(R.drawable.image6, 6, R.string.imgDesc6, R.string.title6, R.string.description6),
        ItemData(R.drawable.image7, 7, R.string.imgDesc7, R.string.title7, R.string.description7),
        ItemData(R.drawable.image8, 8, R.string.imgDesc8, R.string.title8, R.string.description8),
        //partea 2
        ItemData(R.drawable.image9, 9, R.string.imgDesc9, R.string.title9, R.string.description9),
        ItemData(
            R.drawable.image10,
            10,
            R.string.imgDesc10,
            R.string.title10,
            R.string.description10
        ),
        ItemData(
            R.drawable.image11,
            11,
            R.string.imgDesc11,
            R.string.title11,
            R.string.description11
        ),
        ItemData(
            R.drawable.image12,
            12,
            R.string.imgDesc12,
            R.string.title12,
            R.string.description12
        ),
        ItemData(
            R.drawable.image13,
            13,
            R.string.imgDesc13,
            R.string.title13,
            R.string.description13
        ),
        ItemData(
            R.drawable.image14,
            14,
            R.string.imgDesc14,
            R.string.title14,
            R.string.description14
        ),
        ItemData(
            R.drawable.image15,
            15,
            R.string.imgDesc15,
            R.string.title15,
            R.string.description15
        ),
        ItemData(
            R.drawable.image16,
            16,
            R.string.imgDesc16,
            R.string.title16,
            R.string.description16
        ),
        ItemData(
            R.drawable.image17,
            17,
            R.string.imgDesc17,
            R.string.title17,
            R.string.description17
        ),
        ItemData(
            R.drawable.image18,
            18,
            R.string.imgDesc18,
            R.string.title18,
            R.string.description18
        ),
        ItemData(
            R.drawable.image19,
            19,
            R.string.imgDesc19,
            R.string.title19,
            R.string.description19
        ),
        ItemData(
            R.drawable.image20,
            20,
            R.string.imgDesc20,
            R.string.title20,
            R.string.description20
        ),
        ItemData(
            R.drawable.image21,
            21,
            R.string.imgDesc21,
            R.string.title21,
            R.string.description21
        ),
        //partea 3
        ItemData(
            R.drawable.image22,
            22,
            R.string.imgDesc22,
            R.string.title22,
            R.string.description22
        ),
        ItemData(
            R.drawable.image23,
            23,
            R.string.imgDesc23,
            R.string.title23,
            R.string.description23
        ),
        ItemData(
            R.drawable.image24,
            24,
            R.string.imgDesc24,
            R.string.title24,
            R.string.description24
        ),
        ItemData(
            R.drawable.image25,
            25,
            R.string.imgDesc25,
            R.string.title25,
            R.string.description25
        ),
        ItemData(
            R.drawable.image26,
            26,
            R.string.imgDesc26,
            R.string.title26,
            R.string.description26
        ),
        ItemData(
            R.drawable.image27,
            27,
            R.string.imgDesc27,
            R.string.title27,
            R.string.description27
        ),
        ItemData(
            R.drawable.image28,
            28,
            R.string.imgDesc28,
            R.string.title28,
            R.string.description28
        ),
        ItemData(
            R.drawable.image29,
            29,
            R.string.imgDesc29,
            R.string.title29,
            R.string.description29
        ),
        ItemData(
            R.drawable.image30,
            30,
            R.string.imgDesc30,
            R.string.title30,
            R.string.description30
        )
    )

    val contentSet: List<ItemContent> = listOf(
        //partea 1
        ItemContent(1, R.string.fullDescription1),
        ItemContent(2, R.string.fullDescription2),
        ItemContent(3, R.string.fullDescription3),
        ItemContent(4, R.string.fullDescription4),
        ItemContent(5, R.string.fullDescription5),
        ItemContent(6, R.string.fullDescription6),
        ItemContent(7, R.string.fullDescription7),
        ItemContent(8, R.string.fullDescription8),
        //partea 2
        ItemContent(9, R.string.fullDescription9),
        ItemContent(10, R.string.fullDescription10),
        ItemContent(11, R.string.fullDescription11),
        ItemContent(12, R.string.fullDescription12),
        ItemContent(13, R.string.fullDescription13),
        ItemContent(14, R.string.fullDescription14),
        ItemContent(15, R.string.fullDescription15),
        ItemContent(16, R.string.fullDescription16),
        ItemContent(17, R.string.fullDescription17),
        ItemContent(18, R.string.fullDescription18),
        ItemContent(19, R.string.fullDescription19),
        ItemContent(20, R.string.fullDescription20),
        ItemContent(21, R.string.fullDescription21),
        //partea 3
        ItemContent(22, R.string.fullDescription22),
        ItemContent(23, R.string.fullDescription23),
        ItemContent(24, R.string.fullDescription24),
        ItemContent(25, R.string.fullDescription25),
        ItemContent(26, R.string.fullDescription26),
        ItemContent(27, R.string.fullDescription27),
        ItemContent(28, R.string.fullDescription28),
        ItemContent(29, R.string.fullDescription29),
        ItemContent(30, R.string.fullDescription30)
    )
}