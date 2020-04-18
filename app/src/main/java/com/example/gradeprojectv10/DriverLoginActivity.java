package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class DriverLoginActivity extends AppCompatActivity {

    private Button driverLoginbtn; // create Button variables to link with user layout
    private TextView driverRegisterlink; // create TextView variables to link with user layout
    private EditText driverEmail, driverpassword; // create EditText variables to link with user layout
    private ProgressDialog loadingbar; // create loading bar

    private FirebaseAuth mAuth ; // create variable mAuth to get the user ID
    private DatabaseReference driverRef; // create variable customerRef to connect to the database
    private String driverID ; // store the driver ID

    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_driver_login); // link the activity to the xml layout


        mAuth = FirebaseAuth.getInstance(); // connect to Firebase

        // link the buttons, TextView and EditText to the user layout
        driverLoginbtn = (Button) findViewById( R.id.login_driver_btn );
        driverRegisterlink = (TextView) findViewById( R.id.register_driver_link );
        driverEmail = (EditText) findViewById( R.id.driver_email );
        driverpassword = (EditText) findViewById( R.id.driver_password );
        loadingbar = new ProgressDialog( this );


        // if the user click on register link
        driverRegisterlink.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( DriverLoginActivity.this, CreateAccountActivity.class );
                i.putExtra("flag", "Driver"); // add the user type to the intent
                startActivity( i ); // send the user to the next class
            }
        } );


        // if the driver click on login button
        driverLoginbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // signOut if the program connected to previous user

                // create string to get and store text of the email and password
                String email = driverEmail.getText().toString();
                String password = driverpassword.getText().toString();

                if(email.isEmpty()){  // check if the field is empty
                    driverEmail.setError( "Please enter Email" );  // display message to the user
                    driverEmail.requestFocus(); // focus on the field
                }
                else if (password.isEmpty()){  // check if the field is empty
                    driverpassword.setError( "Please enter Password" ); // display message to the user
                    driverpassword.requestFocus(); // focus on the field
                }
                else {
                    loadingbar.setTitle( "Driver Login" ); // loading bar title
                    loadingbar.setMessage( "please wait a moment.." ); // loading bar message
                    loadingbar.show(); // show loading bar

                    // Login the user user SignIn function
                    mAuth.signInWithEmailAndPassword( email, password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() { // add complete listener to to check if login completed
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){ // if login Successful
                                // create intent to send the user to Main activity
                                Intent i = new Intent( DriverLoginActivity.this, MainActivity.class );
                                i.putExtra("flag", "Driver"); // add the user type to the intent
                                startActivity( i ); // send the user to the next class
                                finish();

                                // Display success message to the user
                                Toast.makeText( DriverLoginActivity.this, "Logged in successfully..", Toast.LENGTH_SHORT ).show();
                                loadingbar.dismiss(); // dismiss the loading bar
                            }
                            else {
                                //if error occur
                                String massage = task.getException().getMessage(); // get the error message
                                // Display error message to the user
                                Toast.makeText( DriverLoginActivity.this, "Error" + massage, Toast.LENGTH_SHORT ).show();
                                loadingbar.dismiss(); // dismiss the loading bar
                            }
                        }
                    } );
                }
            }
        } );
    }
}
// end