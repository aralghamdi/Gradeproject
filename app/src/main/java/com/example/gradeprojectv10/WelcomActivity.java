package com.example.gradeprojectv10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomActivity extends AppCompatActivity {

    // create variables to link with user layout
    private Button driverbtn ;
    private Button customerbtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_welcom );

        // link the objects the user layout
        driverbtn = (Button) findViewById( R.id.Driver_btn );
        customerbtn = (Button) findViewById( R.id.Customer_btn );

        // if the user click on "Customer" Button it will be directed to Customer login page
        customerbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent to send the user to Main activity
                Intent i = new Intent( WelcomActivity.this, CustomerLoginActivity.class );
                startActivity( i ); // send the user to the next class
            }
        } );

        // if the user press on "Driver" Button it will be directed to Driver login page
        driverbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent to send the user to Main activity
                Intent i = new Intent( WelcomActivity.this, DriverLoginActivity.class );
                startActivity( i ); // send the user to the next class
            }
        } );
    }
}
// End