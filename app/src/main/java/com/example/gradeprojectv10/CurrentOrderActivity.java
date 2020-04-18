package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class CurrentOrderActivity extends AppCompatActivity {  // This class is to show the current orders for the customer and driver

    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference requestRef ; // create variable requestRef to retrieve the request ID
    private RecyclerView requestList ; // create requestList viewer to display the requests to the user
    private String currentUserID, userType; // create strings to store the user ID and the user type (Driver or customer)
    private Intent intent ; // get the user type from the previous activity
    private androidx.appcompat.widget.Toolbar mToolbar ;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override // create the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_current_order ); // link the activity to the xml layout



        intent = getIntent(); // check the user type from the previous activity
        userType = intent.getStringExtra("flag"); // store the user type


        //connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance(); // connect to the firebase
        currentUserID = mAuth.getCurrentUser().getUid(); // store the current user ID
        requestRef = FirebaseDatabase.getInstance().getReference().child( "Requests" ).child( "Accepted requests" );; // retrieve and store the request ID from the database


        //Page view
        requestList = (RecyclerView) findViewById( R.id.Current_requestList ); // link RecyclerView to the user layout
        requestList.setHasFixedSize( true ); // let the RecyclerView keep the same size
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( this ); // create RecyclerView manager
        linearLayoutManager.setReverseLayout( true ); // the oldest item is display at the end of the RecyclerView
        linearLayoutManager.setStackFromEnd( true ); // display the items from the end
        requestList.setLayoutManager( linearLayoutManager ); // set the RecyclerView manager
    }

    // when the activity start
    protected void onStart() {
        super.onStart();

        // Display the requests for the driver or customer
        if(userType != null && userType.equals("Driver")) { // if the current user is Driver
            DriverRequests(); // display current requests for the driver
        }
        else { // if the current user is Driver
            CustomerRequests();  // display current requests for the Customer
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
            date = itemView.findViewById( R.id.request_date ); // display the request date
            time = itemView.findViewById( R.id.request_time ); // display the request date
            description = itemView.findViewById( R.id.Request_description ); // display the request date

        }
    }


    // function to display the request for the driver
    private void DriverRequests() {
        Query currentUserRequests = requestRef.orderByChild( "Driver uid" ).startAt( currentUserID ).endAt( currentUserID + "\uf8ff");
        // use the requestRef to retrieve the request from the database
        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>().setQuery(currentUserRequests, Requests.class).build();

        // use the class Requests to retrieve and store the request information from the database
        FirebaseRecyclerAdapter<Requests, CurrentOrderActivity.RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, CurrentOrderActivity.RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CurrentOrderActivity.RequestsViewHolder holder, final int position, @NonNull Requests model) {

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
                                Intent i = new Intent( CurrentOrderActivity.this, DriverRequestClickActivity.class ); // create intent to send the user
                                i.putExtra( "requestKey", requestKey ); // add the request id to the intent
                                i.putExtra( "flag", "Current order" ); // add the request type to the intent
                                startActivity( i ); // send the user to the next class
                            }
                        } );


                    }

                    @NonNull
                    @Override
                    public CurrentOrderActivity.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.requests_layout, parent, false );
                        CurrentOrderActivity.RequestsViewHolder viewHolder = new CurrentOrderActivity.RequestsViewHolder( view );
                        return viewHolder ;
                    }
                };

        requestList.setAdapter( adapter ); // display the request to the user
        adapter.startListening();
    }


    // function to display the request for the customer
    private void CustomerRequests() {

        // create query to only retrieve requests of the current customer user id
        Query currentUserRequests = requestRef.orderByChild( "uid" ).startAt( currentUserID ).endAt( currentUserID + "\uf8ff");
        // use the requestRef to retrieve the request from the database
        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>().setQuery(currentUserRequests, Requests.class).build();

        // use the class Requests to retrieve and store the request information from the database
        FirebaseRecyclerAdapter<Requests, CurrentOrderActivity.RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, CurrentOrderActivity.RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CurrentOrderActivity.RequestsViewHolder holder, final int position, @NonNull Requests model) {

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
                                Intent i = new Intent( CurrentOrderActivity.this, CustomerRequestClickActivity.class );
                                i.putExtra( "requestKey", requestKey );  // add the request id to the intent
                                i.putExtra( "flag", "Current order" ); // add the request type to the intent
                                startActivity( i ); // send the user to the next class
                            }
                        } );


                    }

                    @NonNull
                    @Override
                    public CurrentOrderActivity.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.requests_layout, parent, false );
                        CurrentOrderActivity.RequestsViewHolder viewHolder = new CurrentOrderActivity.RequestsViewHolder( view );
                        return viewHolder ;
                    }
                };

        requestList.setAdapter( adapter ); // display the request to the user
        adapter.startListening();
    }


}
// End