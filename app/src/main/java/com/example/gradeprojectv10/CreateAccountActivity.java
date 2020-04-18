package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    private Button Registerbtn; // create Button variables to link with user layout
    private EditText accountEmail, accountPassword,  Confpassword; // create EditText variables to link with user layout
    private ProgressDialog loadingbar; // create loading bar
    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference userRef; // create variable to connect to the database
    private String userID; // store the customer ID
    String  checkFlag; // create strings to save the user id an user type
    Intent intent ; // get the user type from the previous activity



    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account); // link the activity to the xml layout

        mAuth = FirebaseAuth.getInstance(); //connect to Firebase

        // link the buttons, TextView and EditText to the user layout
        Registerbtn = (Button) findViewById( R.id.Account_btn );
        accountEmail = (EditText) findViewById( R.id.Account_email );
        accountPassword = (EditText) findViewById( R.id.Account_password );
        Confpassword = (EditText) findViewById( R.id.Account_confirm_password );
        loadingbar = new ProgressDialog( this );


        // if the customer click on register button
        Registerbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create string to get and store text of the email, password and confirm password
                String email = accountEmail.getText().toString();
                String password = accountPassword.getText().toString();
                String confPassword = Confpassword.getText().toString();

                // start register function with email , password and confirm password
                register(email, password, confPassword);
            }
        } );
    }

    // Function to get the user type
    private void userType() {
        intent = getIntent(); // check the order type from the previous activity
        checkFlag = intent.getStringExtra( "flag" ); // store the order type
    }

    // register function
    private void register(String email, String password, String confPassword) {


        if(email.isEmpty()){ // check if the field is empty
            accountEmail.setError( "Please enter Email" ); // display message to the user
            accountEmail.requestFocus(); // focus on the field
        }
        else if (password.isEmpty()){// check if the field is empty
            accountPassword.setError( "Please enter Password" ); // display message to the user
            accountPassword.requestFocus(); // focus on the field
        }
        else if (confPassword.isEmpty( )){ // check if the field is empty
            Confpassword.setError( "Please confirm Password" ); // display message to the user
            Confpassword.requestFocus();
        }
        else if (! password.equals( confPassword ) ){ // check if the passwords matches
            Confpassword.setError( "Passwords did not match" ); // display message to the user
            Confpassword.requestFocus(); // focus on the field
        }
        else {
            loadingbar.setTitle( "Create new account" ); // loading bar title
            loadingbar.setMessage( "please wait a moment.." ); // loading bar message
            loadingbar.show(); // show loading bar

            // create new user using createUser function
            mAuth.createUserWithEmailAndPassword( email, password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){ // if create new user successful
                        userID = mAuth.getCurrentUser().getUid(); // get the user id

                        // check if the user is a customer or driver
                        userType() ;

                        if(checkFlag.equals("Customer")) {
                            // Save the Customer ID to the database
                            userRef = FirebaseDatabase.getInstance().getReference().child( "Users" ).child( "Customers" ).child(userID);
                            userRef.setValue( true );
                        }

                        if(checkFlag.equals("Driver")) {
                            // Save the driver ID to the database
                            userRef = FirebaseDatabase.getInstance().getReference().child( "Users" ).child( "Drivers" ).child(userID);
                            userRef.setValue( true );

                        }

                        // create intent to send the user to Main activity
                        Intent i = new Intent( CreateAccountActivity.this, setUpActivity.class);
                        i.putExtra("flag", checkFlag); // add the user type to the intent
                        startActivity( i );  // send the user to the next class
                        finish(); // unable the user to go back to this page
                        // Display success message to the user
                        Toast.makeText( CreateAccountActivity.this, "New account created successfully..", Toast.LENGTH_SHORT ).show();
                        loadingbar.dismiss(); // dismiss the loading bar
                    }
                    else {
                        //if error occur
                        String message = task.getException().getMessage(); // get the error message
                        // Display error message to the user
                        Toast.makeText( CreateAccountActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();
                        loadingbar.dismiss();
                    }
                }
            } );
        }
    }


}
// End



