package com.example.womensafety;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CrowdedAreasMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowded_areas_map);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Example crowded areas with safety features
        LatLng marketSquare = new LatLng(37.7749, -122.4194); // Replace with real coordinates
        mMap.addMarker(new MarkerOptions()
                .position(marketSquare)
                .title("Market Square")
                .snippet("Crowded - Security Cameras Present")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        LatLng downtownPlaza = new LatLng(37.7850, -122.4060); // Replace with real coordinates
        mMap.addMarker(new MarkerOptions()
                .position(downtownPlaza)
                .title("Downtown Plaza")
                .snippet("Crowded - Police Patrols")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Center the map on the first crowded area
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marketSquare, 15));
    }
}