package com.example.myapiapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo_table")
data class MemoDto(
    @PrimaryKey(autoGenerate = true)
    val id : Long,
    var title : String?,
    var category : String?,
    var address : String?,
    var memo : String?,
    var fileName : String?,
)
