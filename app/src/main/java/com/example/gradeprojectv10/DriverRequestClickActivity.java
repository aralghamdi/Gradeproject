package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DriverRequestClickActivity extends AppCompatActivity implements LocationListener {

    private TextView customerName, customerMobile, requestDescription; // create TextView variables to link with user layout
    private Button locationBtn, acceptBtn, completeBtn, messageBtn; // create Button variables to link with user layout
    private String requestKey, uid, description, userName, userMobile, currentUserID, latitude, longitude, checkFlag;

    private FirebaseAuth mAuth; // create variable mAuth to connect to firebase
    private DatabaseReference clickRequestRef, AccedptedRequestsRef, driverLocation; // create variables to connect to the database

    Intent intent; // get the order type from the previous activity
    LocationManager locationManager; // create location manager variable
    Location lastLocation; // create location variable
    LocationListener locationListener; // create location listener variable

    // check location permissions
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 99;


    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_driver_request_click ); // link the activity to the xml layout


        intent = getIntent(); // check the order type from the previous activity
        checkFlag = intent.getStringExtra( "flag" ); // store the order type
        requestKey = getIntent().getExtras().get( "requestKey" ).toString(); // store the request id


        //connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance(); //connect to Firebase
        currentUserID = mAuth.getCurrentUser().getUid();
        clickRequestRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "New requests" ).child( requestKey ); // retrieve the new request from the database
        AccedptedRequestsRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "Accepted requests" ).child( requestKey ); ; // save accepted order to database
        driverLocation = FirebaseDatabase.getInstance().getReference().child( "Driver Location" );


        // link the buttons and TextView to the user layout
        customerName = (TextView) findViewById( R.id.request_customer_name );
        customerMobile = (TextView) findViewById( R.id.request_customer_mobile );
        requestDescription = (TextView) findViewById( R.id.driver_click_description );
        locationBtn = (Button) findViewById( R.id.customer_locationBtn );
        acceptBtn = (Button) findViewById( R.id.accept_request );
        completeBtn = (Button) findViewById( R.id.delivery_completeBtn );
        messageBtn = (Button) findViewById( R.id.message_customerBtn );
        completeBtn.setEnabled( false ); // disable edit on the button
        completeBtn.setVisibility( View.INVISIBLE ); // make button invisible
        messageBtn.setEnabled( false ); // disable edit on the button
        messageBtn.setVisibility( View.INVISIBLE ); // make button invisible

        // link location manager to location services
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        // ask the user to grant location permissions
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this );

    }

    // when the activity start
    protected void onStart() {
        super.onStart();

        // Display a new request or current request ?
        if(checkFlag != null && checkFlag.equals("Current order")) {
            CurrentOrder(); // if it's current order
        }
        else {
            NewOrder();  // if it's new order
        }



    }

    // show the current orders
    private void CurrentOrder() {

        acceptBtn.setEnabled( false );
        acceptBtn.setVisibility( View.INVISIBLE );
        completeBtn.setEnabled( true );
        completeBtn.setVisibility( View.VISIBLE );
        messageBtn.setEnabled( true );
        messageBtn.setVisibility( View.VISIBLE );

        // retrieve the information of the order from the database
        AccedptedRequestsRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild( "uid" ) && dataSnapshot.hasChild( "description" ) && dataSnapshot.hasChild( "name" ) && dataSnapshot.hasChild( "mobile" ) && dataSnapshot.hasChild( "Latitude" ) && dataSnapshot.hasChild( "Longitude" ) ) {
                    uid = dataSnapshot.child( "uid" ).getValue().toString();
                    userName = dataSnapshot.child( "name" ).getValue().toString();
                    userMobile = dataSnapshot.child( "mobile" ).getValue().toString();
                    description = dataSnapshot.child( "description" ).getValue().toString();
                    latitude = dataSnapshot.child( "Latitude" ).getValue().toString();
                    longitude = dataSnapshot.child( "Longitude" ).getValue().toString();

                    customerName.setText( userName );
                    customerMobile.setText( userMobile );
                    requestDescription.setText( description );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );


        // when the driver click on customer location button
        locationBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the location of the customer and open it on google maps
                String uriBegin = "geo:" + latitude + "," + longitude;
                String query = latitude + "," + longitude + "(Customer location)";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                Uri uri = Uri.parse(uriString);
                // create intent to send the user to google maps
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        } );

        // when the driver click on complete button
        completeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove the request from current requests list
                AccedptedRequestsRef.removeValue();
                // create intent to send the user to Main activity
                Intent i = new Intent( DriverRequestClickActivity.this, MainActivity.class );
                i.putExtra("flag", "Driver"); // add the user type to the intent
                startActivity( i ); // send the user to the next class
                finish();
            }
        } );

        // when the user click on message customer button
        messageBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent to send the user to messages page
                Intent i = new Intent( DriverRequestClickActivity.this, MessageActivity.class);
                i.putExtra( "userId", uid ); // add the customer id to the intent
                i.putExtra( "userName", userName ); // add the customer name to the intent
                startActivity( i ); // send the user to the next class
            }
        } );

    }

    // retrieve and display new requests
    private void NewOrder() {

        // retrieve the information of the order from the database
        clickRequestRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // get the request description information
                if (dataSnapshot.hasChild( "uid" ) && dataSnapshot.hasChild( "description" ) && dataSnapshot.hasChild( "name" ) && dataSnapshot.hasChild( "mobile" ) && dataSnapshot.hasChild( "Latitude" ) && dataSnapshot.hasChild( "Longitude" ) ) {
                    uid = dataSnapshot.child( "uid" ).getValue().toString();
                    userName = dataSnapshot.child( "name" ).getValue().toString();
                    userMobile = dataSnapshot.child( "mobile" ).getValue().toString();
                    description = dataSnapshot.child( "description" ).getValue().toString();
                    latitude = dataSnapshot.child( "Latitude" ).getValue().toString();
                    longitude = dataSnapshot.child( "Longitude" ).getValue().toString();

                    // display the request information
                    customerName.setText( userName );
                    customerMobile.setText( userMobile );
                    requestDescription.setText( description );
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );


        // get the location of the customer and show it on google maps
        locationBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uriBegin = "geo:" + latitude + "," + longitude;
                String query = latitude + "," + longitude + "(Customer location)";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        } );


        // if user click on accept the order
        acceptBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create hash map to store accepted request information and add the Driver id and driver location
                HashMap orderMap = new HashMap(  );
                orderMap.put( "Driver uid", currentUserID );
                orderMap.put( "uid", uid );
                orderMap.put( "name", userName );
                orderMap.put( "mobile", userMobile );
                orderMap.put( "description", description );
                orderMap.put( "Latitude", latitude );
                orderMap.put( "Longitude", longitude );

                // save accepted request to the "Accepted requests" list
                AccedptedRequestsRef.updateChildren( orderMap ).addOnCompleteListener( new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) { // if data saved successfully
                            // Display success message to the user
                            Toast.makeText( DriverRequestClickActivity.this, "Success.", Toast.LENGTH_SHORT ).show();

                            // create intent to send the user to Main activity
                            Intent i = new Intent( DriverRequestClickActivity.this, MainActivity.class);
                            i.putExtra("flag", "Driver"); // add the user type to the intent
                            startActivity( i );  // send the user to the next class
                            finish(); // unable the user to go back to this page

                        }
                        else {  //if error occur
                            String massage = task.getException().getMessage(); // get the error message
                            // Display error message to the user
                            Toast.makeText( DriverRequestClickActivity.this,"Error" + massage, Toast.LENGTH_SHORT ).show();

                        }
                    }
                } );

                // remove the request form new list
                clickRequestRef.removeValue();
                // create intent to send the user to Main activity
                Intent i = new Intent( DriverRequestClickActivity.this, MainActivity.class  );
                i.putExtra("flag", "Driver"); // add the user type to the intent
                startActivity( i ); // send the user to the next class
                finish(); // unable the user to go back to this page

            }
        } );

    }



    @Override// get the driver current location
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null ) {
            lastLocation = location;

            GeoFire geoFire = new GeoFire( driverLocation );
            geoFire.setLocation( currentUserID, new GeoLocation( location.getLatitude(), location.getLongitude() ) );
        }
    }

    // End


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

