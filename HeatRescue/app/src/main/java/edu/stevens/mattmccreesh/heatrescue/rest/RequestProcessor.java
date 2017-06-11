package edu.stevens.cs522.chat.rest;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.stevens.cs522.chat.entities.ChatMessage;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.RequestManager;
import edu.stevens.cs522.chat.managers.TypedCursor;
import edu.stevens.cs522.chat.util.StringUtils;

/**
 * Created by dduggan.
 */

public class RequestProcessor {

    private Context context;

    private RestMethod restMethod;

    private RequestManager requestManager;

    public RequestProcessor(Context context) {
        this.context = context;
        this.restMethod =  new RestMethod(context);
        this.requestManager = new RequestManager(context);
    }

    public Response process(Request request) {
        return request.process(this);
    }

    public Response perform(RegisterRequest request) {
        return restMethod.perform(request);
    }

    public Response perform(PostMessageRequest request) {
        // We will just insert the message into the database, and rely on background sync to upload
        // return restMethod.perform(request)
        requestManager.persist(request.message);
        return request.getDummyResponse();
    }

    public Response perform(SynchronizeRequest request) {
        RestMethod.StreamingResponse response = null;
        final TypedCursor<ChatMessage> messages = requestManager.getUnsentMessages();
        //final int numMessagesReplaced = messages.getCount();
        int numMessagesReplaced = 0;
        if(messages.getCount()>0) {//added this for case no messages sent
            messages.moveToFirst();
            do {
                if (messages.getEntity().seqNum == 0)
                    numMessagesReplaced++;
            } while (messages.moveToNext());
        }

        System.out.println("Num Messages replaced: " + numMessagesReplaced);
        try {
            RestMethod.StreamingOutput out = new RestMethod.StreamingOutput() {
                @Override
                public void write(final OutputStream os) throws IOException {
                    try {
                        JsonWriter wr = new JsonWriter(new OutputStreamWriter(new BufferedOutputStream(os)));
                        wr.beginArray();
                        /*
                         * TODO stream unread messages to the server: Not sure about what I have below
                         * {
                         *   chatroom : ...,
                         *   timestamp : ...,
                         *   latitude : ...,
                         *   longitude : ....,
                         *   text : ...
                         * }
                         */
                        if(messages.getCount()>0) {//added this in case no errors sent
                            messages.moveToFirst();
                            do {
                                ChatMessage message = messages.getEntity();
                                if (message.seqNum == 0) {
                                    wr.beginObject();
                                    wr.name("chatroom");//is this still correct?
                                    wr.value(message.chatRoom);
                                    wr.name("timestamp");
                                    wr.value(message.timestamp.getTime());
                                    wr.name("latitude");
                                    wr.value(message.latitude);
                                    wr.name("longitude");
                                    wr.value(message.longitude);
                                    wr.name("text");
                                    wr.value(message.messageText);
                                    wr.endObject();
                                    System.out.println("wrote a chatmessage");
                                }
                            } while (messages.moveToNext());
                        }

                        wr.endArray();
                        wr.flush();
                    } finally {
                        messages.close();
                    }
                }
            };
            response = restMethod.perform(request, out);


            JsonReader rd = new JsonReader(new InputStreamReader(new BufferedInputStream(response.getInputStream()), StringUtils.CHARSET));
            // TODO parse data from server (messages and peers) and update database
            // See RequestManager for operations to help with this.
            //trying this. Be careful for case where json is empty (or messages part is empty)
            rd.beginObject();
            System.out.println("Starting");
            System.out.println(rd.nextName());//clients
            rd.beginArray();
            while(rd.peek().equals(JsonToken.BEGIN_OBJECT)) {
                Peer p = new Peer();
                rd.beginObject();
                System.out.println(rd.nextName());//username
                //System.out.println(rd.nextString());
                p.name = rd.nextString();
                System.out.println(rd.nextName());//timestamp
                //System.out.println(rd.nextLong());
                p.timestamp = new Date(rd.nextLong());
                System.out.println(rd.nextName());//latitude
               // System.out.println(rd.nextDouble());
                p.latitude=rd.nextDouble();
                System.out.println(rd.nextName());//longitude
                //System.out.println(rd.nextDouble());
                p.longitude = rd.nextDouble();
                requestManager.persist(p);
                rd.endObject();
            }
            rd.endArray();

            ArrayList<ChatMessage> lmessages =new ArrayList<ChatMessage>();//not fully sure if ArrayList is okay here but trying it
            System.out.println(rd.nextName());//messages
            rd.beginArray();
            while(rd.peek().equals(JsonToken.BEGIN_OBJECT)) {
                ChatMessage m = new ChatMessage();
                rd.beginObject();
                System.out.println(rd.nextName());//chatroom
                //System.out.println(rd.nextString());
                m.chatRoom = rd.nextString();
                System.out.println(rd.nextName());//timestamp
                //System.out.println(rd.nextLong());
                m.timestamp = new Date(rd.nextLong());
                System.out.println(rd.nextName());//latitude
                //System.out.println(rd.nextDouble());
                m.latitude = rd.nextDouble();
                System.out.println(rd.nextName());//longitude
                //System.out.println(rd.nextDouble());
                m.longitude = rd.nextDouble();
                System.out.println(rd.nextName());//seqnum
                //System.out.println(rd.nextLong());
                m.seqNum = rd.nextLong();
                System.out.println(rd.nextName());//sender
                //System.out.println(rd.nextString());
                m.sender = rd.nextString();
                System.out.println(rd.nextName());//text
                //System.out.println(rd.nextString());
                m.messageText = rd.nextString();
                if(m.seqNum>requestManager.getLastSequenceNumber())//adding this to prevent duplication
                    lmessages.add(m);
                rd.endObject();
            }

            requestManager.syncMessages(numMessagesReplaced, lmessages);
            System.out.println("ended");


            return response.getResponse();

        } catch (IOException e) {
            System.out.println("Error in RequestProcessor perform method: " + e.toString());
            return new ErrorResponse(request.id, e);

        } finally {
            if (response != null) {
                response.disconnect();
            }
        }
    }

}
