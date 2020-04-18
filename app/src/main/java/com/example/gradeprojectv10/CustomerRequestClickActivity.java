package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerRequestClickActivity extends AppCompatActivity {

    private TextView requestDescription ; // create TextView variables to link with user layout
    private Button deleteBtn, messageDriverBtn, driverLocationBtn; // create Button variables to link with user layout
    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference clickRequestRef, clickCurrentRef,driverNameRf; // create variables to connect to the database
    private String requestKey,description, currentUserID, checkFlag, DriverName, DriverID;
    Intent intent ; // get the order type from the previous activity


    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_customer_request_click ); // link the activity to the xml layout


        intent = getIntent(); // check the order type from the previous activity
        checkFlag = intent.getStringExtra( "flag" ); // store the order type
        requestKey = getIntent().getExtras().get( "requestKey" ).toString(); // store the request id


        //connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance(); // connect to Firebase
        currentUserID = mAuth.getCurrentUser().getUid(); // get the user ID
        clickRequestRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "New requests" ).child( requestKey ); // retrieve the new request from the database
        clickCurrentRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "Accepted requests" ).child( requestKey ); // retrieve the current request from the database
        driverNameRf = FirebaseDatabase.getInstance().getReference().child( "Users" ).child( "Drivers" ); // retrieve the driver name


        // link the buttons and TextView to the user layout
        requestDescription = (TextView) findViewById( R.id.click_description );
        deleteBtn = (Button) findViewById( R.id.delete_requestBtn );
        messageDriverBtn = (Button) findViewById( R.id.message_driver );
        driverLocationBtn = (Button) findViewById( R.id.driver_locationBtn );


        messageDriverBtn.setEnabled( false ); // disable edit on the button
        messageDriverBtn.setVisibility( View.INVISIBLE ); // make button invisible
        driverLocationBtn.setEnabled( false ); // disable edit on the button
        driverLocationBtn.setVisibility( View.INVISIBLE ); // make button invisible

    }


    // when the activity start
    protected void onStart() {
        super.onStart();

        // Display a new request or current request ?
        if(checkFlag != null && checkFlag.equals("Current order")) {
            CurrentOrder(); // if it's current order
        }
        else {
            NewOrder(); // if it's new order
        }

    }

    // retrieve and display new requests
    private void NewOrder() {

        // retrieve the information of the order from the database
        clickRequestRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild( "description" )) {
                    // get the request description
                    description = dataSnapshot.child( "description" ).getValue().toString();

                    // display the request description
                    requestDescription.setText( description );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        } );

        // if the user click on the delete button
        deleteBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRequestRef.removeValue(); // delete the request from the database

                // Display success message to the user
                Toast.makeText( CustomerRequestClickActivity.this, "Deleted successfully", Toast.LENGTH_SHORT ).show();

                // create intent to send the user to Main activity
                Intent MainIntent = new Intent( CustomerRequestClickActivity.this, MainActivity.class );
                MainIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity( MainIntent ); // send the user to the next class
                finish(); // unable the user to go back to this page
            }
        } );

    }

    // retrieve and display current requests
    private void CurrentOrder() {

        messageDriverBtn.setEnabled( true ); // enable edit on the button
        messageDriverBtn.setVisibility( View.VISIBLE ); // make button visible
        driverLocationBtn.setEnabled( true ); // enable edit on the button
        driverLocationBtn.setVisibility( View.VISIBLE ); // make button visible

        // retrieve the information of the order from the database
        clickCurrentRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild( "description" )) {
                    // get the request description
                    description = dataSnapshot.child( "description" ).getValue().toString();
                    DriverID = dataSnapshot.child( "Driver uid" ).getValue().toString();

                    // display the request description
                    requestDescription.setText( description );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        // change the text of delete button to complete "Delivery complete"
        deleteBtn.setText( "Delivery complete" );

        //if the user click on "Delivery complete"
        deleteBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCurrentRef.removeValue(); // delete the request from the database

                // Display success message to the user
                Toast.makeText( CustomerRequestClickActivity.this, "Deleted successfully", Toast.LENGTH_SHORT ).show();

                // create intent to send the user to Main activity
                Intent MainIntent = new Intent( CustomerRequestClickActivity.this, MainActivity.class );
                MainIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity( MainIntent ); // send the user to the next class
                finish(); // unable the user to go back to this page
            }
        } );


        // if the user click on message the driver button
        messageDriverBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get the driver name
                driverNameRf.child( DriverID ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild( "Username" )) {
                            // get the driver name
                            DriverName = dataSnapshot.child( "Username" ).getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                } );

                // create intent to send the user to Message activity
                Intent i = new Intent( CustomerRequestClickActivity.this, MessageActivity.class );
                i.putExtra( "userId", DriverID ); // add the driver ID to the intent
                i.putExtra( "userName", "Driver" ); //  // add the driver name to the intent
                startActivity( i ); // send the user to the next class
                finish(); // unable the user to go back to this page
            }
        } );

        // if the user click on driver location
        driverLocationBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( CustomerRequestClickActivity.this, DriversMapsActivity.class );
                i.putExtra( "Driver ID", DriverID );
                startActivity( i );
            }
        } );
    }
}
// end
