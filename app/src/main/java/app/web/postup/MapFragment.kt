package app.web.postup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.web.postup.R
import timber.log.Timber


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    lateinit var googleMap:GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var googleMapFragment: SupportMapFragment
    private lateinit var viewModel : ViewModel

    private lateinit var sendText : EditText
    private lateinit var sendButton:Button

    companion object {
        val instance = MapFragment()
//        private fun newInstance() = MapFragment()
    }

    override fun onMapReady(map : GoogleMap) {

        googleMap = map
        googleMap.run{
            uiSettings.isZoomControlsEnabled = true
            setOnMarkerClickListener(this@MapFragment)
            //googleMap.setPadding(left, top, right, bottom);
        }
        googleMap.isMyLocationEnabled = true
        setCurrenLocation()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_fragment, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

//        googleMapFragment = activity!!.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        googleMapFragment = childFragmentManager.fragments[0] as SupportMapFragment
        googleMapFragment.getMapAsync(this)

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.postList.observe(activity!!, Observer{
            for (item in it) {
                Timber.d("hh ${item.text}")
                googleMap.addMarker(
                    MarkerOptions().position(LatLng(item.location.lat.toDouble(), item.location.lng.toDouble()))
                        .title(item.userName)
                        .snippet(item.text)
                )
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(item.location.lat, item.location.lng), 17f))
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sendText = view.findViewById(R.id.to_send_text)
        sendButton = view.findViewById(R.id.send_button)
        sendButton.setOnClickListener{
            onClickSendButton(sendText)
        }
        super.onViewCreated(view, savedInstanceState)
    }
    fun setCurrenLocation(){

        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3

            location.let{
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                viewModel.getPostByRangeFromHere(lastLocation.latitude, lastLocation.longitude, 0.001)
//                tempGetPost()

            }
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

    }
    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let{
            marker.showInfoWindow()

        }
        return false
    }
     fun onClickSendButton(v : View){
        val text = sendText.text.toString()
        if(!text.isBlank()){
            viewModel.addPost(text,lastLocation.latitude.toFloat(), lastLocation.longitude.toFloat())
            sendText.setText("")
        }

    }

}

