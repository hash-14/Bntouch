package com.example.bntouch;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatsView;

    private RecyclerView chats_list;
    private DatabaseReference chatsRef, usersRef, messagesRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        chats_list = (RecyclerView)privateChatsView.findViewById(R.id.chats_list);
        chats_list.setLayoutManager(new LinearLayoutManager(getContext()));
        currentUserID = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);
        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(chatsRef, Contacts.class)
                    .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder chatsViewHolder, final int position, @NonNull Contacts contacts) {
                final String userIDs = getRef(position).getKey();
                final String[] image = {"default_image"};

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            if(dataSnapshot.hasChild("image")) {
                                image[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image[0]).placeholder(R.drawable.profile_image).into(chatsViewHolder.profileImage);
                            }

                            final String username = dataSnapshot.child("name").getValue().toString();
                            chatsViewHolder.userName.setText(username);
                           /* chatsViewHolder.userStatus.setText("Last Seen: " + "\n" + "Date " + " Time");

                            if(dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                if(state.equals("online")) {
                                    chatsViewHolder.userStatus.setText("online");
                                } else if(state.equals("offline")) {
                                    chatsViewHolder.userStatus.setText("Last seen: " + date + " " + time);
                                }
                            } else {
                                chatsViewHolder.userStatus.setText("offline");
                            }*/

                            chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", userIDs);
                                    chatIntent.putExtra("visit_user_name", username);
                                    chatIntent.putExtra("visit_user_image", image[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                messagesRef.child(userIDs).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("message")) {
                                String setStatusToLastMessage = dataSnapshot.child("message").getValue().toString();
                                chatsViewHolder.userStatus.setText(setStatusToLastMessage);
                            }
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
                });
            }
            //LmQvER3d31OVOwT1Ls3
            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };

        chats_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName =  itemView.findViewById(R.id.user_name_find_friends);
            userStatus =  itemView.findViewById(R.id.user_status_find_friends);
            profileImage = itemView.findViewById(R.id.users_profile_image_find_friends);
        }
    }

}
