package com.example.bntouch;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bntouch.functionshelper.ChatRequestsHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestsFragmentView;
    private RecyclerView chat_requests_lists;

    private ChatRequestsHelper chatRequestsHelper;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, contactsRef;

    private String currentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chat_requests_lists = (RecyclerView) requestsFragmentView.findViewById(R.id.chat_requests_lists);
        chat_requests_lists.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        chatRequestsHelper = new ChatRequestsHelper(getResources().getString(R.string.request_type));

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestsHelper.getConnectionHandler().getDatabaseReference().child(currentUserID), Contacts.class)
                        .build();
        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder requestsViewHolder, final int position, @NonNull Contacts contacts) {
                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    if(type.equals("received")) {
                                        requestsViewHolder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);
                                        requestsViewHolder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(requestsViewHolder.profileImage);
                                                }
                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                                requestsViewHolder.userName.setText(requestUserName);
                                                requestsViewHolder.userStatus.setText("Wants to connect with you");

                                                requestsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + " Chat Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                switch(which){
                                                                    case 0://Accept
                                                                        contactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()) {
                                                                                    chatRequestsHelper.removeBothChatRequest(currentUserID, list_user_id);
                                                                                }
                                                                            }
                                                                        });
                                                                        contactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                }
                                                                            }
                                                                        });
                                                                        break;
                                                                    case 1://Cancel
                                                                        chatRequestsHelper.removeBothChatRequest(currentUserID, list_user_id);
                                                                        break;
                                                                    default:
                                                                        break;
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }

                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else if(type.equals("sent")) {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                       requestsViewHolder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);
                                                                       if (dataSnapshot.hasChild("image")) {
                                                                           final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                                                           Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(requestsViewHolder.profileImage);
                                                                       }
                                                                       final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                                                       requestsViewHolder.userName.setText(requestUserName);
                                                                       requestsViewHolder.userStatus.setText("Cacnel Request");
                                                                       requestsViewHolder.itemView.findViewById(R.id.request_cancel_button).setOnClickListener(new View.OnClickListener() {
                                                                           @Override
                                                                           public void onClick(View v) {
                                                                               chatRequestsHelper.removeBothChatRequest(currentUserID, list_user_id);
                                                                           }
                                                                       });
                                                                   }
                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                               });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;
                    }
                };
        chat_requests_lists.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public RequestsViewHolder(@NonNull View itemView){
            super(itemView);
            userName =  itemView.findViewById(R.id.user_name_find_friends);
            userStatus =  itemView.findViewById(R.id.user_status_find_friends);
            profileImage = itemView.findViewById(R.id.users_profile_image_find_friends);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_cancel_button);

        }
    }
}
