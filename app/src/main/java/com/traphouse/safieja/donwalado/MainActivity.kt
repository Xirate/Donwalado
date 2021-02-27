package com.traphouse.safieja.donwalado

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private val RC_SIGN_IN = 0

    private val plateNumberPairingRepository = PlateNumberPairingRepository()

    private var searchVisible = false

    private lateinit var googleMap: GoogleMap
    private var lastMarker: Marker? = null
    private lateinit var callbackManager: CallbackManager
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(applicationContext)
//        AppEventsLogger.activateApp(applicationContext)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val pairingList = PlateNumberDataReader().readPlateNumberDataFromJson(resources)
        plateNumberPairingRepository.populateMappingsFromList(pairingList)
        Toast.makeText(this, "Loaded ${plateNumberPairingRepository.plateNumberMappings.entries.size} pairings", Toast.LENGTH_SHORT).show()
        searchButton.setOnClickListener(this)
        callbackManager = CallbackManager.Factory.create()
        fb_login_button.setPermissions("email")
        fb_login_button.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Toast.makeText(this@MainActivity, "Zalogowano", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Przerwano", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@MainActivity, "Zjebano", Toast.LENGTH_SHORT).show()
            }

        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        google_login_button.setOnClickListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val bounds = LatLngBounds(LatLng(49.459069, 13.602941), LatLng(55.383678, 25.182531))
        if (googleMap != null) {
            this.googleMap = googleMap
        }
        googleMap?.setOnMapLoadedCallback {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10))
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.searchButton -> onSearchClicked()
            R.id.google_login_button -> onGoogleLogin()
        }
    }

    private fun onSearchClicked() {
        if (!searchVisible) {
            searchInput.visibility = View.VISIBLE
            searchVisible = true
        } else {
            if (lastMarker != null) {
                lastMarker?.remove()
            }
            val pairing = plateNumberPairingRepository.plateNumberMappings[searchInput.text.toString().toUpperCase().split(" ")[0]]
            pairing?.let {
                noInputInfo.text = it.city
                lastMarker = googleMap.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                val bounds = LatLngBounds(LatLng(it.lat - 0.5, it.lng - 0.5), LatLng(it.lat + 0.5, it.lng + 0.5))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10))
            }
        }
    }

    private fun onGoogleLogin() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            RC_SIGN_IN -> {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
            }
            else -> {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            Toast.makeText(this, "Zalogowano Googlem", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("DonwaladoMainActivity", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Zjebano", Toast.LENGTH_SHORT).show()
        }
    }
}