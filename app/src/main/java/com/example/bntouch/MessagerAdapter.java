package com.example.bntouch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagerAdapter extends RecyclerView.Adapter<MessagerAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private LinkedHashMap<String, Messages> userHashMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private static final String FILE_URL_IMAGE = "https://firebasestorage.googleapis.com/v0/b/bntouch-775e8.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=b497af95-451d-4352-91e3-0404b62a1be6";
    public MessagerAdapter(List<Messages> userMessagesList, LinkedHashMap<String, Messages> userHashMessages) {
        this.userMessagesList = userMessagesList;
        this.userHashMessages = userHashMessages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
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

        holder.receiver_message_text.setVisibility(View.GONE);
        holder.receiver_profile_image.setVisibility(View.GONE);
        holder.sender_message_text.setVisibility(View.GONE);
        holder.message_sender_image_view.setVisibility(View.GONE);
        holder.message_receiver_image_view.setVisibility(View.GONE);



        if(fromMessageType.equals("text")) {

            if(fromUserID.equals(messageSenderId)) {
                holder.sender_message_text.setVisibility(View.VISIBLE);
                holder.sender_message_text.setBackgroundResource(R.drawable.sender_message_layout);
                holder.sender_message_text.setTextColor(Color.BLACK);
                holder.sender_message_text.setText(messages.getMessage() +  "\n\n" + messages.getTime() + "-" +messages.getDate());

            } else {
                holder.receiver_message_text.setVisibility(View.VISIBLE);
                holder.receiver_profile_image.setVisibility(View.VISIBLE);

                holder.receiver_message_text.setBackgroundResource(R.drawable.reveiver_message_layout);
                holder.receiver_message_text.setTextColor(Color.BLACK);
                holder.receiver_message_text.setText(messages.getMessage() +  "\n\n" + messages.getTime() + "-" +messages.getDate());
            }
        } else if(fromMessageType.equals("image")) {
            if(fromUserID.equals(messageSenderId)) {
                holder.message_sender_image_view.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.message_sender_image_view);
            } else {
                holder.receiver_profile_image.setVisibility(View.VISIBLE);
                holder.message_receiver_image_view.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.message_receiver_image_view);
            }
        } else  if(fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if(fromUserID.equals(messageSenderId)) {
                holder.message_sender_image_view.setVisibility(View.VISIBLE);

                Picasso.get().load(FILE_URL_IMAGE).into(holder.message_sender_image_view);

            } else {
                holder.receiver_profile_image.setVisibility(View.VISIBLE);
                holder.message_receiver_image_view.setVisibility(View.VISIBLE);

                Picasso.get().load(FILE_URL_IMAGE).into(holder.message_receiver_image_view);
            }
        }
        if(fromUserID.equals(messageSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View This Document",
                                        "Cancel",
                                        "Delete For Everyone"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        DeleteSentMessages(position, holder);
                                        break;
                                    case 1:
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        DeleteMessagesForEveryone(position, holder);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    } else if(userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete For Everyone"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        DeleteSentMessages(position, holder);
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        DeleteMessagesForEveryone(position, holder);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    } else  if(userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete For Everyone"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        DeleteSentMessages(position, holder);
                                        break;
                                    case 1:
                                        Intent intentImageViewer = new Intent(holder.itemView.getContext(), ImageViewrActivity.class);
                                        intentImageViewer.putExtra("imageUrl", userMessagesList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intentImageViewer);
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        DeleteMessagesForEveryone(position, holder);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View This Document",
                                        "Cancel"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        DeleteReceivedMessages(position, holder);
                                        break;
                                    case 1:
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                        break;
                                    case 2:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    } else if(userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        DeleteReceivedMessages(position, holder);
                                        break;
                                    case 1:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    } else  if(userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        DeleteReceivedMessages(position, holder);
                                        break;
                                    case 1:
                                        Intent intentImageViewer = new Intent(holder.itemView.getContext(), ImageViewrActivity.class);
                                        intentImageViewer.putExtra("imageUrl", userMessagesList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intentImageViewer);
                                        break;
                                    case 2:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void onClickItemsInChat(final String fromUserID, final String senderUserID, @NonNull final MessageViewHolder holder, final int position) {

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView receiver_message_text,sender_message_text;
        public CircleImageView receiver_profile_image;
        public ImageView message_sender_image_view, message_receiver_image_view;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sender_message_text = (TextView)itemView.findViewById(R.id.sender_message_text);
            receiver_message_text = (TextView)itemView.findViewById(R.id.receiver_message_text);
            receiver_profile_image = (CircleImageView)itemView.findViewById(R.id.receiver_profile_image);
            message_sender_image_view = (ImageView)itemView.findViewById(R.id.message_sender_image_view);
            message_receiver_image_view = (ImageView)itemView.findViewById(R.id.message_receiver_image_view);
        }
    }

    private void DeleteSentMessages(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //userMessagesList.remove(position);
                    //MessagerAdapter.this.notifyDataSetChanged();
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void DeleteReceivedMessages(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //userMessagesList.remove(position);
                   // MessagerAdapter.this.notifyDataSetChanged();
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void DeleteMessagesForEveryone(final int position, final MessageViewHolder holder) {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //MessagerAdapter.this.notifyDataSetChanged();
                    //userHashMessages.remove(userMessagesList.get(position).getMessageID());
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    //userMessagesList.remove(position);
                    // MessagerAdapter.this.notifyDataSetChanged();
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
