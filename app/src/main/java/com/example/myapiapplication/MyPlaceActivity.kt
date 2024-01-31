package com.example.myapiapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapiapplication.data.MemoDao
import com.example.myapiapplication.data.MemoDatabase
import com.example.myapiapplication.databinding.MyPlaceBinding
import com.example.myapiapplication.ui.MyPlaceAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPlaceActivity : AppCompatActivity() {
    val TAG = "MyPlaceActivity"

    lateinit var myPlaceBinding: MyPlaceBinding
    val memoDB : MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao : MemoDao by lazy{
        memoDB.memoDao()
    }
    val adapter = MyPlaceAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myPlaceBinding = MyPlaceBinding.inflate(layoutInflater)
        setContentView(myPlaceBinding.root)

        showAllMemo()
        val itemList = myPlaceBinding.itemList
        itemList.adapter = adapter

        myPlaceBinding.toMain.setOnClickListener {
            val toMainIntent = Intent(this, MainActivity::class.java)
            startActivity(toMainIntent)
        }
    }

    fun showAllMemo() {
        Log.d(TAG, "showAllMemo 메서드 입장")
        CoroutineScope(Dispatchers.Main).launch {
            memoDao.showAllMemos().collect { memos ->
                adapter.myPlaceList = memos
                adapter.notifyDataSetChanged()
            }
        }
    }
}