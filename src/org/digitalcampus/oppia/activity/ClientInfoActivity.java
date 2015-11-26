package org.digitalcampus.oppia.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.ClientDataSyncListener;
import org.digitalcampus.oppia.model.Client;
import org.digitalcampus.oppia.model.ClientSession;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.TrackerService;
import org.digitalcampus.oppia.task.ClientDataSyncTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.bright.future.oppia.mobile.learning.R;

import java.util.ArrayList;
import java.util.Map;

public class ClientInfoActivity extends AppActivity implements ClientDataSyncListener {
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
    private TextView clientHusbandNameTextView, clientMethodNameTextView, adaptedMethodNameTextView;
    private RelativeLayout clientChildAgeRelativeLayout, husbandNameRelativeLayout, methodNameRelativeLayout, AdaptedethodNameRelativeLayout;
    private Button makeVisitButton, editClientInfoButton, makeCallButton;//button_client_edit
    private Button closeCase, deleteClient;
    private ProgressDialog dialog;
    private boolean isButtonDeleteClient;
    private boolean isBackPressed;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_client_detail);
        final String language = ctx.getResources().getConfiguration().locale.getLanguage(); 
        if(language.equals("ur")) { 
	        RelativeLayout detailContainer = (RelativeLayout) findViewById(R.id.client_row); 
	        detailContainer.setTextDirection(View.TEXT_DIRECTION_RTL);
	        detailContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
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
        adaptedMethodNameTextView = (TextView) findViewById(R.id.adapted_method_name_value);

        makeVisitButton = (Button) findViewById(R.id.client_detail_visit_button);
        editClientInfoButton = (Button) findViewById(R.id.client_edit_info_button);
        makeCallButton = (Button) findViewById(R.id.client_call_button);

        clientChildAgeRelativeLayout = (RelativeLayout) findViewById(R.id.child_age_layout);
        husbandNameRelativeLayout = (RelativeLayout) findViewById(R.id.husband_name_layout);
        methodNameRelativeLayout = (RelativeLayout) findViewById(R.id.method_name_layout);
        AdaptedethodNameRelativeLayout = (RelativeLayout) findViewById(R.id.adapted_method_name_layout);
        
        closeCase = (Button) findViewById(R.id.close_client_case);
        deleteClient = (Button) findViewById(R.id.delete_client_record);
       
        closeCase.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//check network
				if(isNetworkAvailable(ctx)){
				
					new AlertDialog.Builder(ctx)
				    .setTitle("Close Client Case")
				    .setMessage("Are you sure you want to close this client?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				        	//check for client sync status.
				            // synced, continue with close case
				        	if(client.getClientServerId() > 0) {
					        	isButtonDeleteClient = false;
					        	closeCase.setEnabled(false);
					        	db = new DbHelper(ctx);
								client.setClientCloseCase(1);
								db.updateClientAfterSync(client);
								DatabaseManager.getInstance().closeDatabase();
				                MobileLearning app = (MobileLearning) ctx.getApplicationContext();
				                if (app.omSubmitClientSyncTask == null) {
				                    Log.d(TAG,"Syncing and updating client task");
				                    app.omSubmitClientSyncTask = new ClientDataSyncTask(ctx);
				                    app.omSubmitClientSyncTask.setClientDataSyncListener((ClientDataSyncListener) ctx);
				                    app.omSubmitClientSyncTask.execute();
				                }
				                else {
				                	// previous data sync is not completed. try after some time.
				                	closeCase.setEnabled(true);
				                	UIUtils.showAlert(ctx, "Can't Close", "Please try again");
				                }
					        }
				        	else {
				        		//client is not synced, can't close. Please try after some time. 
				        		UIUtils.showAlert(ctx, "Can't close", "Please try after some time");
				        	}
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            // do nothing
				        }
				     })
				    .setIcon(android.R.drawable.ic_dialog_alert)
				    .show();
				}
				else {
					UIUtils.showAlert(ctx, R.string.error, R.string.error_connection);
				}
			}
		});
        
        deleteClient.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//check network
				if(isNetworkAvailable(ctx)){
					deleteClient();
				}
				else {
					if(client.getClientServerId() <= 0) {
						// not synced, delete it from local db.
						deleteClient();
					}
					else {
						UIUtils.showAlert(ctx, R.string.error, R.string.error_connection);
					}
				}
			}
		});
        
        makeVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Course c = (Course) db.getCourse(db.getCourseID("mcounseling"), db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, "")));
                Intent i = new Intent(ClientInfoActivity.this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, c);
                tb.putInt(MobileLearning.UJJWAL_COMPONENT_TAG, MobileLearning.CLIENT_COUNSELLING_COMPONENT);
                i.putExtras(tb);

                db = new DbHelper(ctx);
                ClientSession clientSession = new ClientSession();

                clientSession.setHealthWorker(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
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
				DatabaseManager.getInstance().closeDatabase();
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
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("prefClientServerID", client.getClientServerId());
                editor.commit();
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

    private void deleteClient() {
    	new AlertDialog.Builder(ctx)
	    .setTitle("Delete Client Record")
	    .setMessage("Are you sure you want to delete this client?")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	//check for client sync status.
	            // synced, delete request to server. not synced delete it from local.
	        	db = new DbHelper(ctx);
				if(client.getClientServerId() > 0) {
		        	isButtonDeleteClient = true;
		        	deleteClient.setEnabled(false);
		        	
		        	client.setClientDeleteRecord(1);
					db.updateClientAfterSync(client);
					MobileLearning app = (MobileLearning) ctx.getApplicationContext();
					
	                if (app.omSubmitClientSyncTask == null) {
	                    Log.d(TAG,"Syncing and updating client task");
	                    app.omSubmitClientSyncTask = new ClientDataSyncTask(ctx);
	                    app.omSubmitClientSyncTask.setClientDataSyncListener((ClientDataSyncListener) ctx);
	                    app.omSubmitClientSyncTask.execute();
	                }
	                else {
	                	// previous data sync is not completed. try after some time.
	                	deleteClient.setEnabled(true);
	                	UIUtils.showAlert(ctx, "Can't delete", "Please try again");
	                }
	        	}
	        	else {
	        		// not synced, delete it from local db.
	        		clientDataSyncProgress();
	        		client.setClientDeleteRecord(1);
	        		db.updateClientAfterSync(client);
	        		db.deleteUnregisteredClients(client.getClientId());
	        		clientDataSyncComplete(null);
	        	}
				DatabaseManager.getInstance().closeDatabase();
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .show();
    }
    @Override
    public void onStart() {
        super.onStart();
        Boolean isNewClient=false;
        try {
	        Intent intent = getIntent();
	        Bundle bundle=intent.getExtras();
	        isNewClient = bundle.getBoolean("isNewClient");
        }
        catch(Exception e){
        	
        }
        db = new DbHelper(this);
        long clientId = prefs.getLong("prefClientLocalID", 0L);
        client = db.getClient(clientId);
    	long clientServerId=prefs.getLong("prefClientServerID", 0L);
    	if( clientServerId > 0 )
    		client = db.getServerClient(clientServerId);
        if(isNewClient !=null && isNewClient) {
        	client=db.getLastCreatedClient();
        }
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
       
        if (client.getAdaptedMethodName() != null && client.getAdaptedMethodName().length() != 0) {
        	AdaptedethodNameRelativeLayout.setVisibility(View.VISIBLE);
        	adaptedMethodNameTextView.setText(client.getAdaptedMethodName());
        } else {
        	AdaptedethodNameRelativeLayout.setVisibility(View.GONE);
        	adaptedMethodNameTextView.setText("");
        	
        	 if (client.getMethodName() != null && client.getMethodName().length() != 0) {
                 methodNameRelativeLayout.setVisibility(View.VISIBLE);
                 clientMethodNameTextView.setText(client.getMethodName());
             } else {
                 methodNameRelativeLayout.setVisibility(View.GONE);
                 clientMethodNameTextView.setText("");
             }
        }
		DatabaseManager.getInstance().closeDatabase();
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
        //ArrayList<Client> clients3 = db.getAllClients(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
        //ArrayList<ClientSession> clientSessions2 = db.getAllClientSessions(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));

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

    @Override
    public void onBackPressed() {
        isBackPressed = true;
    	clientDataSyncProgress();
    	clientDataSyncComplete(null);
    }
    
	@Override
	public void clientDataSyncComplete(Payload response) {
		if(dialog != null){
			dialog.dismiss();
		}
		
	    Intent i = new Intent(ClientInfoActivity.this, ClientListActivity.class);
	    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle tb = new Bundle();
        //tb.putBoolean("editClient", true);
        tb.putLong("localClientID", client.getClientId());
        i.putExtras(tb);
        startActivity(i);
        ClientInfoActivity.this.finish();
	}

	@Override
	public void clientDataSyncProgress() {
		String header;
		if(isButtonDeleteClient) {
			header="Deleting Client";
		}
		else {
			header="Closing Case";
		}
		if(isBackPressed) header = "Loading";
		
		dialog = ProgressDialog.show(ctx, header,
			    "Please Wait...", true);
	}
	public boolean isNetworkAvailable(final Context context) {
	    final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	    return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}
}
