package com.example.mapArduino;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import app.akexorcist.bluetotohspp.library.DeviceList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivityold extends FragmentActivity implements OnMapReadyCallback {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    public static boolean mLocationPermissionGranted = false;
    GoogleMap map;
    Button connect_btn;
    Button marker_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect_btn = findViewById(R.id.connect);
        marker_btn = findViewById(R.id.darkModeToggle);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent blueIntent = new Intent(getApplicationContext(), DeviceList.class);
                startActivity(blueIntent);
            }
        });

        marker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                addNewMarker("NewMarker", Float.parseFloat(latx.getText().toString()), Float.parseFloat(laty.getText().toString()));

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json_dark)));
        addNewMarker("Jerusalem", 31.746421, 35.239667);
        map.addMarker(new MarkerOptions().position(new LatLng(31.746421, 0.0)).title("Not jerusalem"));
    }


    public void addNewMarker(String name, double latX, double latY){
        LatLng pos = new LatLng(latX, latY);
        map.addMarker(new MarkerOptions().position(pos).title(name));
        map.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }


}
