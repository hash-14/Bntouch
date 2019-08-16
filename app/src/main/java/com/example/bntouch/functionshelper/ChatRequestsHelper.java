package com.example.bntouch.functionshelper;

import com.example.bntouch.dbaccess.ChatRequestsDB;
import com.example.bntouch.dbconnections.ConnectionHandler;
import com.example.bntouch.interfaces.CallBackOnComplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public class ChatRequestsHelper extends ChatRequestsDB {
    ConnectionHandler connectionHandler;

    public ChatRequestsHelper(String requestType) {
        super(requestType);
        this.connectionHandler = new ConnectionHandler("Chat Requests");
    }

    public void addChatRequest(String senderid, String recieverid, String request_type_value, final CallBackOnComplete callBackOnComplete) {
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
                                callBackOnComplete.callBackResult();
                            }
                        }
                    });
        } catch (Exception ex) {
            System.out.println("Exception " + ex.toString());
        }
        finally {
        }
    }

    public void deleteChatRequest(String senderid, String recieverid, final CallBackOnComplete callBackOnComplete) {
        try{
            this.connectionHandler.getDatabaseReference()
                    .child(senderid)
                    .child(recieverid)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            callBackOnComplete.callBackResult();
                        }
                    });
        }
        catch (Exception ex) {
            System.out.println("Exception " + ex.toString());
        }
        finally {

        }
    }

    public ConnectionHandler getConnectionHandler(){
        return this.connectionHandler;
    }
}

