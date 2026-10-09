package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.ui.viewmodel.PhoneViewModel
import com.example.ui.viewmodel.UiImportItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PhoneViewModel = viewModel()) {
    val items by viewModel.uiItems.collectAsState()
    val calledHistory by viewModel.calledHistory.collectAsState()
    val importError by viewModel.importError.collectAsState()
    val importSuccess by viewModel.importSuccessMessage.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var searchQueries by remember { mutableStateOf("") }
    var dialTargetNumber by remember { mutableStateOf<String?>(null) }
    var alreadyCalledWarningNumber by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Trigger phone calling flow safely
    val performDial = { number: String ->
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(dialIntent)
            viewModel.markAsCalled(number)
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در برقراری تماس: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Force RTL local layout direction for user requested Farsi application layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Pasar Dialer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = DarkBlueText,
                                        letterSpacing = (-0.01).sp
                                    )
                                    Text(
                                        "مدیریت و هماهنگ‌سازی تماس‌ها",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightBlueTertiary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        },
                        actions = {
                            // Compact rounded-full Import Button
                            Button(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.testTag("import_button"),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("وارد کردن JSON", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            IconButton(
                                onClick = { viewModel.clearCalledHistory() },
                                modifier = Modifier.testTag("reset_history_button")
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "پاک کردن تاریخچه تماس‌ها",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (items.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.clearAllImported() },
                                    modifier = Modifier.testTag("clear_imported_button")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "پاک کردن مخاطبین",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Divider(color = Blue100Border, thickness = 1.dp)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Toast Alerts
                LaunchedEffect(importError) {
                    importError?.let {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        viewModel.clearMessages()
                    }
                }
                LaunchedEffect(importSuccess) {
                    importSuccess?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessages()
                    }
                }

                // Geometric Balance Stats Ribbon bar
                val totalCount = items.size
                val calledCount = items.count { it.isCalled }
                val pendingCount = totalCount - calledCount
                val callLaterCount = items.count { it.isCallLater }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LightBlueRibbon,
                    border = BorderStroke(1.dp, Blue100Border.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Records
                        StatCard(
                            title = "کل شماره‌ها",
                            value = totalCount.toString(),
                            color = LightBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        // Pending
                        StatCard(
                            title = "زنگ نزده",
                            value = pendingCount.toString(),
                            color = LightBlueTertiary,
                            modifier = Modifier.weight(1f)
                        )
                        // Called
                        StatCard(
                            title = "زنگ زده",
                            value = calledCount.toString(),
                            color = RedCalled,
                            modifier = Modifier.weight(1f)
                        )
                        // Call Later
                        StatCard(
                            title = "دوباره زنگ بزن",
                            value = callLaterCount.toString(),
                            color = Color(0xFFD97706), // Amber-600 indicator
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Category list Tab Row
                val tabHeaders = listOf("همه", "زنگ نزده", "زنگ زده", "دوباره زنگ بزن", "تاریخچه تماس")
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 12.dp,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { Divider(color = SlateOutline.copy(alpha = 0.5f), thickness = 1.dp) }
                ) {
                    tabHeaders.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Search Filter Input
                if (items.isNotEmpty() && selectedTab != 4) {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    OutlinedTextField(
                        value = searchQueries,
                        onValueChange = { searchQueries = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("search_text_input"),
                        placeholder = { Text("فیلتر شماره، ویژگی‌ها، شهرها و...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQueries.isNotEmpty()) {
                                IconButton(onClick = { searchQueries = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                    )
                }

                // Main Display List
                if (selectedTab == 4) {
                    // History Log tab view
                    if (calledHistory.isEmpty()) {
                        EmptyStateView(
                            isHistory = true,
                            isListEmpty = true,
                            onImportClick = { showImportDialog = true }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            items(calledHistory, key = { it.phoneNumber + "_" + it.calledAt }) { record ->
                                HistoryRecordItem(
                                    phoneNumber = record.phoneNumber,
                                    calledAt = record.calledAt,
                                    onDialClick = { dialTargetNumber = record.phoneNumber },
                                    onDeleteClick = { viewModel.removeCalledHistory(record.phoneNumber) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                } else {
                    // Filter inputs based on Tab selection plus Search text
                    val currentTabItems = when (selectedTab) {
                        1 -> items.filter { !it.isCalled } // زنگ نزده
                        2 -> items.filter { it.isCalled }  // زنگ زده
                        3 -> items.filter { it.isCallLater } // دوباره زنگ بزن
                        else -> items // همه
                    }

                    val filteredItems = currentTabItems.filter { item ->
                        searchQueries.isBlank() ||
                        item.phoneNumber.contains(searchQueries, ignoreCase = true) ||
                        item.otherInfo.values.any { value -> value.contains(searchQueries, ignoreCase = true) } ||
                        item.otherInfo.keys.any { key -> key.contains(searchQueries, ignoreCase = true) }
                    }

                    if (filteredItems.isEmpty()) {
                        EmptyStateView(
                            isHistory = false,
                            isListEmpty = items.isEmpty(),
                            onImportClick = { showImportDialog = true }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .testTag("phone_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                            items(filteredItems, key = { it.id }) { item ->
                                PhoneRecordItem(
                                    item = item,
                                    onToggleCallLater = { checked ->
                                        viewModel.toggleCallLater(item.phoneNumber, checked)
                                    },
                                    onClick = {
                                        if (item.isCalled) {
                                            alreadyCalledWarningNumber = item.phoneNumber
                                        } else {
                                            dialTargetNumber = item.phoneNumber
                                        }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }

        // Dial Dialog Check
        dialTargetNumber?.let { num ->
            AlertDialog(
                onDismissRequest = { dialTargetNumber = null },
                icon = { Icon(Icons.Default.Call, contentDescription = "تماس با شماره", tint = MaterialTheme.colorScheme.primary) },
                title = { Text("برقراری تماس تلفنی") },
                text = { Text("آیا مایل به تماس با شماره $num هستید؟ این اقدام در تاریخچه ثبت خواهد شد تا از تماس‌های تکراری پیشگیری شود.") },
                confirmButton = {
                    Button(
                        onClick = {
                            dialTargetNumber = null
                            performDial(num)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تماس برقرار شود")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { dialTargetNumber = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                }
            )
        }

        // Called Warning Safeguard Dialog
        alreadyCalledWarningNumber?.let { num ->
            AlertDialog(
                onDismissRequest = { alreadyCalledWarningNumber = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = "هشدار تماس تکراری", tint = RedCalled) },
                title = { Text("هشدار تماس تکراری", color = RedCalled) },
                text = { Text("شما قبلاً با شماره $num تماس گرفته‌اید! تماس مجدد ممکن است تکراری باشد. آیا تمایل دارید این هشدار ایمنی را نادیده گرفته و دوباره شماره‌گیری کنید؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            alreadyCalledWarningNumber = null
                            performDial(num)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedCalled, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("شماره‌گیری مجدد")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { alreadyCalledWarningNumber = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف ایمن")
                    }
                }
            )
        }

        // Import Dialog overlay
        if (showImportDialog) {
            ImportUiDialog(
                onDismiss = { showImportDialog = false },
                onImportAction = { rawContent ->
                    showImportDialog = false
                    viewModel.importDataJson(rawContent)
                }
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Blue100Border),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LightBlueSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneRecordItem(
    item: UiImportItem,
    onToggleCallLater: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val cardBorderColor = if (item.isCalled) Red100Border else if (item.isCallLater) Color(0xFFFDE8E8) else Blue100Border
    val accentIndicatorColor = if (item.isCalled) RedIndicator else if (item.isCallLater) Color(0xFFF59E0B) else LightBlueIndicator

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("phone_record_card_${item.phoneNumber}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left decorative bar
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(accentIndicatorColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.phoneNumber,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlueText,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Badge Status indicator
                            val badgeBg = if (item.isCalled) Color(0xFFFEE2E2) else if (item.isCallLater) Color(0xFFFEF3C7) else Color(0xFFEFF6FF)
                            val badgeText = if (item.isCalled) "زنگ زده" else if (item.isCallLater) "به تعویق افتاده" else "پاسخ داده نشده"
                            val badgeColor = if (item.isCalled) Color(0xFFEF4444) else if (item.isCallLater) Color(0xFFD97706) else Color(0xFF2563EB)

                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = if (item.isCalled) "پیشگیری از تماس همزمان کارساز است" else "جهت شماره‌گیری، کارت مخاطب را لمس کنید",
                            color = if (item.isCalled) Color(0xFFEF4444) else LightBlueSecondary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Dialer Button Icon Container
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (item.isCalled) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "تماس تلفنی",
                                tint = if (item.isCalled) Color(0xFFEF4444) else Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Checkbox component: "بعدا زنگ بزن"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = item.isCallLater,
                        onCheckedChange = onToggleCallLater,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFD97706),
                            uncheckedColor = LightBlueSecondary.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "بعدا زنگ بزن (عدم پاسخ‌گویی شماره)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBlueText.copy(alpha = 0.8f)
                    )
                }

                // Sub-attribute tags area (any dynamic Persian/English custom fields)
                if (item.otherInfo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(
                        color = if (item.isCalled) Red100Border else Blue100Border,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    RowOfTags(infoMap = item.otherInfo, isCalled = item.isCalled)
                }
            }
        }
    }
}

@Composable
fun HistoryRecordItem(
    phoneNumber: String,
    calledAt: Long,
    onDialClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateString = remember(calledAt) {
        try {
            val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault())
            sdf.format(Date(calledAt))
        } catch (e: Exception) {
            "تاریخ نامشخص"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Blue100Border),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4) // Soft green for success history
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = phoneNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DarkBlueText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "زمان تماس: $dateString",
                        fontSize = 11.sp,
                        color = LightBlueSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Return call button
                IconButton(onClick = onDialClick) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "تماس مجدد",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Delete history button
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف رکورد",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowOfTags(infoMap: Map<String, String>, isCalled: Boolean) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        infoMap.entries.forEach { (key, value) ->
            Surface(
                color = if (isCalled) RedCalled.copy(alpha = 0.05f) else LightBlueSecondary.copy(alpha = 0.06f),
                border = BorderStroke(
                    0.5.dp, 
                    if (isCalled) RedCalled.copy(alpha = 0.3f) else LightBlueSecondary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.heightIn(min = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$key: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCalled) RedCalled else LightBlueSecondary,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCalled) RedCalled.copy(alpha = 0.9f) else DarkBlueText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    isHistory: Boolean = false,
    isListEmpty: Boolean,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isHistory) "تاریخچه تماس کاملاً خالی است" else if (isListEmpty) "دفترچه شماره‌ها خالی است" else "هیچ شماره‌ای مطابقت نداشت",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isHistory) {
                "هنوز تماسی از برنامه برقرار نشده است. پس از شماره‌گیری هر مخاطب، تاریخچه تماس در اینجا نمایش داده می‌شود."
            } else if (isListEmpty) {
                "برای بارگذاری لیست شماره‌های خود از پایتون، دکمه بارگذاری در بالای صفحه را بفشارید."
            } else {
                "جستجوی خود را تغییر دهید تا نتایج متفاوتی طبق معیارهای فیلتر دریافت کنید."
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        if (isListEmpty && !isHistory) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onImportClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("بارگذاری لیست شماره‌ها")
            }
        }
    }
}

// Dialog that supports pasting raw Python dictionary strings OR loading files
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportUiDialog(onDismiss: () -> Unit, onImportAction: (String) -> Unit) {
    var rawInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Set up file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val text = inputStream.bufferedReader().use { reader -> reader.readText() }
                    if (text.isNotBlank()) {
                        rawInput = text
                        Toast.makeText(context, "فایل با موفقیت بارگذاری شد!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در بارگذاری فایل: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val sampleSnippet = "[{'number':'09334769777', 'other_informations':{'کد ملی':'۱۲۳۴۵۶۷۸۹۰', 'شهر':'تهران', 'بخش':'پشتیبانی'}}, {'number':'09121234567','other_informations':{'کد ملی':'۹۸۷۶۵۴۳۲۱۰', 'وضعیت':'مشتری طلایی'}}]"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "وارد کردن پایگاه داده شماره‌ها",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "فهرست شماره‌های خود را به صورت آرایه یا قالب JSON پایتون وارد نمایید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Paste zone
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("import_text_input"),
                    placeholder = { Text("قالب دیتابیس را اینجا الصاق نمایید یا دکمه انتخاب فایل را بزنید...", fontSize = 13.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Accessory Row
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.testTag("select_file_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("انتخاب فایل (.txt/.json)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { rawInput = sampleSnippet },
                        modifier = Modifier.testTag("load_sample_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("استفاده از نمونه پایتون فارسی", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("لغو")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onImportAction(rawInput) },
                        modifier = Modifier.testTag("submit_import_button"),
                        enabled = rawInput.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("وارد کردن لیست")
                    }
                }
            }
        }
    }
}
