package com.al.websocketchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
    private static final String KEY_SESSION_ID = "sessionId", FLAG_MESSAGE = "message";
    public static final String IMEI_SAMSUNG_BIG = "353001071388616";
    public static final String IMEI_SAMSUNG_SMALL = "358307052658264";
    public static final String IMEI_NEXUS = "351565052658231";

    private Context mContext;
    private SharedPreferences mSharedPref;


    public Utils(Context context) {
        mContext = context;
        mSharedPref = mContext.getSharedPreferences(KEY_SHARED_PREF, Context.MODE_PRIVATE);
    }


    public void storeSessionId(String sessionId) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.commit();
    }


    public String getSessionId() {
        return mSharedPref.getString(KEY_SESSION_ID, null);
    }


    public String getSendMessageJSON(String message) {
        String json = null;

        try {
            JSONObject jObj = new JSONObject();
            jObj.put("flag", FLAG_MESSAGE);
            jObj.put("sessionId", getSessionId());
            jObj.put("message", message);

            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
