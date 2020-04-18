package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

   // create variables to link with user layout
    private NavigationView navigationView ;
    private DrawerLayout drawerLayout ;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView requestList ;
    private androidx.appcompat.widget.Toolbar mToolbar ;
    private CircleImageView navProfileImage ;
    private TextView navProfileUsename ;
    private ImageButton addNewOrderBtn;

    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference userRef, requestRef;  // create variables to connect to the database
    String currentUserID, checkFlag; // create strings to save the user id an user type




    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance(); // connect to the firebase
        currentUserID = mAuth.getCurrentUser().getUid(); // get the current user ID
        userRef = FirebaseDatabase.getInstance().getReference().child( "Users" ); // retrieve the users reference from the database
        requestRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "New requests" ); // retrieve new requests reference from the database

        // check if the user is a customer or driver
        userType();


        // link the objects the user layout
        mToolbar = (Toolbar) findViewById( R.id.main_page_toolbar );
        setSupportActionBar( mToolbar );
        getSupportActionBar().setTitle( "Home" );
        drawerLayout = (DrawerLayout) findViewById( R.id.drawer_Layout );
        actionBarDrawerToggle = new ActionBarDrawerToggle( MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close );
        drawerLayout.addDrawerListener( actionBarDrawerToggle );
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        navigationView =(NavigationView) findViewById( R.id.navigation_View );
        addNewOrderBtn = (ImageButton) findViewById( R.id.add_new_order );
        View navView = navigationView.inflateHeaderView( R.layout.navigation_header );
        navProfileImage = (CircleImageView) navView.findViewById( R.id.nav_profile_image );
        navProfileUsename = (TextView) navView.findViewById( R.id.nav_user_full_name);


        requestList = (RecyclerView) findViewById( R.id.Users_PostList );
        requestList.setHasFixedSize( true );
        LinearLayoutManager linearLayputManager = new LinearLayoutManager( this );
        linearLayputManager.setReverseLayout( true );
        linearLayputManager.setStackFromEnd( true );
        requestList.setLayoutManager( linearLayputManager );

        // when the customer click on new order image
        addNewOrderBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send the customer to new request page
                sendUserToRequests();
            }
        } );


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Display the side menu
        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);
                return false;
            }
        } );

        //check the user authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendUserToLogin();
        }

        if(checkFlag != null && checkFlag.equals("Driver")) { // if the user is a driver
            DisplayAllUsersRequests(); // display all the requests
        }
        else { // if the user is a customer
            DisplayCurrentUserRequests();  // display current customer requests when you open the app
        }

    }

    // Function to get the user type
    private void userType() {

        if (checkFlag == null ) {
            userRef.child( "Customers" ).addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild( currentUserID )) {
                        checkFlag = "Customer";
                        DisplayCustomerInfo();
                        DisplayCurrentUserRequests();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
        }
        if (checkFlag == null ) {
            userRef.child( "Drivers" ).addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild( currentUserID )) {
                        checkFlag = "Driver";
                        DisplayDriverInfo();
                        DisplayAllUsersRequests();

                        addNewOrderBtn.setVisibility(View.INVISIBLE);
                        addNewOrderBtn.setEnabled(false);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
        }
    }

    // link the RecyclerView to the user layout
    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        // create TextView variables and link it to the user layout to display the request information
        TextView userName , date , time, description ;

        public RequestsViewHolder(@NonNull View itemView) {
            super( itemView );
// link the variables to the xml file
            userName = itemView.findViewById( R.id.request_Username ); // display the user name for the request
            date = itemView.findViewById( R.id.request_date );// display the request date
            time = itemView.findViewById( R.id.request_time ); // display the request date
            description = itemView.findViewById( R.id.Request_description ); // display the request date

        }
    }


    // function to display the request for the driver
    private void DisplayAllUsersRequests() {
        // use the requestRef to retrieve the request from the data base
        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>().setQuery(requestRef, Requests.class).build();

        // use the class Requests to retrieve and store the request information from the database
        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestsViewHolder holder, final int position, @NonNull Requests model) {

                        // get the request information and display it to the user
                        holder.userName.setText( model.getName() );
                        holder.date.setText( model.getDate() );
                        holder.time.setText( model.getTime() );
                        holder.description.setText( model.getDescription() );
                        // if the driver click on a request, send the driver to the request page
                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String requestKey = getRef( position ).getKey(); // save the request id
                                Intent i = new Intent( MainActivity.this, DriverRequestClickActivity.class ); // create intent to send the user
                                i.putExtra( "requestKey", requestKey ); // add the request id to the intent
                                startActivity( i ); // send the user to the next class
                            }
                        } );


                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.requests_layout, parent, false );
                        RequestsViewHolder viewHolder = new RequestsViewHolder( view );
                        return viewHolder ;
                    }
                };

        requestList.setAdapter( adapter ); // display the request to the user
        adapter.startListening();
    }


    // function to display the request for the customer
    private void DisplayCurrentUserRequests() {
        // create query to only retrieve requests of the current customer user id
        Query currentUserRequests = requestRef.orderByChild( "uid" ).startAt( currentUserID ).endAt( currentUserID + "\uf8ff");
        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>().setQuery(currentUserRequests, Requests.class).build();

        // use the class Requests to retrieve and store the request information from the database
        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestsViewHolder holder, final int position, @NonNull Requests model) {

                        // get the request information and display it to the user
                        holder.userName.setText( model.getName() );
                        holder.date.setText( model.getDate() );
                        holder.time.setText( model.getTime() );
                        holder.description.setText( model.getDescription() );
                        // if the customer click on a request, send the customer to the request page
                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String requestKey = getRef( position ).getKey(); // save the request id
                                // create intent to send the user
                                Intent i = new Intent( MainActivity.this, CustomerRequestClickActivity.class );
                                i.putExtra( "requestKey", requestKey ); // add the request id to the intent
                                startActivity( i ); // send the user to the next class
                            }
                        } );


                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.requests_layout, parent, false );
                        RequestsViewHolder viewHolder = new RequestsViewHolder( view );
                        return viewHolder ;
                    }
                };

        requestList.setAdapter( adapter ); // display the request to the user
        adapter.startListening();
    }

    // retrieve and display the driver information
    private void DisplayDriverInfo() {
        userRef.child( "Drivers" ).child( currentUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve the user name and image from the data base
                if(dataSnapshot.exists()){
                    if (dataSnapshot.hasChild( "Username" )){
                        String Fullname = dataSnapshot.child( "Username" ).getValue().toString();
                        navProfileUsename.setText( Fullname );
                    }
                    if (dataSnapshot.hasChild( "profileImage" )) {
                        String Image = dataSnapshot.child( "profileImage" ).getValue().toString();
                        Picasso.get().load( Image ).placeholder( R.drawable.profile ).into( navProfileImage );
                    }
                    else {
                        Toast.makeText( MainActivity.this, "Profile name or picture dose not exist...", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }


    // retrieve and display the customer information
    private void DisplayCustomerInfo() {
        userRef.child( "Customers" ).child( currentUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve the use name and image from the data base
                if(dataSnapshot.exists()){
                    if (dataSnapshot.hasChild( "Username" )){
                        String Fullname = dataSnapshot.child( "Username" ).getValue().toString();
                        navProfileUsename.setText( Fullname );
                    }
                    if (dataSnapshot.hasChild( "profileImage" )) {
                        String Image = dataSnapshot.child( "profileImage" ).getValue().toString();
                        Picasso.get().load( Image ).placeholder( R.drawable.profile ).into( navProfileImage );
                    }
                    else {
                        Toast.makeText( MainActivity.this, "Profile name or pictuer dose not exist...", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }


    // function to send the user to the login page
    private void sendUserToLogin() {
        // create intent to send the user
        Intent loginIntent = new Intent( MainActivity.this, WelcomActivity.class );
        loginIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear this page
        startActivity( loginIntent ); // send the user to the next class
        finish(); // unable the user to go back to this page
    }


    // function to send the user to the request page
    private void sendUserToRequests() {
        // create intent to send the user
        Intent intent = new Intent( MainActivity.this, CustomerNewRequestActivity.class );
        startActivity( intent ); // send the user to the next class

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected( item )){  //side menu button
            return true;
        }
        return super.onOptionsItemSelected( item );
    }
    private void userMenuSelector(MenuItem item) {

        // set click listener for the side menu items
        switch (item.getItemId()){

            case R.id.nav_deliveries:
                Toast.makeText( this, "deliveries", Toast.LENGTH_SHORT).show();
                if(checkFlag != null && checkFlag.equals("Driver")) {
                    Intent i = new Intent( MainActivity.this, CurrentOrderActivity.class );
                    i.putExtra("flag", "Driver");
                    startActivity( i );
;                }
                else {
                    Intent i = new Intent( MainActivity.this, CurrentOrderActivity.class );
                    i.putExtra("flag", "Customer");
                    startActivity( i );
                }
                break;

            case R.id.nav_settings:
                Toast.makeText( this, "Settings", Toast.LENGTH_SHORT).show();
                if(checkFlag != null && checkFlag.equals("Driver")) {
                    Intent i = new Intent( MainActivity.this, SettingsActivity.class );
                    i.putExtra("flag", "Driver");
                    startActivity( i );
                    ;                }
                else {
                    Intent i = new Intent( MainActivity.this, SettingsActivity.class );
                    i.putExtra("flag", "Customer");
                    startActivity( i );
                }
                break;

            case R.id.nav_logOut:
                mAuth.signOut();
                sendUserToLogin();
                Toast.makeText( this, "Logout", Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
// End
