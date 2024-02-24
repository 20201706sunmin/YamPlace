package com.example.myapiapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo : MemoDto)

    @Query("UPDATE memo_table SET memo = :newMemo, fileName = :newFileName WHERE id = :id")
    suspend fun updateMemoById(newMemo: String, newFileName: String?, id : Long)

    @Query("SELECT * FROM memo_table WHERE id = :id")
    suspend fun getMemoById(id: Long) : MemoDto

    @Query("DELETE FROM memo_table WHERE id = :id")
    suspend fun deleteMemoById(id : Long)

    @Query("SELECT * FROM memo_table")
    fun showAllMemos() : Flow<List<MemoDto>>

}