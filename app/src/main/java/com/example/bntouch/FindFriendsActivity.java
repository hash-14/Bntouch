package com.example.bntouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar find_friends_toolbar;
    private RecyclerView find_friends_recycler_list;

    private DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        InitializeFields();
    }

    private void InitializeFields() {
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        find_friends_toolbar = (Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(find_friends_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        find_friends_recycler_list = (RecyclerView)findViewById(R.id.find_friends_recycler_list);
        find_friends_recycler_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                                                         .setQuery(usersRef, Contacts.class)
                                                         .build();
        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder findFriendViewHolder, final int position, @NonNull Contacts contacts) {
                findFriendViewHolder.user_name_find_friends.setText(contacts.getName());
                findFriendViewHolder.user_status_find_friends.setText(contacts.getStatus());
                if(contacts.getImage() != null && contacts.getImage() != "") {
                    Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(findFriendViewHolder.users_profile_image_find_friends);
                }
                findFriendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        find_friends_recycler_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView user_name_find_friends, user_status_find_friends;
        CircleImageView users_profile_image_find_friends;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name_find_friends = itemView.findViewById(R.id.user_name_find_friends);
            user_status_find_friends = itemView.findViewById(R.id.user_status_find_friends);
            users_profile_image_find_friends = itemView.findViewById(R.id.users_profile_image_find_friends);

        }
    }
}
