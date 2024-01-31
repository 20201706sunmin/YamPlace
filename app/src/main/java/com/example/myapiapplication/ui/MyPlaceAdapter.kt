package com.example.myapiapplication.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapiapplication.ItemDetailActivity
import com.example.myapiapplication.MyPlaceActivity
import com.example.myapiapplication.R
import com.example.myapiapplication.data.MemoDao
import com.example.myapiapplication.data.MemoDatabase
import com.example.myapiapplication.data.MemoDto
import com.example.myapiapplication.data.Restaurant
import com.example.myapiapplication.databinding.MyPlaceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.myapiapplication.databinding.MyPlaceListviewBinding

class MyPlaceAdapter (var context : Context) : BaseAdapter() { //myplace로 들어가서 리스트뷰에 관한 것들
    val TAG = "MyPlaceAdapter"

    var myPlaceList: List<MemoDto>? = null

    val memoDB : MemoDatabase by lazy {
        MemoDatabase.getDatabase(context)
    }

    val memoDao : MemoDao by lazy{
        memoDB.memoDao()
    }

    val adapter = this

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View { //리스트뷰 화면에 어떻게 보일지 설정
        var view = convertView
        val context = parent?.context

        if (view == null) {
            val inflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.my_place_listview, parent, false)
        }
        val titleMyPlace = view!!.findViewById<TextView>(R.id.titleMyPlace)
        val catMyPlcae = view!!.findViewById<TextView>(R.id.catMyPlace)
        val addrMyPlace = view!!.findViewById<TextView>(R.id.addrMyPlace)
        val btnUpdMemoMyPlace = view!!.findViewById<Button>(R.id.btnUpdMemoMyPlace)
        val btnDelMemoMyPlace = view!!.findViewById<Button>(R.id.btnDelMemoMyPlace)
        val memoMyPlace = view!!.findViewById<TextView>(R.id.memoMyPlace)
        val listViewItem = myPlaceList?.get(position)

        titleMyPlace.setText(listViewItem?.title?.replace("<b>"," ")?.replace("</b>", " "))
        catMyPlcae.setText("종류 : " + listViewItem?.category)
        addrMyPlace.setText("주소 : " + listViewItem?.address)
        memoMyPlace.setText("메모 내용 : " + listViewItem?.memo)

        btnUpdMemoMyPlace.setOnClickListener {
            var outIntent : Intent = Intent(context, ItemDetailActivity::class.java)
            outIntent.putExtra("position", position)
            outIntent.putExtra("id", listViewItem?.id)
            outIntent.putExtra("requestCode",2)
            context?.startActivity(outIntent)
        }

        btnDelMemoMyPlace.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                memoDao.deleteMemoById(listViewItem!!.id)
            }
            adapter.notifyDataSetChanged()
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return myPlaceList?.get(position) ?: Int
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return myPlaceList?.size ?: 0
    }
}