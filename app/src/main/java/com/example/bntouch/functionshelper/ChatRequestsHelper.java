package com.example.bntouch.functionshelper;

import com.example.bntouch.dbaccess.ChatRequestsDB;
import com.example.bntouch.dbconnections.ConnectionHandler;
import com.example.bntouch.interfaces.ChatRequestsInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedHashMap;

import androidx.annotation.NonNull;

public class ChatRequestsHelper extends ChatRequestsDB {
    ConnectionHandler connectionHandler;

    public ChatRequestsHelper(String requestType) {
        super(requestType);
        this.connectionHandler = new ConnectionHandler("Chat Requests");
    }

    public void addChatRequest(String senderid, String recieverid, String request_type_value, final ChatRequestsInterface chatRequestsInterface) {
        try {
            this.connectionHandler.getDatabaseReference()
                    .child(senderid)
                    .child(recieverid)
                    .child(this.getRequest_type())
                    .setValue(request_type_value)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                System.out.println("Chat request added successfully");
                                chatRequestsInterface.onSuccess();
                            }
                        }
                    });
        } catch (Exception ex) {
            System.out.println("Exception " + ex.toString());
        }
        finally {
        }
    }

    public void deleteChatRequest(String senderid, String recieverid, final ChatRequestsInterface chatRequestsInterface) {
        try{
            this.connectionHandler.getDatabaseReference()
                    .child(senderid)
                    .child(recieverid)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            System.out.println("Chat request deleted successfully from first side");
                            chatRequestsInterface.onSuccess();
                        }
                    });
        }
        catch (Exception ex) {
            System.out.println("Exception " + ex.toString());
        }
        finally {

        }
    }

    public void removeBothChatRequest(final String senederid, final String receiverid) {
        this.deleteChatRequest(senederid, receiverid, new ChatRequestsInterface() {
            @Override
            public void onSuccess() {
                ChatRequestsHelper.this.deleteChatRequest(receiverid, senederid, new ChatRequestsInterface() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Chat request deleted successfully from both sides");
                    }
                });
            }
        });
    }
    public ConnectionHandler getConnectionHandler(){
        return this.connectionHandler;
    }
}

