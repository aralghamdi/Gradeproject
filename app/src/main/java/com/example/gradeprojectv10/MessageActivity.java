package com.example.gradeprojectv10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar messsagesToolbar ;
    private ImageButton sendMessageBtn ;
    private EditText textBox ;
    private TextView userProfileName ;
    private RecyclerView userMessagesList ;
    private final List<Chats> messagesList = new ArrayList<>(  );
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter ;

    private FirebaseAuth mAuth ;
    private DatabaseReference rootRef ;
    private String messageReceiverID, messageReceiverName, messageSenderID, saveCurrentDate, saveCurrentTime ;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_message );

        // get the receiver user ID and name
        messageReceiverID = getIntent().getExtras().get( "userId" ).toString();
        messageReceiverName = getIntent().getExtras().get( "userName" ).toString();


        //connect the objects to the database reference
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();


        // page view
        messsagesToolbar = (Toolbar) findViewById( R.id.messages_bar_layout );
        setSupportActionBar( messsagesToolbar );
        getSupportActionBar().setTitle( "" );

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setDisplayShowCustomEnabled( true );
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View action_bar_view = layoutInflater.inflate( R.layout.messages_custom_bar, null );
        actionBar.setCustomView( action_bar_view );

        userProfileName = (TextView) findViewById( R.id.custom_profile_name );
        sendMessageBtn = (ImageButton) findViewById( R.id.send_message_btn );
        textBox = (EditText) findViewById( R.id.input_message );
        userProfileName.setText( messageReceiverName );
        messagesAdapter = new MessagesAdapter( messagesList );
        userMessagesList = (RecyclerView) findViewById( R.id.messages_list );
        linearLayoutManager = new LinearLayoutManager( this );
        userMessagesList.setHasFixedSize( true );
        userMessagesList.setLayoutManager( linearLayoutManager );
        userMessagesList.setAdapter( messagesAdapter );


        // send a message when user click send
        sendMessageBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        } );

        fetchMessages();
    }

    private void fetchMessages() {
        rootRef.child( "Messages" ).child( messageSenderID ).child( messageReceiverID ).addChildEventListener( new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    messagesList.add( chats );
                    messagesAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        } );
    }

    private void sendMessage() {
        String messageTesx = textBox.getText().toString();

        // check if the message box has text
        if(messageTesx.isEmpty()){ // if its empty
            textBox.setError( "Please enter your message" );
            textBox.requestFocus();
        }
        else {
            String message_senderRef = "Messages/" + messageSenderID + "/" + messageReceiverID ;
            String message_ReceiverRef = "Messages/"  + messageReceiverID + "/" + messageSenderID ;
            // create unique key for the message
            DatabaseReference user_messageKey = rootRef.child( "Messages" ).child( messageSenderID ).child( messageReceiverID ).push();

            String messagePush_id = user_messageKey.getKey(); // get the key


            // get the current date and time
            Calendar calDate = Calendar.getInstance();
            final SimpleDateFormat currentdate = new SimpleDateFormat( "dd-MM-yyyy" );
            saveCurrentDate = currentdate.format( calDate.getTime() );
            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat( "HH:mm aa" );
            saveCurrentTime = currentTime.format( calDate.getTime() );

            // create hash map to save the message information to the database
            Map messageTextBody = new HashMap(  );
            messageTextBody.put("message", messageTesx);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("from", messageSenderID);
            Map messageBodyDetails = new HashMap(  );
            messageBodyDetails.put(message_senderRef + "/" + messagePush_id , messageTextBody);
            messageBodyDetails.put(message_ReceiverRef + "/" + messagePush_id , messageTextBody);

            rootRef.updateChildren( messageBodyDetails ).addOnCompleteListener( new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){

                    }
                    else { // if error occur
                        // display error message to the user
                        String message = task.getException().getMessage();
                        Toast.makeText( MessageActivity.this, "Error:" + message, Toast.LENGTH_SHORT ).show();
                    }

                    // empty the textBox
                    textBox.setText( "" );
                }
            } );

        }
    }
}
// End
