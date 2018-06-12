package com.al.websocketchat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SELF = "self", TAG_NEW = "new", TAG_MESSAGE = "message", TAG_EXIT = "exit";

    private Button btnSend;
    private EditText edtMsg;
    private ListView lvMessages;

    private OkHttpClient mClient;
    private WebSocket mWebSocket;
    private MessageListAdapter mAdapter;
    private List<Message> mListMessages;
    private Utils mUtils;
    private String mName;
    private String mIMEI;
    private int mMobile;
    private boolean mIsConnected;
    private boolean mViewClosing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnSend = findViewById(R.id.btn_send);
        edtMsg = findViewById(R.id.edt_msg);
        lvMessages = findViewById(R.id.lv_messages);

        mUtils = new Utils(getApplicationContext());

        if (mUtils.isNetworkAvailable())
            mIsConnected = true;
        else
            mIsConnected = false;

        runConectionThread();

        // Getting the person name from previous screen
        Intent i = getIntent();
        mName = i.getStringExtra("name");
        mMobile = i.getIntExtra("mobile", 0);

        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            return;

        mIMEI = telephony.getDeviceId();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer(mUtils.getSendMessageJSON(edtMsg.getText()
                        .toString()));

                // Clearing the input filed once message was sent
                edtMsg.setText("");
            }
        });

        mListMessages = new ArrayList<>();
        mAdapter = new MessageListAdapter(this, mListMessages);
        lvMessages.setAdapter(mAdapter);

        connectWebsocket();
    }


    private void connectWebsocket() {
        Request request = new Request.Builder().url(WsConfig.URL_WEBSOCKET + mName).build();
        mClient = new OkHttpClient();
        mWebSocket = mClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);

                Log.d(TAG, String.format("Got string message! %s", text));
                parseMessage(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);

                String message = String.format("Disconnected! Code: %d Reason: %s", code, reason);
                showToast(message);
                mUtils.storeSessionId(null);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);

                Log.e(TAG, "Error! : " + t.getMessage());
                showToast("Error! : " + t.getMessage());
            }
        });
    }


    private void runConectionThread() {
        mViewClosing = false;
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Reconectando ...");
        progress.setCancelable(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mViewClosing) {
                    if (mUtils.isNetworkAvailable() && !mIsConnected) {
                        mIsConnected = true;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                            }
                        });

                        connectWebsocket();
                    } else if (!mUtils.isNetworkAvailable()) {
                        mIsConnected = false;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.show();
                            }
                        });
                    }
                }
            }
        }).start();
    }


    /**
     * Method to send message to web socket server
     * */
    private void sendMessageToServer(String message) {
        if (mWebSocket != null) {
            mWebSocket.send(message);
        }
    }


    /**
     * Parsing the JSON message received from server The intent of message will
     * be identified by JSON node 'flag'. flag = self, message belongs to the
     * person. flag = new, a new person joined the conversation. flag = message,
     * a new message received from server. flag = exit, somebody left the
     * conversation.
     * */
    private void parseMessage(final String msg) {
        try {
            JSONObject jObj = new JSONObject(msg);
            String flag = jObj.getString("flag");

            if (flag.equalsIgnoreCase(TAG_SELF)) {
                String sessionId = jObj.getString("sessionId");
                mUtils.storeSessionId(sessionId);
                Log.e(TAG, "Your session id: " + mUtils.getSessionId());
            } else if (flag.equalsIgnoreCase(TAG_NEW)) {
                String name = jObj.getString("name");
                String message = jObj.getString("message");
                String onlineCount = jObj.getString("onlineCount");
                showToast(name + message + ". Currently " + onlineCount
                        + " people online!");
            } else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
                String fromName = mName;
                String message = jObj.getString("message");
                String sessionId = jObj.getString("sessionId");
                boolean isSelf = true;

                if (!sessionId.equals(mUtils.getSessionId())) {
                    fromName = jObj.getString("name");
                    isSelf = false;
                }

                Message m = new Message(fromName, message, isSelf);
                appendMessage(m);
            } else if (flag.equalsIgnoreCase(TAG_EXIT)) {
                String name = jObj.getString("name");
                String message = jObj.getString("message");
                showToast(name + message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mClient != null)
            mClient.dispatcher().executorService().shutdown();

        mViewClosing = true;
    }


    /**
     * Appending message to list view
     * */
    private void appendMessage(final Message message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (checkDevice(message.getFromName())) {
                    mListMessages.add(message);
                    mAdapter.notifyDataSetChanged();
                    playBeep();
                }
            }
        });
    }


    private void showToast(final String message) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_LONG).show();
            }
        });

    }


    private boolean checkDevice(String name) {
        boolean result = false;

        switch (mMobile) {
            case 0:
                if (name.equals(Utils.IMEI_SAMSUNG_BIG) || name.equals(mIMEI))
                    result = true;
                break;

            case 1:
                if (name.equals(Utils.IMEI_SAMSUNG_SMALL) || name.equals(mIMEI))
                    result = true;
                break;

            case 2:
                if (name.equals(Utils.IMEI_NEXUS) || name.equals(mIMEI))
                    result = true;
                break;
        }

        return result;
    }


    /**
     * Plays device's default notification sound
     * */
    public void playBeep() {

        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
