package com.example.data.repository

import com.example.data.dao.PhoneDao
import com.example.data.entity.CalledNumber
import com.example.data.entity.ImportedItem
import com.example.data.entity.CallLaterNumber
import kotlinx.coroutines.flow.Flow

class PhoneRepository(private val phoneDao: PhoneDao) {
    val allCalledNumbers: Flow<List<CalledNumber>> = phoneDao.getAllCalledNumbers()
    val allImportedItems: Flow<List<ImportedItem>> = phoneDao.getAllImportedItems()
    val allCallLaterNumbers: Flow<List<CallLaterNumber>> = phoneDao.getAllCallLaterNumbers()

    suspend fun addCalledNumber(phoneNumber: String) {
        phoneDao.insertCalledNumber(CalledNumber(phoneNumber))
    }

    suspend fun removeCalledNumber(phoneNumber: String) {
        phoneDao.deleteCalledNumber(phoneNumber)
    }

    suspend fun clearCalledHistory() {
        phoneDao.clearAllCalledNumbers()
    }

    suspend fun addCallLaterNumber(phoneNumber: String) {
        phoneDao.insertCallLaterNumber(CallLaterNumber(phoneNumber))
    }

    suspend fun removeCallLaterNumber(phoneNumber: String) {
        phoneDao.deleteCallLaterNumber(phoneNumber)
    }

    suspend fun clearCallLater() {
        phoneDao.clearAllCallLaterNumbers()
    }

    suspend fun saveImportedItems(items: List<ImportedItem>) {
        phoneDao.clearAllImportedItems()
        phoneDao.insertImportedItems(items)
    }

    suspend fun clearImportedItems() {
        phoneDao.clearAllImportedItems()
    }
}
