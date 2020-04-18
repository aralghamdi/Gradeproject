package com.example.gradeprojectv10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override  // this class is to show the logo when the user open the app
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    // display this page for 3 seconds
                    sleep(3000); // Show the start screen for 3 seconds
                }
                catch (Exception e){
                   e.printStackTrace();
                }
                finally {
                    // send the user to the welcome page
                    Intent i = new Intent( SplashActivity.this, WelcomActivity.class );
                    startActivity(i);
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
// End