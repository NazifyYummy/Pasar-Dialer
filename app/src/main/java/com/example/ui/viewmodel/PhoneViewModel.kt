package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ImportedItem
import com.example.data.entity.CalledNumber
import com.example.data.entity.CallLaterNumber
import com.example.data.repository.PhoneRepository
import com.example.utils.JsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiImportItem(
    val id: Int,
    val phoneNumber: String,
    val otherInfo: Map<String, String>,
    val isCalled: Boolean,
    val isCallLater: Boolean
)

class PhoneViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PhoneRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PhoneRepository(database.phoneDao())
    }

    // List of call history
    val calledHistory: StateFlow<List<CalledNumber>> = repository.allCalledNumbers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combine imported items, called items, and call later items into unified UI models
    val uiItems: StateFlow<List<UiImportItem>> = combine(
        repository.allImportedItems,
        repository.allCalledNumbers,
        repository.allCallLaterNumbers
    ) { imported, called, callLater ->
        val calledSet = called.map { it.phoneNumber }.toSet()
        val callLaterSet = callLater.map { it.phoneNumber }.toSet()
        imported.map { item ->
            UiImportItem(
                id = item.id,
                phoneNumber = item.phoneNumber,
                otherInfo = JsonHelper.jsonToMap(item.otherInfoJson),
                isCalled = calledSet.contains(item.phoneNumber),
                isCallLater = callLaterSet.contains(item.phoneNumber)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    private val _importSuccessMessage = MutableStateFlow<String?>(null)
    val importSuccessMessage: StateFlow<String?> = _importSuccessMessage.asStateFlow()

    fun clearMessages() {
        _importError.value = null
        _importSuccessMessage.value = null
    }

    fun importDataJson(rawJson: String) {
        viewModelScope.launch {
            if (rawJson.isBlank()) {
                _importError.value = "JSON وارد شده نمی‌تواند خالی باشد."
                return@launch
            }
            try {
                _importError.value = null
                _importSuccessMessage.value = null
                val parsed = JsonHelper.parseImportedData(rawJson)
                if (parsed.isEmpty()) {
                    _importError.value = "هیچ رکورد معتبری یافت نشد. قالب متن را بررسی کنید."
                    return@launch
                }

                val dbEntries = parsed.map {
                    ImportedItem(
                        phoneNumber = it.number,
                        otherInfoJson = JsonHelper.mapToJson(it.otherInformations)
                    )
                }

                repository.saveImportedItems(dbEntries)
                _importSuccessMessage.value = "تعداد ${parsed.size} شماره با موفقیت وارد شد!"
            } catch (e: Exception) {
                _importError.value = "خطا در تجزیه داده‌ها: ${e.localizedMessage ?: "قالب نامعتبر"}"
            }
        }
    }

    fun markAsCalled(phoneNumber: String) {
        viewModelScope.launch {
            repository.addCalledNumber(phoneNumber)
        }
    }

    fun removeCalledHistory(phoneNumber: String) {
        viewModelScope.launch {
            repository.removeCalledNumber(phoneNumber)
        }
    }

    fun toggleCallLater(phoneNumber: String, isCallLater: Boolean) {
        viewModelScope.launch {
            if (isCallLater) {
                repository.addCallLaterNumber(phoneNumber)
            } else {
                repository.removeCallLaterNumber(phoneNumber)
            }
        }
    }

    fun clearCalledHistory() {
        viewModelScope.launch {
            repository.clearCalledHistory()
        }
    }

    fun clearAllImported() {
        viewModelScope.launch {
            repository.clearImportedItems()
            repository.clearCallLater()
            _importSuccessMessage.value = null
            _importError.value = null
        }
    }
}
