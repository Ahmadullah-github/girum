package com.ahmad.girum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ahmad.girum.core.AppContainer
import com.ahmad.girum.data.AppLanguage
import com.ahmad.girum.data.DownloadItemEntity
import com.ahmad.girum.data.DownloadStatus
import com.ahmad.girum.data.QueueStats
import com.ahmad.girum.data.QueueStatsCalculator
import com.ahmad.girum.download.DownloadRepository
import com.ahmad.girum.download.EnqueueResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GirumUiState(
    val downloads: List<DownloadItemEntity> = emptyList(),
    val language: AppLanguage = AppLanguage.DARI,
    val stats: QueueStats = QueueStats(),
    val activeDownloads: List<DownloadItemEntity> = emptyList(),
    val recentDownloads: List<DownloadItemEntity> = emptyList(),
    val isAnalyzing: Boolean = false,
)

class GirumViewModel(
    private val appContainer: AppContainer,
) : ViewModel() {
    private val repository: DownloadRepository = appContainer.downloadRepository
    private val analyzing = kotlinx.coroutines.flow.MutableStateFlow(false)

    val messages = MutableSharedFlow<String>()

    val uiState: StateFlow<GirumUiState> = combine(
        repository.downloads,
        appContainer.settingsRepository.language,
        analyzing,
    ) { downloads, language, isAnalyzing ->
        GirumUiState(
            downloads = downloads,
            language = language,
            stats = QueueStatsCalculator.from(downloads),
            activeDownloads = downloads
                .filter { it.status == DownloadStatus.RUNNING.name || it.status == DownloadStatus.QUEUED.name || it.status == DownloadStatus.PAUSED.name }
                .sortedBy { it.createdAt }
                .take(4),
            recentDownloads = downloads
                .filter { it.status == DownloadStatus.COMPLETED.name || it.status == DownloadStatus.FAILED.name }
                .sortedByDescending { it.updatedAt }
                .take(5),
            isAnalyzing = isAnalyzing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = GirumUiState(),
    )

    fun analyze(input: String) {
        if (input.isBlank()) {
            sendMessage("Enter or share a link")
            return
        }
        viewModelScope.launch {
            analyzing.value = true
            val result = runCatching { repository.enqueueFromInput(input) }
                .getOrElse { EnqueueResult.Error(it.message ?: "Could not analyze link") }
            analyzing.value = false
            when (result) {
                is EnqueueResult.Success -> messages.emit(result.message)
                is EnqueueResult.Error -> messages.emit(result.message)
            }
        }
    }

    fun pause(id: String) {
        viewModelScope.launch { repository.pause(id) }
    }

    fun resume(id: String) {
        viewModelScope.launch { repository.resume(id) }
    }

    fun retry(id: String) {
        viewModelScope.launch { repository.retry(id) }
    }

    fun cancel(id: String) {
        viewModelScope.launch { repository.cancel(id) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { appContainer.settingsRepository.setLanguage(language) }
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch { messages.emit(message) }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GirumViewModel(appContainer) as T
                }
            }
        }
    }
}
