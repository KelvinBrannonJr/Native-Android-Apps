package kbrannon.youpickfoodpicker.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kbrannon.youpickfoodpicker.R
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mFoodPlaceDetails:  YouPickFoodPickerModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mFoodPlaceDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as YouPickFoodPickerModel
        }

        if (mFoodPlaceDetails != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mFoodPlaceDetails!!.title


            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {

        val position = LatLng(
            mFoodPlaceDetails!!.latitude,
            mFoodPlaceDetails!!.longitude
        )
        googleMap.addMarker(MarkerOptions().position(position).title(mFoodPlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}

