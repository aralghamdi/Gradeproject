package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriversMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    // create variables to get google maps services
    private GoogleMap mMap;
    GoogleApiClient googleApiClient ;
    Location lastLocation;
    LocationRequest locationRequest;


    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference driverlocation ; // create variables to connect to the database
    private String driverID; // create string to store the driver id
    private Marker driverMarker; // create map marker
    Intent intent ; // get the driver id from the previous activity

    // to check the location permissions
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 99;


    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_drivers_maps );


        intent = getIntent(); // get the driver id from the previous activity
        driverID = intent.getStringExtra( "Driver ID" ); // store the driver id

        //connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance(); //connect to Firebase
        driverlocation = FirebaseDatabase.getInstance().getReference().child( "Driver Location" ); // retrieve the driver location from the database

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );
    }

    // retrieve driver location
    private void getDriverLocation() {
        driverlocation.child( driverID ).child( "l" ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0 ;
                    double loactionLng = 0 ;

                    // get the latitude
                    if (driverLocationMap.get( 0 ) != null){
                        locationLat = Double.parseDouble( driverLocationMap.get( 0 ).toString() );
                    }
                    // get the longitude
                    if (driverLocationMap.get( 1 ) != null){
                        loactionLng = Double.parseDouble( driverLocationMap.get( 1 ).toString() );
                    }

                    // Locate the driver on the map using a marker
                    LatLng driverLatLng = new LatLng( locationLat, loactionLng );
                    // in case of error getting a location
                    if ( driverMarker != null){
                        driverMarker.remove(); // dont shoe the marker
                    }

                    //show Driver location on the map using marker
                    driverMarker = mMap.addMarker( new MarkerOptions().position( driverLatLng ).title( "Driver" ).icon(BitmapDescriptorFactory.fromResource(R.drawable.carmarker)) );
                    mMap.moveCamera( CameraUpdateFactory.newLatLng( driverLatLng ) );
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 12 ) );


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiclint();

        // ask the user to grant location permissions
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }
        mMap.setMyLocationEnabled( true );
        //sow the driver current location
        getDriverLocation();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // show the user current location
        locationRequest = new LocationRequest();
        locationRequest.setInterval( 1000 );
        locationRequest.setFastestInterval( 1000 );
        locationRequest.setPriority( locationRequest.PRIORITY_HIGH_ACCURACY );

        // ask the user to grant location permissions
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, locationRequest, this );

    }

    // End


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
       if (getApplicationContext() != null ){
           lastLocation = location;
       }
    }

    protected synchronized void buildGoogleApiclint(){
        googleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
        googleApiClient.connect();
        }


}

