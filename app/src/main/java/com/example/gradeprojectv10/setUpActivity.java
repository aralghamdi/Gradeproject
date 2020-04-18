package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class setUpActivity extends AppCompatActivity {

    // create variables to link with user layout
    private EditText userName , mobileNumber , City ;
    private Button saveBtn ;
    private CircleImageView profileImage ;
    private FirebaseAuth mAtuth ;
    private DatabaseReference  customerRef, driverRef;
    private StorageReference userImageRef ;
    private String currentUserID;
    final static int galleryPick = 1;




    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        // connect the objects to the database reference
        mAtuth = FirebaseAuth.getInstance(); // connect to the firebase
        currentUserID = mAtuth.getCurrentUser().getUid(); // get the current user ID
        customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(currentUserID); // create reference to the Customers from the database
        driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(currentUserID); // create reference to the Drivers from the database
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images"); // create reference to the Profile Images from the database

        // link the objects the user layout
        userName = (EditText) findViewById(R.id.setup_username);
        mobileNumber = (EditText) findViewById(R.id.setup_mobile);
        City = (EditText) findViewById(R.id.setup_city);
        saveBtn = (Button) findViewById(R.id.setup_save);
        profileImage = (CircleImageView) findViewById(R.id.setUp_profileimage);


        // when the user click on Profile image
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // redirect the user to the gallery to select a pic
                Intent galleryIntet = new Intent();
                galleryIntet.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntet.setType("image/*");
                startActivityForResult(galleryIntet, galleryPick);
            }
        });


        // Display the customer profile image
        DisplayUserImage();

        // when the user click on save button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountInfo();

            }
        });
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


    @Override // image cropper
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            Uri ImagerUri = data.getData();  // get the image selected by the user

            // cropping the image
            CropImage.activity(ImagerUri).setGuidelines( CropImageView.Guidelines.ON ).setAspectRatio( 1,1 ).start( this );
        }

        // get the cropped image
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult( data );
            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                // save image in the database using the user ID
                final StorageReference fileBath = userImageRef.child( currentUserID + ".jpg");
                // if the image uploaded successfully
                fileBath.putFile( resultUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // get the link for the image
                        fileBath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                Intent intent = getIntent();
                                String checkFlag= intent.getStringExtra("flag");

                                // Display the image to the customer
                                if(checkFlag.equals("Customer")) {
                                    customerRef.child( "profileImage" ).setValue( downloadUrl ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                // refresh the page
                                                Intent i = new Intent( setUpActivity.this, setUpActivity.class );
                                                i.putExtra("flag", "Customer"); // add the user type to the intent
                                                startActivity( i );
                                                // display success message to the user
                                                Toast.makeText( setUpActivity.this, "Image saved successfully", Toast.LENGTH_SHORT ).show();
                                            }
                                            else { // if error occur
                                                // display error message to the user
                                                String message = task.getException().toString();
                                                Toast.makeText( setUpActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();
                                            }
                                        }
                                    } );
                                }

                                // Display the image for the driver
                                else if (checkFlag.equals("Driver")){ // if driver
                                    driverRef.child( "profileImage" ).setValue( downloadUrl ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                // refresh the page
                                                Intent i = new Intent( setUpActivity.this, setUpActivity.class );
                                                i.putExtra("flag", "Driver"); // add the user type to the intent
                                                startActivity( i );
                                                // display success message to the user
                                                Toast.makeText( setUpActivity.this, "Image saved successfully", Toast.LENGTH_SHORT ).show();
                                            }
                                            else { // if error occur
                                                // display error message to the user
                                                String message = task.getException().toString();
                                                Toast.makeText( setUpActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();
                                            }
                                        }
                                    } );}

                            }
                        });
                    }
                } );
            }
            else { // if the image cannot be cropped
                // display error message
                Toast.makeText( setUpActivity.this, "Error: Image can not be cropped, Please try again. ", Toast.LENGTH_SHORT ).show();
            }
        }
    }



    // function to save the user information
    private void saveAccountInfo() {

        // get the information entered by the user
        String username = userName.getText().toString();
        String mobile = mobileNumber.getText().toString();
        String city = City.getText().toString();

        //
        if(username.isEmpty()){ // check if the field is empty
            userName.setError( "Please enter full name" ); // display message to the user
            userName.requestFocus(); // focus on the field
        }
        else if(mobile.isEmpty()) { // check if the field is empty
            mobileNumber.setError( "Please enter mobile number" ); // display message to the user
            mobileNumber.requestFocus();
        }
        else if(city.isEmpty()){ // check if the field is empty
            City.setError( "Please enter your city" ); // display message to the user
            City.requestFocus(); // focus on the field
        }
        else {
            // create hash map with the user information
            HashMap userMap = new HashMap();
            userMap.put( "Username", username );
            userMap.put( "MobileNumber", mobile );
            userMap.put( "City", city );

            // Check if the user is customer or driver
            Intent intent = getIntent();
            String checkFlag= intent.getStringExtra("flag");
            if(checkFlag.equals("Customer")) { // if customer
                // save the information to the database
                customerRef.updateChildren( userMap ).addOnCompleteListener( new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // create intent to send the user to Main activity
                            Intent i = new Intent( setUpActivity.this, MainActivity.class);
                            i.putExtra("flag", "Customer"); // add the user type to the intent
                            startActivity( i );  // send the user to the next class
                            finish(); // unable the user to go back to this page
                            // Display success message to the user
                            Toast.makeText( setUpActivity.this, "Profile updated Successfully", Toast.LENGTH_LONG ).show();
                        } else {
                            //if error occur
                            String message = task.getException().getMessage(); // get the error message
                            // Display error message to the user
                            Toast.makeText( setUpActivity.this, "Error occur" + message, Toast.LENGTH_LONG ).show();
                        }
                    }
                } );
            }
            else if (checkFlag.equals("Driver")){ // if driver
                driverRef.updateChildren( userMap ).addOnCompleteListener( new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // create intent to send the user to Main activity
                            Intent i = new Intent( setUpActivity.this, MainActivity.class);
                            i.putExtra("flag", "Driver"); // add the user type to the intent
                            startActivity( i );  // send the user to the next class
                            finish(); // unable the user to go back to this page
                            // Display success message to the user
                            Toast.makeText( setUpActivity.this, "Profile updated Successfully", Toast.LENGTH_LONG ).show();
                        } else {
                            //if error occur
                            String message = task.getException().getMessage(); // get the error message
                            // Display error message to the user
                            Toast.makeText( setUpActivity.this, "Error occur" + message, Toast.LENGTH_LONG ).show();
                        }
                    }
                } );
            }
        }
    }


}
// End