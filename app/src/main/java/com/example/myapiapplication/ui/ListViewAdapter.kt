package com.example.myapiapplication.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.myapiapplication.ItemDetailActivity
import com.example.myapiapplication.R
import com.example.myapiapplication.data.Restaurant

class ListViewAdapter (var context : Context) : BaseAdapter() {
    val TAG = "ListViewAdapter"

    var restaurantList: ArrayList<Restaurant>? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View { //리스트뷰 화면에 어떻게 보일지 설정
        var view = convertView
        val context = parent?.context

        if (view == null) {
            val inflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.listview_item, parent, false)
        }
        val resTitle = view!!.findViewById<TextView>(R.id.resTitle)
        val resCat = view!!.findViewById<TextView>(R.id.resCat)
        val resAddr = view!!.findViewById<TextView>(R.id.resAddr)
        val btnInsMemo = view!!.findViewById<Button>(R.id.btnInsMemo)

        val listViewItem = restaurantList?.get(position)

        resTitle.setText(listViewItem?.title?.replace("<b>"," ")?.replace("</b>", " "))
        resCat.setText("종류 : " + listViewItem?.category)
        resAddr.setText("주소 : " + listViewItem?.address)


        btnInsMemo.setOnClickListener {
            var outIntent : Intent = Intent(context, ItemDetailActivity::class.java)
            outIntent.putExtra("position", position);
            outIntent.putExtra("title",listViewItem?.title)
            outIntent.putExtra("category", listViewItem?.category)
            outIntent.putExtra("address", listViewItem?.address)
            context?.startActivity(outIntent)
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return restaurantList?.get(position) ?: Int
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return restaurantList?.size ?: 0
    }
}