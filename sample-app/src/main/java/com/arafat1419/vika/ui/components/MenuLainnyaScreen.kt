package com.arafat1419.vika.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arafat1419.vika.R

@Composable
fun MenuLainnyaContent(
    onMenuClick: (GridMenuItem) -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        MenuLainnyaTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Menu Pilihanmu Section
            MenuSection(
                title = stringResource(R.string.section_menu_pilihanmu),
                showEdit = true,
                onEditClick = onEditClick
            )

            MenuPilihanmuGrid(onMenuClick = onMenuClick)

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Lainnya Section
            MenuSection(
                title = stringResource(R.string.section_menu_lainnya),
                showEdit = false,
                onEditClick = {}
            )

            MenuLainnyaGrid(onMenuClick = onMenuClick)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuLainnyaTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color(0xFF212121)
            )
        }

        Text(
            text = stringResource(R.string.menu_lainnya_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun MenuSection(
    title: String,
    showEdit: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        if (showEdit) {
            TextButton(onClick = onEditClick) {
                Text(
                    text = stringResource(R.string.edit),
                    color = Color(0xFF0066CC),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MenuPilihanmuGrid(
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(menuItems) { item ->
            MenuGridItem(item = item, onClick = { onMenuClick(item) })
        }
    }
}

@Composable
fun getMenuLainnyaItems(): List<GridMenuItem> {
    return listOf(
        GridMenuItem(
            13,
            stringResource(R.string.menu_health_screening),
            Icons.Filled.HealthAndSafety
        ),
        GridMenuItem(
            14,
            stringResource(R.string.menu_service_registration),
            Icons.Filled.AppRegistration
        ),
        GridMenuItem(15, stringResource(R.string.menu_bed_availability), Icons.Filled.Bed),
        GridMenuItem(
            16,
            stringResource(R.string.menu_surgery_schedule),
            Icons.Filled.MedicalServices
        ),
        GridMenuItem(17, stringResource(R.string.menu_contribution_info), Icons.Filled.Receipt),
        GridMenuItem(18, stringResource(R.string.menu_auto_debit), Icons.Filled.AutoMode),
        GridMenuItem(19, stringResource(R.string.menu_payment_history), Icons.Filled.History),
        GridMenuItem(
            20,
            stringResource(R.string.menu_virtual_account),
            Icons.Filled.AccountBalance
        ),
        GridMenuItem(21, stringResource(R.string.menu_medication), Icons.Filled.Medication),
        GridMenuItem(22, stringResource(R.string.menu_disease_trend), Icons.Filled.ShowChart),
        GridMenuItem(23, stringResource(R.string.menu_mobile_bpjs), Icons.Filled.LocalShipping),
        GridMenuItem(24, stringResource(R.string.menu_ecommerce), Icons.Filled.ShoppingCart)
    )
}

@Composable
fun MenuLainnyaGrid(
    onMenuClick: (GridMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = getMenuLainnyaItems()

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 500.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(menuItems) { item ->
            MenuGridItem(item = item, onClick = { onMenuClick(item) })
        }
    }
}
