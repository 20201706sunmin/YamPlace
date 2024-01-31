package com.example.myapiapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MemoDto::class], version = 1)
abstract class MemoDatabase  : RoomDatabase(){

    abstract fun memoDao() : MemoDao

    companion object{
        @Volatile
        private var INSTANCE : MemoDatabase? = null

        fun getDatabase(context : Context) : MemoDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, MemoDatabase::class.java, "memo_db1"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}