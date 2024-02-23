package com.example.myapiapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.myapiapplication.data.Restaurant
import com.example.myapiapplication.databinding.ActivityMainBinding
import com.example.myapiapplication.network.NetworkManager
import com.example.myapiapplication.ui.ListViewAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var mainBinding : ActivityMainBinding
    lateinit var restaurants : ArrayList<Restaurant>
    lateinit var adapter : ListViewAdapter
    lateinit var networkDao : NetworkManager

    lateinit var fusedLocationClient : FusedLocationProviderClient
    lateinit var geocoder : Geocoder
    lateinit var currentLoc : Location
    lateinit var googleMap : GoogleMap
    var centerMarker : Marker? = null
    var markerArr = ArrayList<Marker?>()
    var addrArr  = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        checkPermissions()
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync (mapReadyCallback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        networkDao = NetworkManager(this)
        adapter = ListViewAdapter(this)
        mainBinding.itemList.adapter = adapter

        mainBinding.btnSearch.setOnClickListener {
            var keyword = mainBinding.etKeyword.text.toString()
            if(keyword.isEmpty()){
                Log.d(TAG, "키워드:"+keyword)
                Toast.makeText(this@MainActivity, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                restaurants = ArrayList<Restaurant>(0)
            }
            addrArr.clear()
            CoroutineScope(Dispatchers.Main).launch{
                val def = async(Dispatchers.IO) {
                    try { //여기서 parsing 시작, 결과: restaurants
                        restaurants = networkDao.downloadXml(keyword)
                        for(i in restaurants.indices){
                            addrArr.add(restaurants[i].address.toString())
                            Log.d(TAG, "addr : " + addrArr.get(i))
                        }
                    } catch (e: IOException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    } catch (e: XmlPullParserException) {
                        Log.d(TAG, e.message?: "null")
                        null
                    }
                    restaurants
                }
                adapter.restaurantList = def.await()
                adapter.notifyDataSetChanged()
                for(i in markerArr.indices){
                    markerArr.get(i)?.remove()
                }
                startLocUpdates()
                //지도 검색 시 처음으로 나타낼 위치 지정
                try {
                    geocoder.getFromLocationName(
                        "${addrArr.get(0)}",
                        5
                    ) { addresses -> //Geocoding을 쓸때는 기본적으로 코루틴으로 감싸줘야함
                        CoroutineScope(Dispatchers.Main).launch {
                            val locPosition =
                                LatLng(addresses.get(0).latitude, addresses.get(0).longitude)
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    locPosition,
                                    14.5F
                                )
                            )
                        }
                    }
                }catch(e:Exception){
                    Log.d(TAG, "message: ${e.message}")
                }
            }
        }

        mainBinding.myPlace.setOnClickListener {
            //my_place.xml로 가는 intent 생성
            val intent = Intent(this, MyPlaceActivity::class.java)
            startActivity(intent)
        }
    }


    fun checkPermissions () {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions are already granted")
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    val locationPermissionRequest
            = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) {
            permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.d(TAG,"FINE_LOCATION is granted")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d(TAG,"COARSE_LOCATION is granted")
            }
            else -> {
                Log.d(TAG,"Location permissions are required")
            }
        }
    }
    /*GoogleMap 로딩이 완료될 경우 실행하는 Callback*/
    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) { //지도가 로딩되었을 때 무엇을 실행할지 정의
            googleMap = map
            Log.d(TAG, "GoogleMap is ready")
            googleMap.setOnMarkerClickListener {
                false //false 왜 반환? => true반환은 여기서 처리가 끝났다는 것. false반환은 이벤트 처리가 여기서 끝나지 않았다는거. true로 하면 infoWindow 미출력
            }
            googleMap.setOnInfoWindowClickListener { marker ->
            }
            googleMap.setOnMapClickListener { latLng ->
            }
        }
    }

    /*위치 정보 수신 받았을 때 수행할 동작을 정의하는 Callback함수*/
    val locCallback : LocationCallback = object : LocationCallback() {
        @SuppressLint("NewApi")
        override fun onLocationResult(locResult: LocationResult) {
            for((i, v) in addrArr.withIndex()) {
                geocoder.getFromLocationName("${v.toString()}", 5) { addresses -> //Geocoding
                    CoroutineScope(Dispatchers.Main).launch { //비동기 방식
                        val targetLoc = LatLng(addresses.get(0).latitude, addresses.get(0).longitude) // addrArr의 첫번째 요소 위도, 경도
                        Log.d(TAG, "위도: ${addresses.get(0).latitude}, 경도: ${addresses.get(0).longitude}")
                        addMarker(targetLoc, i)
                    }
                }
            }
        }
    }

    /*위치 정보 수신을 위한 조건 설정*/
    val locRequest = LocationRequest.Builder(5000)
        .setMinUpdateIntervalMillis(3000)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    /*위치 정보 수신 시작*/
    @SuppressLint("MissingPermission")
    private fun startLocUpdates() { //이걸 하는 순간 위치정보 수신받음
        fusedLocationClient.requestLocationUpdates(
            locRequest,     // LocationRequest 객체
            locCallback,    // LocationCallback 객체
            Looper.getMainLooper()  // System 메시지 수신 Looper
        )
    }


    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }

    /*마커 추가*/
    fun addMarker(targetLoc: LatLng, idx : Int) {  // LatLng(37.606320, 127.041808)
        val markerOptions : MarkerOptions = MarkerOptions()
         markerOptions.position(targetLoc)
            .title(restaurants.get(idx).title?.replace("<b>"," ")?.replace("</b>", " "))
            .snippet(restaurants.get(idx).category)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        centerMarker = googleMap.addMarker(markerOptions)
        markerArr.add(centerMarker)
        centerMarker?.showInfoWindow()
    }
}