package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.map.ObjectMapper;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.ClientTrackerListener;
import org.digitalcampus.oppia.model.Client;
import org.digitalcampus.oppia.model.ClientSession;
import org.digitalcampus.oppia.model.ClientSessionDTO;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.bright.future.oppia.mobile.learning.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ClientTrackerTask extends AsyncTask<Payload, Object, Payload> {
    public static final String TAG = ClientDataSyncTask.class.getSimpleName();

    private Context ctx;
    private ClientTrackerListener clientTrackerListener;
    private SharedPreferences prefs;

    public ClientTrackerTask(Context c) {
        this.ctx = c;
    }

    @Override
    protected Payload doInBackground(Payload... params) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
        DbHelper db = new DbHelper(ctx);
        Payload payload = new Payload();
        ArrayList<ClientSession> clientSessions = new ArrayList<ClientSession>(db.getUnsentClientTrackers(prefs.getString(PrefsActivity.PREF_USER_NAME, "")));
		int clientSessionSentCount = prefs.getInt("prefSessionSentCount", 0);
        boolean sessionsReadyForSync = true;
    	for(int i= 0; i<clientSessions.size(); i++) {
    		if(!clientSessions.get(i).getIsSynced() || clientSessions.get(i).getClientId()==0 ) {
    			sessionsReadyForSync = false; // sessions needs to be corrected before sending request
    			// try update session
    	        Client clientDetail = db.getClient(clientSessions.get(i).getClientId());
    	        db.updateClientSession(clientDetail, clientSessions.get(i).getId());
       		}
    	}
    		
        	if (clientSessions.size() > 0 && sessionsReadyForSync) {
            String url = client.getFullURL(MobileLearning.CLIENT_TRACKER_DATA);
            HttpPost httpPost = new HttpPost(url);
            ObjectMapper mapper = new ObjectMapper();
            ClientSessionDTO clientSessionDTO = new ClientSessionDTO();
            try {
            	User u = db.getUser(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
                clientSessionDTO.setSessions(clientSessions);
                publishProgress(ctx.getString(R.string.client_tracker));
                String str = mapper.writeValueAsString(clientSessionDTO);
                StringEntity se = new StringEntity( str,"utf8");
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.addHeader(client.getAuthHeader(u.getUsername(), u.getApiKey())); // authorization
                httpPost.setEntity(se);
                HttpResponse response = client.execute(httpPost);
                // read response
                InputStream content = response.getEntity().getContent();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
                String responseStr = "";
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    responseStr += s;
                }
                Log.d("responseJSONFromServer", responseStr);
                switch (response.getStatusLine().getStatusCode()){
                    case 400: // unauthorised
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_login));
                        break;
                    case 201: // logged in
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("prefSessionSentCount", (clientSessionSentCount+clientSessions.size()));
                        editor.commit();
                        for (ClientSession session: clientSessions) {
                        //      db.setClientSession(session.getId());
                            db.deleteClientSession(session.getId());
                        }
                    	DatabaseManager.getInstance().closeDatabase();
                        break;
                    default:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
               
            } catch (UnsupportedEncodingException e) {
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_connection));
            } catch (ClientProtocolException e) {
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_connection));
            } catch (IOException e) {
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_connection));
            } catch (UserNotFoundException unfe) {
            	unfe.printStackTrace();
    			payload.setResult(false);
    			payload.setResultResponse(ctx.getString(R.string.error_connection));
    		} finally {
            }
       		
        }
        return payload;
    }

    @Override
    protected void onPostExecute(Payload response) {
        synchronized (this) {
            if (clientTrackerListener != null) {
                clientTrackerListener.clientTrackerComplete(response);
            }
        }
        // reset submit task back to null after completion - so next call can run properly
        MobileLearning app = (MobileLearning) ctx.getApplicationContext();
        app.omSubmitClientTrackerTask = null;
    }
    
    
	public void setClientTrackerListener(ClientTrackerListener ctl) {
		clientTrackerListener = ctl;
    }
}
