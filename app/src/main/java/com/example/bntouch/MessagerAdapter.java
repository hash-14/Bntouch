package com.example.bntouch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagerAdapter extends RecyclerView.Adapter<MessagerAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagerAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("image")){
                        String receiverImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiver_profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(fromMessageType.equals("text")) {
            holder.receiver_message_text.setVisibility(View.INVISIBLE);
            holder.receiver_profile_image.setVisibility(View.INVISIBLE);
            holder.sender_message_text.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(messageSenderId)) {
                holder.sender_message_text.setVisibility(View.VISIBLE);
                holder.sender_message_text.setBackgroundResource(R.drawable.sender_message_layout);
                holder.sender_message_text.setTextColor(Color.BLACK);
                holder.sender_message_text.setText(messages.getMessage());
            } else {
                holder.receiver_message_text.setVisibility(View.VISIBLE);
                holder.receiver_profile_image.setVisibility(View.VISIBLE);

                holder.receiver_message_text.setBackgroundResource(R.drawable.reveiver_message_layout);
                holder.receiver_message_text.setTextColor(Color.BLACK);
                holder.receiver_message_text.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView receiver_message_text,sender_message_text;
        public CircleImageView receiver_profile_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sender_message_text = (TextView)itemView.findViewById(R.id.sender_message_text);
            receiver_message_text = (TextView)itemView.findViewById(R.id.receiver_message_text);
            receiver_profile_image = (CircleImageView)itemView.findViewById(R.id.receiver_profile_image);
        }
    }
}
