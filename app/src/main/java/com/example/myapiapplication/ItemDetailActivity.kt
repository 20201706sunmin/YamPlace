package com.example.myapiapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.myapiapplication.data.MemoDao
import com.example.myapiapplication.data.MemoDatabase
import com.example.myapiapplication.data.MemoDto
import com.example.myapiapplication.databinding.ItemDetailBinding
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemDetailActivity : AppCompatActivity() {
    private val TAG = "ListViewActivity"
    lateinit var itemDetailBinding: ItemDetailBinding
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var geocoder: Geocoder
    lateinit var googleMap: GoogleMap
    var centerMarker: Marker? = null
    var title: String? = null
    var category: String? = null
    var address: String? = null
    var id : Long? = 0
    var requestCode : Int? = null
    val REQUEST_IMAGE_CAPTURE = 1
    val MEMO_REQUEST = 2

    val memoDatabase : MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao : MemoDao by lazy{
        memoDatabase.memoDao()
    }

    var memoDto : MemoDto = MemoDto(id!!, title, category, address, "memo", fileName = null)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemDetailBinding = ItemDetailBinding.inflate(layoutInflater)
        setContentView(itemDetailBinding.root)

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment
        mapFragment.getMapAsync(mapReadyCallback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        val receivedIntent = intent
        title = receivedIntent.getStringExtra("title")
        category = receivedIntent.getStringExtra("category")
        address = receivedIntent.getStringExtra("address")
        id = receivedIntent.getLongExtra("id", 0)
        requestCode = receivedIntent.getIntExtra("requestCode", 0)
        if(requestCode == MEMO_REQUEST){
            CoroutineScope(Dispatchers.IO).launch {
                memoDto = memoDao.getMemoById(id!!)

                itemDetailBinding.titleDetail.setText(memoDto.title?.replace("<b>"," ")?.replace("</b>", " "))
                itemDetailBinding.catDetail.setText("종류 : " + memoDto.category)
                itemDetailBinding.addrDetail.setText("주소 : " + memoDto.address)
                itemDetailBinding.memoContent.setText(memoDto.memo)
                address = memoDto.address
                Log.d(TAG, "주소${address}")
            }

            if(memoDto.fileName != "null") {
                Log.d(TAG, "사진이름있음: ${memoDto.fileName}")
                val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

                val file = File("${storageDir?.path}/${memoDto.fileName}") //시간으로 파일이름을 생성

                currentPhotoFileName = file.name
                currentPhotoPath = file.absolutePath
                val bitmap = BitmapFactory.decodeFile(currentPhotoPath)

                itemDetailBinding.imgView.setImageBitmap(bitmap)
            }
        }
        else {
            requestCode = 0
            itemDetailBinding.titleDetail.setText(title?.replace("<b>", " ")?.replace("</b>", " "))
            itemDetailBinding.catDetail.setText("종류 : " + category)
            itemDetailBinding.addrDetail.setText("주소 : " + address)
        }
        startLocUpdates()

        itemDetailBinding.btnCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        itemDetailBinding.btnSave.setOnClickListener {
            val memo = itemDetailBinding.memoContent.text.toString()
            if(requestCode == MEMO_REQUEST){//requestCode에 따른 메모 insert or update
                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.updateMemoById(memo, currentPhotoFileName, id!!)
                }

            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.insertMemo(MemoDto(0, title, category, address, memo, currentPhotoFileName))
                }
            }

            Toast.makeText(this@ItemDetailActivity, "메모가 저장되었습니다.", Toast.LENGTH_LONG).show()
        }

        itemDetailBinding.myPlace.setOnClickListener {
            val toMyPlaceIntent = Intent(this, MyPlaceActivity::class.java)
            startActivity(toMyPlaceIntent)
        }

    }

    /*GoogleMap 로딩이 완료될 경우 실행하는 Callback*/
    val mapReadyCallback = object : OnMapReadyCallback {
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
    val locCallback: LocationCallback = object : LocationCallback() {
        @SuppressLint("NewApi")
        override fun onLocationResult(locResult: LocationResult) {
            geocoder.getFromLocationName("${address}", 1) { addresses -> //Geocoding
                CoroutineScope(Dispatchers.Main).launch { //비동기 방식
                    val targetLoc = LatLng(addresses.get(0).latitude, addresses.get(0).longitude) // addrArr의 첫번째 요소 위도, 경도
                    Log.d(TAG, "위도: ${addresses.get(0).latitude}, 경도: ${addresses.get(0).longitude}")
                    addMarker(targetLoc)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
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

    /*마커 추가*/
    fun addMarker(targetLoc: LatLng) {  // LatLng(37.606320, 127.041808)
        val markerOptions: MarkerOptions = MarkerOptions()
        markerOptions.position(targetLoc)
            .title(title?.replace("<b>"," ")?.replace("</b>", " "))
            .snippet(category)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        centerMarker = googleMap.addMarker(markerOptions)
        centerMarker?.showInfoWindow()
    }

    private fun dispatchTakePictureIntent() {   // 원본 사진 요청
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) { // 카메라 앱 확인 (생략 가능)
            val photoFile: File? = try { // 고화질 사진을 저장할 파일 생성
                createImageFile() //파일정보생성
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            if (photoFile != null) { //파일을 Uri형태로 생성해서 intent에 putExtra함
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.myapiapplication.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    setPic()
                }
            }
        }
    }

    lateinit var currentPhotoPath: String   // 현재 이미지 파일의 경로 저장
    var currentPhotoFileName: String? = null  // 현재 이미지 파일명 저장

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) //Date객체를 읽는순간 시간이 지정한 형식으로 바뀜
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File ("${storageDir?.path}/${timeStamp}.jpg") //시간으로 파일이름을 생성

        //파일이름 보관했다가 쓸 수 있도록 함
        currentPhotoFileName = file.name
        currentPhotoPath = file.absolutePath
        return file
    }

    fun setPic() {
        Glide.with(this)
            .load(File(currentPhotoPath))
            .override(100, 100)
            .into(itemDetailBinding.imgView)
    }

}
