package com.arafat1419.vika.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arafat1419.vika.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeTopBar(
    onNotificationClick: () -> Unit,
    onCSClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.notification_desc),
                    tint = Color(0xFF616161)
                )
            }

            IconButton(
                onClick = onCSClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Filled.Headset,
                    contentDescription = stringResource(R.string.cs_desc),
                    tint = Color(0xFF616161)
                )
            }
        }
    }
}

@Composable
fun UserGreetingCard(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.greeting_hi),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.member_status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = stringResource(R.string.version, "1.0.0"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QueueCard(
    onTakeQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.antrean_online),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.queue_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0066CC)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.queue_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onTakeQueueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0066CC),
                                    Color(0xFF0052A3)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = stringResource(R.string.queue_button),
                        color = Color.White
                    )
                }
            }
        }
    }
}

data class GridMenuItem(
    val id: Int,
    val title: String,
    val iconRes: Int,
    val hasNewBadge: Boolean = false
)

@Composable
fun getGridMenuItems(): List<GridMenuItem> {
    return listOf(
        GridMenuItem(1, stringResource(R.string.menu_info_program), R.drawable.info_program_jkn),
        GridMenuItem(2, stringResource(R.string.menu_telehealth), R.drawable.telehealth),
        GridMenuItem(
            3,
            stringResource(R.string.menu_service_history),
            R.drawable.info_riwayat_pelayanan
        ),
        GridMenuItem(4, stringResource(R.string.menu_bugar), R.drawable.bugar),
        GridMenuItem(5, stringResource(R.string.menu_rehab), R.drawable.rehap),
        GridMenuItem(
            6,
            stringResource(R.string.menu_add_participant),
            R.drawable.penambahan_peserta
        ),
        GridMenuItem(7, stringResource(R.string.menu_participant_info), R.drawable.info_peserta),
        GridMenuItem(8, stringResource(R.string.menu_sos), R.drawable.sos),
        GridMenuItem(
            9,
            stringResource(R.string.menu_facility_location),
            R.drawable.info_lokasi_faskes
        ),
        GridMenuItem(
            10,
            stringResource(R.string.menu_data_change),
            R.drawable.perubahan_data_peserta
        ),
        GridMenuItem(11, stringResource(R.string.menu_complaint), R.drawable.pengaduan_layanan),
        GridMenuItem(12, stringResource(R.string.menu_other), R.drawable.menu_lainnya)
    )
}

@Composable
fun MenuGrid(
    onMenuClick: (GridMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = getGridMenuItems()

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 400.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menuItems) { item ->
            MenuGridItem(item = item, onClick = { onMenuClick(item) })
        }
    }
}

@Composable
fun MenuGridItem(
    item: GridMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }

            if (item.hasNewBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red)
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Baru",
                        fontSize = 7.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.height(26.dp)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun InfiniteSlider(modifier: Modifier = Modifier) {
    val sliderItems = List(6) { index ->
        SliderItem(
            id = index,
            title = stringResource(R.string.slider_item, index + 1),
            color = when (index % 6) {
                0 -> Color(0xFF4CAF50)
                1 -> Color(0xFF2196F3)
                2 -> Color(0xFFFF9800)
                3 -> Color(0xFF9C27B0)
                4 -> Color(0xFFE91E63)
                else -> Color(0xFF00BCD4)
            }
        )
    }

    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % sliderItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            count = sliderItems.size,
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            itemSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { page ->
            SliderItemCard(item = sliderItems[page])
        }
    }
}

data class SliderItem(
    val id: Int,
    val title: String,
    val color: Color
)

@Composable
fun SliderItemCard(
    item: SliderItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(item.color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
