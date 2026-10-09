package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.CalledNumber
import com.example.data.entity.ImportedItem
import com.example.data.entity.CallLaterNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneDao {
    // Called numbers queries
    @Query("SELECT * FROM called_numbers ORDER BY calledAt DESC")
    fun getAllCalledNumbers(): Flow<List<CalledNumber>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalledNumber(calledNumber: CalledNumber)

    @Query("DELETE FROM called_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun deleteCalledNumber(phoneNumber: String)

    @Query("DELETE FROM called_numbers")
    suspend fun clearAllCalledNumbers()

    // Call later numbers queries
    @Query("SELECT * FROM call_later_numbers")
    fun getAllCallLaterNumbers(): Flow<List<CallLaterNumber>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLaterNumber(callLaterNumber: CallLaterNumber)

    @Query("DELETE FROM call_later_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun deleteCallLaterNumber(phoneNumber: String)

    @Query("DELETE FROM call_later_numbers")
    suspend fun clearAllCallLaterNumbers()

    // Imported items queries
    @Query("SELECT * FROM imported_items ORDER BY id ASC")
    fun getAllImportedItems(): Flow<List<ImportedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedItems(items: List<ImportedItem>)

    @Query("DELETE FROM imported_items")
    suspend fun clearAllImportedItems()
}
