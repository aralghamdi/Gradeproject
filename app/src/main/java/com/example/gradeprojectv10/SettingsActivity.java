package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    // create variables to link with user layout
    private TextView editProfile ;
    private EditText userName , mobileNumber , City ;
    private CircleImageView profileImage ;
    private FirebaseAuth mAtuth ;
    private DatabaseReference customerRef, driverRef;
    private StorageReference userImageRef ;
    private String currentUserID;
    final static int galleryPick = 1;

    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // connect the objects to the database reference
        mAtuth = FirebaseAuth.getInstance(); // connect to the firebase
        currentUserID = mAtuth.getCurrentUser().getUid(); // get the current user ID
        customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(currentUserID); // create reference to the Customers from the database
        driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(currentUserID); // create reference to the Drivers from the database
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images"); // create reference to the Profile Images from the database

        // link the objects the user layout
        userName = (EditText) findViewById(R.id.settings_username);
        mobileNumber = (EditText) findViewById(R.id.settings_mobile);
        City = (EditText) findViewById(R.id.settings_city);
        editProfile = (TextView) findViewById(R.id.edit_settings);
        profileImage = (CircleImageView) findViewById(R.id.settings_profileimage);


        // Display the user profile image
        DisplayUserImage();

        // Display the user profile image
        DisplayUserInfo();


        // when th user click on edit profile information
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user is customer or driver
                Intent intent = getIntent();
                String checkFlag= intent.getStringExtra("flag");
                if(checkFlag.equals("Customer")) { // if customer
                    // create intent to send the user to Main activity
                    Intent i = new Intent( SettingsActivity.this, setUpActivity.class);
                    i.putExtra("flag", "Customer"); // add the user type to the intent
                    startActivity( i );  // send the user to the next class
                    finish(); // unable the user to go back to this page
                }
                else if(checkFlag.equals("Driver")) { // if Driver
                    // create intent to send the user to Main activity
                    Intent i = new Intent( SettingsActivity.this, setUpActivity.class);
                    i.putExtra("flag", "Driver"); // add the user type to the intent
                    startActivity( i );  // send the user to the next class
                    finish(); // unable the user to go back to this page
                }
            }
        });


        // Display the customer profile image
        DisplayUserImage();


    }

    private void DisplayUserInfo() {

        // Check if the user is customer or driver
        Intent intent = getIntent();
        String checkFlag= intent.getStringExtra("flag");
        if(checkFlag.equals("Customer")) { // if customer
            // retrieve the information from the database
           customerRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.hasChild("Username") && dataSnapshot.hasChild("MobileNumber") && dataSnapshot.hasChild("City")){
                       String name = dataSnapshot.child( "Username" ).getValue().toString();
                       String mobile = dataSnapshot.child("MobileNumber").getValue().toString();
                       String city = dataSnapshot.child("City").getValue().toString();

                       // display the information to the user
                       userName.setText(name);
                       mobileNumber.setText(mobile);
                       City.setText(city);
                   }
               }
               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
        }
        else if(checkFlag.equals("Driver")) { // if Driver
            // retrieve the information from the database
            driverRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Username") && dataSnapshot.hasChild("MobileNumber") && dataSnapshot.hasChild("City")){
                        String name = dataSnapshot.child( "Username" ).getValue().toString();
                        String mobile = dataSnapshot.child("MobileNumber").getValue().toString();
                        String city = dataSnapshot.child("City").getValue().toString();

                        // display the information to the user
                        userName.setText(name);
                        mobileNumber.setText(mobile);
                        City.setText(city);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    // Function to display the user image
    private void DisplayUserImage() {
        Intent intent = getIntent();
        String checkFlag = intent.getStringExtra("flag");
        if (checkFlag.equals("Customer")) { // if customer
            // retrieve the image from the database
            customerRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("profileImage")) {
                            // store the image
                            String Image = dataSnapshot.child("profileImage").getValue().toString();
                            // display the image to the user
                            Picasso.get().load(Image).placeholder(R.drawable.profile).into(profileImage);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        // Display the customer profile image
        else if (checkFlag.equals("Driver")) { // if driver
            // retrieve the image from the database
            driverRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("profileImage")) {
                            // store the image
                            String Image = dataSnapshot.child("profileImage").getValue().toString();
                            // display the image to the user
                            Picasso.get().load(Image).placeholder(R.drawable.profile).into(profileImage);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }


}
// End