package com.zero.health.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zero.health.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @date:2025/12/24 10:39
 * @path:com.zero.health.ui.activity.AlarmRemindViewModel
 */
class AlarmRemindViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<ProcessUiModel>>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    private val monitor = ProcessMonitor(2000,
        setOf("com.zionhuang.music.debug", "com.dev.example", "com.toto.jcyj.mvmix",
            "com.github.metacubex.clash.meta"))

    fun load() {
        monitor.start()
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2000)
                val list = monitor.buildUiList()
                _uiState.value = UiState.Success(list)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        monitor.stop()
    }
}