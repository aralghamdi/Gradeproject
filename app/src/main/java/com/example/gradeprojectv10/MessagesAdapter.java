package com.example.gradeprojectv10;

import android.app.Notification;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

// this class is to display the messages to the user
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Chats> userMessagesList ;
    private FirebaseAuth mAuth ; // create variable mAuth to connect to firebase
    private DatabaseReference usersDatabaseRef ; // create variable to connect to the database

    public MessagesAdapter (List<Chats> userMessagesList){
        this.userMessagesList = userMessagesList ;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText ;

        public MessageViewHolder(@NonNull View itemView) {
            super( itemView );
            // link the objects the user layout
            senderMessageText = (TextView) itemView.findViewById( R.id.senderMessage_text );
            receiverMessageText = (TextView) itemView.findViewById( R.id.receiverMessage_text );

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_layout, parent, false );
        mAuth = FirebaseAuth.getInstance(); // connect to the database
        return new MessageViewHolder( V );
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid(); // get the current user id
        Chats chats = userMessagesList.get( position );

        String fromUserID = chats.getFrom();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child( "Users" );

        holder.receiverMessageText.setVisibility( View.INVISIBLE );
        if (fromUserID.equals( messageSenderID )){ // retrieve sent messages
            holder.senderMessageText.setBackgroundResource( R.drawable.sender_messag_background );
            holder.senderMessageText.setTextColor( Color.WHITE );
            holder.senderMessageText.setGravity( Gravity.LEFT );
            holder.senderMessageText.setText( chats.getMessage() );
        }
        else{ // retrieve received messages
            holder.senderMessageText.setVisibility( View.INVISIBLE );
            holder.receiverMessageText.setVisibility( View.VISIBLE );
            holder.receiverMessageText.setBackgroundResource( R.drawable.receiver_message_background );
            holder.receiverMessageText.setTextColor( Color.WHITE );
            holder.receiverMessageText.setGravity( Gravity.LEFT );
            holder.receiverMessageText.setText( chats.getMessage() );

        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
// End
