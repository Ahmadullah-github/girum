package com.ahmad.girum.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmad.girum.R
import com.ahmad.girum.data.AppLanguage
import com.ahmad.girum.data.DownloadItemEntity
import com.ahmad.girum.data.DownloadPlatform
import com.ahmad.girum.data.DownloadStatus
import kotlinx.coroutines.flow.collectLatest
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Border = Color(0xFFE5E7EB)
private val Teal = Color(0xFF009688)
private val TealDark = Color(0xFF007D78)
private val TealLight = Color(0xFF12BDB5)
private val SoftTeal = Color(0xFFDDF7F4)
private val SoftOrange = Color(0xFFFFE4AD)
private val SoftGreen = Color(0xFFBDF4D2)
private val SoftRed = Color(0xFFFFC6CC)
private val Purple = Color(0xFF7457E8)
private val Background = Color(0xFFF7FAFC)
private val CardWhite = Color(0xFFFFFFFF)
private val FieldBackground = Color(0xFFF8FAFC)
private val GirumFontFamily = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_medium, FontWeight.Medium),
    Font(R.font.noto_sans_semibold, FontWeight.SemiBold),
    Font(R.font.noto_sans_bold, FontWeight.Bold),
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold),
)

enum class AppTab(val icon: ImageVector) {
    HOME(Icons.Rounded.Home),
    QUEUE(Icons.AutoMirrored.Rounded.FormatListBulleted),
    HISTORY(Icons.Rounded.History),
    SETTINGS(Icons.Rounded.Settings),
}

@Composable
fun GirumApp(
    viewModel: GirumViewModel,
    onBeforeDownload: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    LaunchedEffect(Unit) {
        viewModel.messages.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    val layoutDirection = if (uiState.language == AppLanguage.DARI) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                primary = Teal,
                secondary = TealDark,
                background = Background,
                surface = CardWhite,
                onSurface = Ink,
            ),
        ) {
            ProvideTextStyle(
                value = TextStyle(
                    fontFamily = GirumFontFamily,
                    color = Ink,
                ),
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        GirumBottomBar(
                            selectedTab = selectedTab,
                            language = uiState.language,
                            onSelect = { selectedTab = it },
                        )
                    },
                    containerColor = Background,
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = Background,
                    ) {
                        when (selectedTab) {
                            AppTab.HOME -> HomeScreen(
                                state = uiState,
                                viewModel = viewModel,
                                onBeforeDownload = onBeforeDownload,
                                onOpenQueue = { selectedTab = AppTab.QUEUE },
                                onOpenHistory = { selectedTab = AppTab.HISTORY },
                            )
                            AppTab.QUEUE -> QueueScreen(uiState, viewModel)
                            AppTab.HISTORY -> HistoryScreen(uiState, viewModel)
                            AppTab.SETTINGS -> SettingsScreen(uiState, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: GirumUiState,
    viewModel: GirumViewModel,
    onBeforeDownload: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val copy = copyFor(state.language)
    var input by remember { mutableStateOf("") }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(Modifier.height(18.dp))
                Header(state.language, viewModel::setLanguage)
            }
            item {
                UrlAnalyzeCard(
                    input = input,
                    onInputChange = { input = it },
                    isAnalyzing = state.isAnalyzing,
                    copy = copy,
                    onAnalyze = {
                        onBeforeDownload()
                        viewModel.analyze(input)
                    },
                )
            }
            item { PlatformChips(copy) }
            item { StatsCard(state.stats, copy) }
            item {
                SectionHeader(
                    title = copy.activeDownloads,
                    action = copy.viewAll,
                    onAction = onOpenQueue,
                )
                Spacer(Modifier.height(8.dp))
                DownloadListCard(
                    items = state.activeDownloads,
                    emptyText = copy.noActiveDownloads,
                    viewModel = viewModel,
                    showControls = true,
                )
            }
            item {
                SectionHeader(
                    title = copy.recentDownloads,
                    action = copy.viewAll,
                    onAction = onOpenHistory,
                )
                Spacer(Modifier.height(8.dp))
                DownloadListCard(
                    items = state.recentDownloads,
                    emptyText = copy.noRecentDownloads,
                    viewModel = viewModel,
                    showControls = false,
                )
            }
            item { ComingSoon(copy) }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun GirumCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(8.dp),
        content = { content() },
    )
}

