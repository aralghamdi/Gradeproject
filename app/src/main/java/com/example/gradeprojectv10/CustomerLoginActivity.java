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

public class CustomerLoginActivity extends AppCompatActivity {

    private Button customerLoginbtn; // create Button variables to link with user layout
    private TextView customerRegisterlink; // create TextView variables to link with user layout
    private EditText customerEmail, cutomerpassword ; // create EditText variables to link with user layout
    private ProgressDialog loadingbar; // create loading bar

    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase

    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_customer_login); // link the activity to the xml layout


        mAuth = FirebaseAuth.getInstance(); //connect to Firebase

        // link the buttons, TextView and EditText to the user layout
        customerLoginbtn = (Button) findViewById( R.id.login_customer_btn );
        customerRegisterlink = (TextView) findViewById( R.id.register_customer_link );
        customerEmail = (EditText) findViewById( R.id.customer_email );
        cutomerpassword = (EditText) findViewById( R.id.customer_password );
        loadingbar = new ProgressDialog( this );


        // if the user click on register link
        customerRegisterlink.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent to send the user to Main activity
                Intent i = new Intent( CustomerLoginActivity.this, CreateAccountActivity.class );
                i.putExtra("flag", "Customer"); // add the user type to the intent
                startActivity( i ); // send the user to the next class
            }
        } );


        // if the customer click on login button
        customerLoginbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // signOut if the program connected to previous user

                // create string to get and store text of the email and password
                String email = customerEmail.getText().toString();
                String password = cutomerpassword.getText().toString();

                if(email.isEmpty()){ // check if the field is empty
                    customerEmail.setError( "Please enter Email" ); // display message to the user
                    customerEmail.requestFocus(); // focus on the field
                }
                else if (password.isEmpty()){ // check if the field is empty
                    cutomerpassword.setError( "Please enter Password" ); // display message to the user
                    cutomerpassword.requestFocus(); // focus on the field
                }
                else {
                    loadingbar.setTitle( "Customer Login" ); // load bar title
                    loadingbar.setMessage( "please wait a moment.." ); // load bar message
                    loadingbar.show(); // show loading bar

                    // Login the user user SignIn function
                    mAuth.signInWithEmailAndPassword( email, password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() { // add complete listener to to check if login completed
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){ // if login Successful
                                // create intent to send the user to Main activity
                                Intent i = new Intent( CustomerLoginActivity.this, MainActivity.class );
                                i.putExtra("flag", "Customer"); // add the user type to the intent
                                startActivity( i ); // send the user to the next class
                                finish(); // unable the user to go back to this page

                                // Display success message to the user
                                Toast.makeText( CustomerLoginActivity.this, "Logged in successfully..", Toast.LENGTH_SHORT ).show();
                                loadingbar.dismiss(); // dismiss the loading bar
                            }
                            else {
                                //if error occur
                                String massage = task.getException().getMessage(); // get the error message
                                // Display error message to the user
                                Toast.makeText( CustomerLoginActivity.this, "Error" + massage, Toast.LENGTH_SHORT ).show();
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
