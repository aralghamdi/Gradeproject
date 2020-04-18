package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CustomerNewRequestActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    // create variables to get google maps services
    private GoogleMap mMap;
    GoogleApiClient googleApiClient ;

    // create variables to get the customer location
    Location lastLocation;
    LocationRequest locationRequest;

    private Button requestBtn ; // create Button variable to link with user layout
    private EditText request_description ; // create EditText variable to link with user layout
    private ProgressDialog loadingbar; // create loading bar

    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference customerRef , orderRef; // create variables to connect to the database
    private LatLng customerLocation ;  // create variables to retrieve the customer coordinates
    private String saveCurrentTime , saveCurrentDate, orderRandomKey, customerId, description ; // create strings

    // to check the location permissions
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_customer_new_request );


        //connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance(); //connect to Firebase
        customerId = mAuth.getCurrentUser().getUid(); // store the current user ID
        customerRef = FirebaseDatabase.getInstance().getReference().child( "Users" ).child( "Customers" ); // retrieve the customer reference from the database
        orderRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "New requests" ); // retrieve the new request from the database

        // link the buttons and TextView to the user layout
        requestBtn = (Button) findViewById( R.id.request_btn );
        request_description = (EditText) findViewById( R.id.description_text );

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );


        // when the customer click on send request button
        requestBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve the request description entered by the user
                description = request_description.getText().toString();

                // retrieve the customer current location
                customerLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                // if the description textBox is empty
                if (description.isEmpty()){
                    // Display message to the user
                    Toast.makeText( CustomerNewRequestActivity.this, "Please enter description for your request", Toast.LENGTH_SHORT ).show();
                }
                else {
                    //loadingbar.setTitle( "Uploading Order" );
                    //loadingbar.setMessage( "please wait a moment.." );
                    //loadingbar.show();

                    // get the current date
                    Calendar calDate = Calendar.getInstance();
                    //set the date format
                    final SimpleDateFormat currentdate = new SimpleDateFormat( "dd-MM-yyyy" );
                    // save current date
                    saveCurrentDate = currentdate.format( calDate.getTime() );

                    // get the current time
                    Calendar calTime = Calendar.getInstance();
                    //set the date format
                    SimpleDateFormat currentTime = new SimpleDateFormat( "HH:mm:ss" );
                    // save current date
                    saveCurrentTime = currentTime.format( calTime.getTime() );

                    // create random key for the request
                    orderRandomKey = saveCurrentDate + saveCurrentTime ;

                    // retrieve the customer information from the database
                    customerRef.child( customerId ).addValueEventListener( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){

                                // get and save the customer name and mobile number
                                String userName = dataSnapshot.child( "Username" ).getValue().toString();
                                String mobile = dataSnapshot.child( "MobileNumber" ).getValue().toString();

                                // create hash map to save the request information to the database
                                HashMap orderMap = new HashMap(  );
                                orderMap.put( "uid", customerId );
                                orderMap.put( "name", userName );
                                orderMap.put( "mobile", mobile );
                                orderMap.put( "date", saveCurrentDate );
                                orderMap.put( "time", saveCurrentTime );
                                orderMap.put( "description", description );
                                orderMap.put( "Latitude", lastLocation.getLatitude() );
                                orderMap.put( "Longitude", lastLocation.getLongitude() );

                                // save the request with a unique key made from the customer id and the current date and time
                                orderRef.child(customerId + orderRandomKey ).updateChildren( orderMap ).addOnCompleteListener( new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) { // if the data saved successfully
                                            // create intent to send the user to Main activity
                                            Intent i = new Intent( CustomerNewRequestActivity.this, MainActivity.class );
                                            i.putExtra("flag", "Customer"); // add the user type to the intent
                                            startActivity( i );  // send the user to the next class
                                            finish(); // unable the user to go back to this page
                                            // Display success message to the user
                                            Toast.makeText( CustomerNewRequestActivity.this, "Success.", Toast.LENGTH_SHORT ).show();
                                            //loadingbar.dismiss(); // dismiss the loading bar
                                        }
                                        else {
                                            //if error occur
                                            String message = task.getException().getMessage(); // get the error message
                                            // Display error message to the user
                                            Toast.makeText( CustomerNewRequestActivity.this, "Error." + message, Toast.LENGTH_SHORT ).show();
                                            //loadingbar.dismiss();
                                        }
                                    }
                                } );

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );
                }
            }
        } );
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }
        buildGoogleApiclint();
        //set current locatin
        mMap.setMyLocationEnabled( true );
    }

    @Override // map fragment
    public void onConnected(@Nullable Bundle bundle) {

        // sow the user the current location
        locationRequest = new LocationRequest();
        locationRequest.setInterval( 1000 );
        locationRequest.setFastestInterval( 1000 );
        locationRequest.setPriority( locationRequest.PRIORITY_HIGH_ACCURACY );

        // ask the user the location permissions
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, locationRequest, this );

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override // get the customer location function
    public void onLocationChanged(Location location) {
        lastLocation = location;
        // get the current location
        LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude());
        mMap.moveCamera( CameraUpdateFactory.newLatLng( latLng ) );
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 12 ) );

    }


    protected synchronized void buildGoogleApiclint(){
        googleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
