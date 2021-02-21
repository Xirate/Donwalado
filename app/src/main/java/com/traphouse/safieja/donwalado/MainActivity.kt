package com.traphouse.safieja.donwalado

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.android.synthetic.main.activity_main.*

//49.459069, 13.602941
//55.383678, 25.182531

class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private val plateNumberPairingRepository = PlateNumberPairingRepository()

    private var searchVisible = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val pairingList = PlateNumberDataReader().readPlateNumberDataFromJson(resources)
        plateNumberPairingRepository.populateMappingsFromList(pairingList)
        Toast.makeText(this, "Loaded ${plateNumberPairingRepository.plateNumberMappings.entries.size} pairings", Toast.LENGTH_SHORT).show()
        searchButton.setOnClickListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val bounds = LatLngBounds(LatLng(49.459069, 13.602941), LatLng(55.383678, 25.182531))
        googleMap?.setOnMapLoadedCallback {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10))
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.searchButton -> onSearchClicked()
        }
    }

    private fun onSearchClicked() {
        if (!searchVisible) {
            searchInput.visibility = View.VISIBLE
            searchVisible = true
        } else {
            noInputInfo.text = plateNumberPairingRepository.plateNumberMappings[searchInput.text.toString().toUpperCase().split(" ")[0]]
        }
    }
}