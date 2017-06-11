package edu.stevens.mattmccreesh.heatrescue.rest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;
/*
import edu.stevens.cs522.chat.entities.ChatMessage;
import edu.stevens.cs522.chat.settings.Settings;
import edu.stevens.cs522.chat.util.ResultReceiverWrapper;*/


/**
 * Created by dduggan.
 */

public class ChatHelper {

    public static final String DEFAULT_CHAT_ROOM = "_default";

    private Context context;
    //added this to match last assignment
    private String chatName;
    //private UUID clientID;

    public ChatHelper(Context context) {
        this.context = context;
        //added this to match last assignment
        //this.chatName = Settings.getChatName(context);
        //this.clientID = Settings.getClientId(context); We don't seem to be using this
    }

    // done I think provide a result receiver that will display a toast message upon completion
    public void register (String chatName) {
        if (chatName != null && !chatName.isEmpty()) {
            RegisterRequest request = new RegisterRequest(chatName);//, clientID);we don't seem to use a uuid anymore for this
            this.chatName = chatName;
            //Settings.saveChatName(context, chatName);
            ResultReceiverWrapper receiver = new ResultReceiverWrapper(new Handler());//not sure about this
            receiver.setReceiver(new ResultReceiverWrapper.IReceive() {
                @Override
                public void onReceiveResult(int resultCode, Bundle data) {
                    switch (resultCode) {
                        case 0://not sure about this
                            Toast.makeText(context, "Registration done", Toast.LENGTH_LONG).show();
                            //Settings.setRegistered(context, true);//adding this
                            break;
                        default:
                            Toast.makeText(context, "Registration failed, try again", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });
            addRequest(request, receiver);
        }
    }

    // done I think provide a result receiver that will display a toast message upon completion
    public void postMessage (String chatRoom, String text) {
        if (text != null && !text.isEmpty()) {
            if (chatRoom == null || chatRoom.isEmpty()) {
                chatRoom = DEFAULT_CHAT_ROOM;
            }
            /*ChatMessage message = new ChatMessage();
            message.chatRoom = chatRoom;
            message.messageText = text;
            message.sender = Settings.getChatName(context);
            message.timestamp = new Date();
            PostMessageRequest request = new PostMessageRequest(message);
            ResultReceiverWrapper receiver = new ResultReceiverWrapper(new Handler());//not sure about this
            receiver.setReceiver(new ResultReceiverWrapper.IReceive() {
                @Override
                public void onReceiveResult(int resultCode, Bundle data) {
                    switch(resultCode) {
                        case 0:
                            Toast.makeText(context, "Message posted", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(context, "Message failed to send", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });
            addRequest(request, receiver);
        }
    }

    private void addRequest(Request request, ResultReceiver receiver) {
        context.startService(createIntent(context, request, receiver));
    }

    private void addRequest(Request request) {
        addRequest(request, null);
    }

    /**
     * Use an intent to send the request to a background service. The request is included as a Parcelable extra in
     * the intent. The key for the intent extra is in the RequestService class.
     */
    public static Intent createIntent(Context context, Request request, ResultReceiver receiver) {
        Intent requestIntent = new Intent(context, RequestService.class);
        System.out.println("adding extras");
        if(request == null)
            System.out.println("null request");
        requestIntent.putExtra(RequestService.SERVICE_REQUEST_KEY, request);
        if (receiver != null) {
            requestIntent.putExtra(RequestService.RESULT_RECEIVER_KEY, receiver);
        }
        else
            System.out.println("null receiver");
        return requestIntent;
    }

    public static Intent createIntent(Context context, Request request) {
        return createIntent(context, request, null);
    }

}