@Composable
private fun Header(language: AppLanguage, onLanguage: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GirumLogo()
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Girum", color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                Spacer(Modifier.height(2.dp))
                Text(copyFor(language).subtitle, color = Muted, fontSize = 17.sp, maxLines = 1)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LanguageSwitch(language, onLanguage)
            Spacer(Modifier.width(14.dp))
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = Color(0xFF374151),
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun GirumLogo() {
    Box(
        modifier = Modifier.size(62.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudDownload,
            contentDescription = null,
            tint = Teal,
            modifier = Modifier.size(56.dp),
        )
    }
}

@Composable
private fun LanguageSwitch(language: AppLanguage, onLanguage: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable {
                onLanguage(if (language == AppLanguage.DARI) AppLanguage.ENGLISH else AppLanguage.DARI)
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "دری",
            color = if (language == AppLanguage.DARI) Ink else Muted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text("|", color = Muted, modifier = Modifier.padding(horizontal = 6.dp))
        Text(
            "EN",
            color = if (language == AppLanguage.ENGLISH) Teal else Muted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun UrlAnalyzeCard(
    input: String,
    onInputChange: (String) -> Unit,
    isAnalyzing: Boolean,
    copy: Copy,
    onAnalyze: () -> Unit,
) {
    GirumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FieldBackground)
                    .border(1.dp, Border, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Link, contentDescription = null, tint = Color(0xFF4B5563), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(18.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Ink,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = GirumFontFamily,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (input.isBlank()) {
                    Text(
                        copy.pasteLink,
                        color = Muted,
                        fontSize = 23.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Box(
                modifier = Modifier
                    .height(58.dp)
                    .width(126.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Teal, TealLight),
                        ),
                    )
                    .clickable(enabled = !isAnalyzing, onClick = onAnalyze),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (isAnalyzing) copy.analyzing else copy.analyze,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlatformChips(copy: Copy) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlatformChip(Icons.Rounded.Link, copy.directUrl, Color(0xFF4B5563), Modifier.weight(1f))
        PlatformChip(Icons.Rounded.OndemandVideo, copy.youtube, Color.Red, Modifier.weight(1f))
        PlatformChip(Icons.AutoMirrored.Rounded.PlaylistPlay, copy.playlist, Teal, Modifier.weight(1f))
        PlatformChip(Icons.Rounded.Share, copy.shareTarget, Purple, Modifier.weight(1f))
    }
}

@Composable
private fun PlatformChip(icon: ImageVector, label: String, color: Color, modifier: Modifier) {
    GirumCard(modifier = modifier.height(62.dp)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (label == "YouTube" || label == "YT" || label == "یوتیوب") {
                YouTubeGlyph()
            } else {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun YouTubeGlyph() {
    Box(
        modifier = Modifier
            .size(width = 29.dp, height = 21.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Red),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun StatsCard(stats: com.ahmad.girum.data.QueueStats, copy: Copy) {
    GirumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(162.dp)
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(Icons.Rounded.Downloading, stats.active, copy.active, Teal, SoftTeal)
            StatDivider()
            StatItem(Icons.Rounded.Schedule, stats.queued, copy.queued, Color(0xFFC77800), SoftOrange)
            StatDivider()
            StatItem(Icons.Rounded.Check, stats.completed, copy.completed, Color(0xFF0B7D27), SoftGreen)
            StatDivider()
            StatItem(Icons.Rounded.Close, stats.failed, copy.failed, Color.Red, SoftRed)
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: Int, label: String, color: Color, background: Color) {
    Column(
        modifier = Modifier.width(74.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(value.toString(), color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        Text(label, color = color, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(90.dp)
            .width(1.dp)
            .background(Border),
    )
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            action,
            color = TealDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DownloadListCard(
    items: List<DownloadItemEntity>,
    emptyText: String,
    viewModel: GirumViewModel,
    showControls: Boolean,
) {
    GirumCard(modifier = Modifier.fillMaxWidth()) {
        if (items.isEmpty()) {
            Text(
                emptyText,
                color = Muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            )
        } else {
            Column {
                items.forEachIndexed { index, item ->
                    DownloadRow(
                        item = item,
                        viewModel = viewModel,
                        showControls = showControls,
                    )
                    if (index != items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Border),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadItemEntity,
    viewModel: GirumViewModel,
    showControls: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (showControls) 128.dp else 86.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DownloadBadge(item)
        Spacer(Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            StatusPill(item)
            Spacer(Modifier.height(6.dp))
            Text(progressLabel(item), color = Muted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.status == DownloadStatus.RUNNING.name || item.status == DownloadStatus.PAUSED.name || item.status == DownloadStatus.QUEUED.name) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Teal,
                    trackColor = Border,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.width(if (showControls) 92.dp else 86.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showControls) {
                Text(speedLabel(item), color = TealDark, fontSize = 15.sp, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Text(etaLabel(item), color = Muted, fontSize = 14.sp, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = {
                            if (item.status == DownloadStatus.PAUSED.name) viewModel.resume(item.id) else viewModel.pause(item.id)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.dp, Teal, CircleShape),
                    ) {
                        Icon(
                            if (item.status == DownloadStatus.PAUSED.name) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = null,
                            tint = Teal,
                        )
                    }
                    IconButton(
                        onClick = { viewModel.cancel(item.id) },
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.dp, Color.Red, CircleShape),
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.Red)
                    }
                }
            } else {
                Text(formatTime(item.updatedAt), color = Muted, fontSize = 14.sp)
                Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = Muted)
            }
        }
    }
}

@Composable
private fun DownloadBadge(item: DownloadItemEntity) {
    val background = when {
        item.platform == DownloadPlatform.YOUTUBE.name -> SoftRed
        item.outputName.endsWith(".mp3", ignoreCase = true) -> Color(0xFFEDE3FF)
        else -> SoftTeal
    }
    val tint = when {
        item.platform == DownloadPlatform.YOUTUBE.name -> Color.Red
        item.outputName.endsWith(".mp3", ignoreCase = true) -> Purple
        else -> Teal
    }
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(if (item.status == DownloadStatus.COMPLETED.name) RoundedCornerShape(8.dp) else CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        if (item.platform == DownloadPlatform.YOUTUBE.name) {
            YouTubeGlyph()
        } else {
            Icon(
                imageVector = when {
                    item.outputName.endsWith(".mp3", ignoreCase = true) -> Icons.Rounded.MusicNote
                    else -> Icons.AutoMirrored.Rounded.InsertDriveFile
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun StatusPill(item: DownloadItemEntity) {
    val label = when (item.status) {
        DownloadStatus.COMPLETED.name -> "Completed"
        DownloadStatus.FAILED.name -> "Failed"
        DownloadStatus.PAUSED.name -> "Paused"
        DownloadStatus.QUEUED.name -> "Queued"
        DownloadStatus.CANCELED.name -> "Canceled"
        else -> if (item.platform == DownloadPlatform.YOUTUBE.name) "YouTube" else "Direct URL"
    }
    val color = when (item.status) {
        DownloadStatus.FAILED.name, DownloadStatus.CANCELED.name -> Color.Red
        DownloadStatus.COMPLETED.name -> Color(0xFF0B7D27)
        else -> Teal
    }
    Text(
        text = label,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = 14.sp,
    )
}

@Composable
private fun ComingSoon(copy: Copy) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFF2F5))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Public, contentDescription = null, tint = Muted)
        Spacer(Modifier.width(12.dp))
        Text(copy.morePlatforms, color = Muted)
    }
}

@Composable
private fun QueueScreen(state: GirumUiState, viewModel: GirumViewModel) {
    ListScreen(
        title = copyFor(state.language).queue,
        items = state.downloads.filter { it.status != DownloadStatus.COMPLETED.name },
        emptyText = copyFor(state.language).queueEmpty,
        viewModel = viewModel,
        showControls = true,
    )
}

@Composable
private fun HistoryScreen(state: GirumUiState, viewModel: GirumViewModel) {
    ListScreen(
        title = copyFor(state.language).history,
        items = state.downloads.filter { it.status == DownloadStatus.COMPLETED.name || it.status == DownloadStatus.FAILED.name },
        emptyText = copyFor(state.language).historyEmpty,
        viewModel = viewModel,
        showControls = false,
    )
}

@Composable
private fun ListScreen(
    title: String,
    items: List<DownloadItemEntity>,
    emptyText: String,
    viewModel: GirumViewModel,
    showControls: Boolean,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(title, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        if (items.isEmpty()) {
            item {
                Text(emptyText, color = Muted, modifier = Modifier.padding(top = 24.dp))
            }
        } else {
            items(items, key = { it.id }) { item ->
                DownloadListCard(listOf(item), emptyText, viewModel, showControls)
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: GirumUiState, viewModel: GirumViewModel) {
    val copy = copyFor(state.language)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(copy.settings, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(copy.language, color = Ink, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.setLanguage(AppLanguage.DARI) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.language == AppLanguage.DARI) Teal else Color(0xFFE5E7EB),
                            contentColor = if (state.language == AppLanguage.DARI) Color.White else Ink,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("دری") }
                    Button(
                        onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.language == AppLanguage.ENGLISH) Teal else Color(0xFFE5E7EB),
                            contentColor = if (state.language == AppLanguage.ENGLISH) Color.White else Ink,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("English") }
                }
                Text(copy.storageHint, color = Muted)
            }
        }
    }
}

@Composable
private fun GirumBottomBar(selectedTab: AppTab, language: AppLanguage, onSelect: (AppTab) -> Unit) {
    val copy = copyFor(language)
    Surface(
        color = CardWhite,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val label = when (tab) {
                    AppTab.HOME -> copy.home
                    AppTab.QUEUE -> copy.queue
                    AppTab.HISTORY -> copy.history
                    AppTab.SETTINGS -> copy.settings
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(74.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) SoftTeal else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (selected) Teal else Color(0xFF4B5563),
                            modifier = Modifier.size(30.dp),
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        label,
                        color = if (selected) Teal else Color(0xFF374151),
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun progressLabel(item: DownloadItemEntity): String {
    return when {
        item.playlistIndex != null && item.playlistTotal != null ->
            "${item.playlistIndex} / ${item.playlistTotal} videos"
        item.totalBytes > 0L ->
            "${bytes(item.downloadedBytes)} / ${bytes(item.totalBytes)} • ${(item.progress * 100).toInt()}%"
        item.status == DownloadStatus.FAILED.name ->
            item.error ?: "Failed"
        else ->
            item.status.lowercase().replaceFirstChar { it.titlecase() }
    }
}

private fun speedLabel(item: DownloadItemEntity): String {
    return if (item.speedBytesPerSecond > 0L) "${bytes(item.speedBytesPerSecond)}/s" else ""
}

private fun etaLabel(item: DownloadItemEntity): String {
    return if (item.etaSeconds > 0L) "ETA ${item.etaSeconds / 60}m ${item.etaSeconds % 60}s" else ""
}

private fun bytes(value: Long): String {
    if (value < 0L) return "-"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var unit = 0
    while (size >= 1024 && unit < units.lastIndex) {
        size /= 1024
        unit++
    }
    return if (unit == 0) "${size.toLong()} ${units[unit]}" else String.format(Locale.US, "%.1f %s", size, units[unit])
}

private fun formatTime(time: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(time))
}

data class Copy(
    val subtitle: String,
    val pasteLink: String,
    val analyze: String,
    val analyzing: String,
    val directUrl: String,
    val youtube: String,
    val playlist: String,
    val shareTarget: String,
    val active: String,
    val queued: String,
    val completed: String,
    val failed: String,
    val activeDownloads: String,
    val recentDownloads: String,
    val viewAll: String,
    val noActiveDownloads: String,
    val noRecentDownloads: String,
    val morePlatforms: String,
    val home: String,
    val queue: String,
    val history: String,
    val settings: String,
    val queueEmpty: String,
    val historyEmpty: String,
    val language: String,
    val storageHint: String,
)

private fun copyFor(language: AppLanguage): Copy {
    return if (language == AppLanguage.DARI) {
        Copy(
            subtitle = "مدیر دانلود خصوصی",
            pasteLink = "لینک را وارد یا شریک سازید",
            analyze = "بررسی",
            analyzing = "در حال بررسی",
            directUrl = "URL",
            youtube = "YT",
            playlist = "Playlist",
            shareTarget = "Share",
            active = "فعال",
            queued = "در صف",
            completed = "تکمیل",
            failed = "ناکام",
            activeDownloads = "دانلودهای فعال",
            recentDownloads = "دانلودهای اخیر",
            viewAll = "همه",
            noActiveDownloads = "دانلود فعالی وجود ندارد.",
            noRecentDownloads = "هنوز دانلودی تکمیل نشده است.",
            morePlatforms = "پلتفرم‌های بیشتر در نسخه‌های بعدی اضافه می‌شوند.",
            home = "خانه",
            queue = "صف",
            history = "تاریخچه",
            settings = "تنظیمات",
            queueEmpty = "صف دانلود خالی است.",
            historyEmpty = "تاریخچه خالی است.",
            language = "زبان",
            storageHint = "فایل‌ها در Downloads/Girum ذخیره می‌شوند.",
        )
    } else {
        Copy(
            subtitle = "Private download manager",
            pasteLink = "Paste or share a link",
            analyze = "Analyze",
            analyzing = "Analyzing",
            directUrl = "URL",
            youtube = "YT",
            playlist = "Playlist",
            shareTarget = "Share",
            active = "Active",
            queued = "Queued",
            completed = "Completed",
            failed = "Failed",
            activeDownloads = "Active Downloads",
            recentDownloads = "Recent Downloads",
            viewAll = "View all",
            noActiveDownloads = "No active downloads yet.",
            noRecentDownloads = "No recent downloads yet.",
            morePlatforms = "More platforms coming soon: Instagram, Facebook, TikTok, and more.",
            home = "Home",
            queue = "Queue",
            history = "History",
            settings = "Settings",
            queueEmpty = "The queue is empty.",
            historyEmpty = "History is empty.",
            language = "Language",
            storageHint = "Files are saved under Downloads/Girum.",
        )
    }
}
