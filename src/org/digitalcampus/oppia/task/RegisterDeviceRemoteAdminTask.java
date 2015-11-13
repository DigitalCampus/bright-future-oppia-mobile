package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RegisterDeviceRemoteAdminTask extends AsyncTask<Payload, Void, Payload> {

    public static final String TAG = RegisterDeviceRemoteAdminTask.class.getSimpleName();

    private Context ctx;
    private SharedPreferences prefs;

    public RegisterDeviceRemoteAdminTask(Context appContext, SharedPreferences sharedPrefs){
        this.ctx = appContext;
        prefs = sharedPrefs;
    }

    @Override
    protected Payload doInBackground(Payload... args) {

        Payload payload = new Payload();
        boolean success = registerDevice(ctx, prefs);
        payload.setResult(success);
        return payload;
    }

    public static boolean registerDevice(Context ctx, SharedPreferences prefs){

        Log.d(TAG, "Checking if is needed to send the token");
        String username = prefs.getString(PrefsActivity.PREF_USER_NAME, "");
        boolean tokenSent = prefs.getBoolean(PrefsActivity.GCM_TOKEN_SENT, false);
        
        //If there is no user logged in or the token has already been sent, we exit the task
        if (tokenSent || username.equals("")){
            return false;
        }

        String token = prefs.getString(PrefsActivity.GCM_TOKEN_ID, "");
        String deviceModel = android.os.Build.BRAND + " " + android.os.Build.MODEL;
        String deviceID = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d(TAG, "Registering device in remote admin list");
        HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
        //String url = MobileLearning.DEVICEADMIN_API_URL + MobileLearning.DEVICEADMIN_ADD_PATH;
        HTTPConnectionUtils fillUrl = new HTTPConnectionUtils(ctx);
        String url = fillUrl.getFullURL(MobileLearning.DEVICEADMIN_ADD_PATH);
        
        HttpPost httpPost = new HttpPost(url);
        try {
        	DbHelper db = new DbHelper(ctx);
        	User u = db.getUser(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
            // Request parameters and other properties.
            //List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            //params.add(new BasicNameValuePair("name", deviceModel));
            //params.add(new BasicNameValuePair("dev_id", deviceID));
            //params.add(new BasicNameValuePair("reg_id", token));
            //params.add(new BasicNameValuePair("username", username));
            JSONObject obj = new JSONObject();
            obj.put("name", deviceModel);
            obj.put("dev_id", deviceID);
            obj.put("reg_id", token);
            
            httpPost.setHeader(client.getAuthHeader(u.getUsername(), u.getApiKey())); // authorization
            httpPost.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(new StringEntity(obj.toString(), "UTF-8"));

            Log.d("Params:",  obj.toString());
            Log.d("User:", u.getUsername()+", "+u.getApiKey() );
            
            // make request
            HttpResponse response = client.execute(httpPost);
            DatabaseManager.getInstance().closeDatabase();
            // check status code
            switch (response.getStatusLine().getStatusCode()){
                case 400: // unauthorised
                    Log.d(TAG, "Bad request");
                    return false;
                case 200: // logged in
                    Log.d(TAG, "Successful registration!");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(PrefsActivity.GCM_TOKEN_SENT, true);
                    editor.commit();
                    break;
            }

        } catch (UnsupportedEncodingException | UserNotFoundException | JSONException | ClientProtocolException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
