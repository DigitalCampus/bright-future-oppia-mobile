package org.digitalcampus.oppia.activity;

import java.util.ArrayList;
import java.util.Map;

import org.bright.future.oppia.mobile.learning.R;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Client;
import org.digitalcampus.oppia.model.ClientSession;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.TrackerService;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClientInfoActivity extends AppActivity {
    public static final String TAG = ClientInfoActivity.class.getSimpleName();
    private DbHelper db;
    private Client client;
    private Context ctx;
    private SharedPreferences prefs;
    private AlertDialog aDialog;
    private ArrayList<Course> courses = new ArrayList<Course>() ;
    private TextView clientNameTextView;
    private TextView clientMobileTextView;
    private TextView clientGenderTextView;
    private TextView clientMaritalStatusTextView;
    private TextView clientAgeTextView;
    private TextView clientParityTextView;
    private TextView clientLifeStageTextView;
    private TextView clientChildAgeTextView;
    private TextView clientHusbandNameTextView, clientMethodNameTextView;
    private RelativeLayout clientChildAgeRelativeLayout, husbandNameRelativeLayout, methodNameRelativeLayout;
    private Button makeVisitButton, editClientInfoButton, makeCallButton;//button_client_edit

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_client_detail);
        clientNameTextView = (TextView) findViewById(R.id.client_name_value);
        clientMobileTextView = (TextView) findViewById(R.id.client_mobile_number_value);
        clientGenderTextView = (TextView) findViewById(R.id.client_sex_value);
        clientMaritalStatusTextView = (TextView) findViewById(R.id.client_is_married_value);
        clientAgeTextView = (TextView) findViewById(R.id.client_age_value);
        clientParityTextView = (TextView) findViewById(R.id.client_parity_value);
        clientLifeStageTextView = (TextView) findViewById(R.id.client_life_stage_value);
        clientChildAgeTextView = (TextView) findViewById(R.id.client_youngest_child_age_value);
        clientHusbandNameTextView = (TextView) findViewById(R.id.client_husband_value);
        clientMethodNameTextView = (TextView) findViewById(R.id.client_method_name_value);

        makeVisitButton = (Button) findViewById(R.id.client_detail_visit_button);
        editClientInfoButton = (Button) findViewById(R.id.client_edit_info_button);
        makeCallButton = (Button) findViewById(R.id.client_call_button);

        clientChildAgeRelativeLayout = (RelativeLayout) findViewById(R.id.child_age_layout);
        husbandNameRelativeLayout = (RelativeLayout) findViewById(R.id.husband_name_layout);
        methodNameRelativeLayout = (RelativeLayout) findViewById(R.id.method_name_layout);

        makeVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Course c = (Course) db.getCourse(db.getCourseID("mcounseling"), db.getUserId(prefs.getString("prefUsername", "")));
                Intent i = new Intent(ClientInfoActivity.this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, c);
                tb.putInt(MobileLearning.UJJWAL_COMPONENT_TAG, MobileLearning.CLIENT_COUNSELLING_COMPONENT);
                i.putExtras(tb);

                db = new DbHelper(ctx);
                ClientSession clientSession = new ClientSession();

                clientSession.setHealthWorker(prefs.getString("prefUsername", ""));
                clientSession.setStartDateTime(System.currentTimeMillis()/1000);
                if (client.getClientServerId() != 0) {
                    clientSession.setClientId(client.getClientServerId());
                    clientSession.setIsSynced(true);
                } else {
                    clientSession.setClientId(client.getClientId());
                    clientSession.setIsSynced(false);
                }
                clientSession.setId(db.addStartClientSession(clientSession));

                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("prefClientSessionId", clientSession.getId());
                editor.putInt("prefClientSessionActive", 1);// client counselling session started
                editor.commit();

                startActivity(i);
//                ClientInfoActivity.this.finish();
            }
        });
        editClientInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientInfoActivity.this, ClientRegActivity.class);
                Bundle tb = new Bundle();
                tb.putBoolean("editClient", true);
                tb.putLong("localClientID", client.getClientId());
                i.putExtras(tb);
                startActivity(i);
                ClientInfoActivity.this.finish();
            }
        });
        makeCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + "+91" + Long.toString(client.getClientMobileNumber())));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.e("Calling a Phone Number", "Call failed", activityException);
                }
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        db = new DbHelper(this);
        long clientId = prefs.getLong("prefClientLocalID", 0L);
        client = db.getClient(clientId);
        clientNameTextView.setText(client.getClientName());
        clientMobileTextView.setText(Long.toString(client.getClientMobileNumber()));
        clientGenderTextView.setText(client.getClientGender());
        clientMaritalStatusTextView.setText(client.getClientMaritalStatus());
        clientAgeTextView.setText(Integer.toString(client.getClientAge()));
        clientParityTextView.setText(client.getClientParity());
        clientLifeStageTextView.setText(client.getClientLifeStage());
        if (client.getHusbandName() != null && client.getHusbandName().length() != 0) {
            husbandNameRelativeLayout.setVisibility(View.VISIBLE);
            clientHusbandNameTextView.setText(client.getHusbandName());
        } else {
            husbandNameRelativeLayout.setVisibility(View.GONE);
            clientHusbandNameTextView.setText("");
        }
        if (client.getAgeYoungestChild() > 0) {
            int year, month;
            year = client.getAgeYoungestChild() / 12;
            month = client.getAgeYoungestChild() % 12;
            if (month == 0) {
                clientChildAgeTextView.setText(Integer.toString(year) + " year");
            } else if (year == 0) {
                clientChildAgeTextView.setText(Integer.toString(month) + " month");
            } else {
                clientChildAgeTextView.setText(Integer.toString(year) + "year " + Integer.toString(month) + "month");
            }
            clientChildAgeRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            clientChildAgeRelativeLayout.setVisibility(View.GONE);
            clientChildAgeTextView.setText("0");
        }
        if (client.getMethodName() != null && client.getMethodName().length() != 0) {
            methodNameRelativeLayout.setVisibility(View.VISIBLE);
            clientMethodNameTextView.setText(client.getMethodName());
        } else {
            methodNameRelativeLayout.setVisibility(View.GONE);
            clientMethodNameTextView.setText("");
        }
    }

    @Override
    public void onPause() {
        if (aDialog != null) {
            aDialog.dismiss();
            aDialog = null;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        db = new DbHelper(ctx);
        ArrayList<Client> clients3 = db.getAllClients(prefs.getString("prefUsername", ""));
        ArrayList<ClientSession> clientSessions2 = db.getAllClientSessions(prefs.getString("prefUsername", ""));

        DatabaseManager.getInstance().closeDatabase();

        if (prefs.getInt("prefClientSessionActive", 0) == 1) {
//            if counselling is on(1) and we come back to the routing screen , save session
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("prefClientSessionActive", 0);
            db = new DbHelper(ctx);
            db.addEndClientSession(prefs.getLong("prefClientSessionId",0L), System.currentTimeMillis()/1000);
            editor.putLong("prefClientSessionId", 0L);
            editor.commit();
            DatabaseManager.getInstance().closeDatabase();
        }
        // start a new tracker service
        Intent service = new Intent(this, TrackerService.class);

        Bundle tb = new Bundle();
        tb.putBoolean("backgroundData", true);
        service.putExtras(tb);
        this.startService(service);
        // remove any saved state info from shared prefs in case they interfere with subsequent page views
        SharedPreferences.Editor editor = prefs.edit();
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            if (entry.getKey().startsWith("widget_")){
                editor.remove(entry.getKey());
            }
        }
        editor.commit();
    }
}
