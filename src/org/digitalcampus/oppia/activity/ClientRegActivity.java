/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.activity;

import java.util.Calendar;
import java.util.Map;

import org.bright.future.oppia.mobile.learning.R;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Client;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.TrackerService;
import org.digitalcampus.oppia.utils.UIUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class ClientRegActivity extends AppActivity {
	
	public static final String TAG = ClientRegActivity.class.getSimpleName();
	private SharedPreferences prefs;
    private Spinner sexSpinner, marriedSpinner, paritySpinner, plsSpinner,usingMethodSpinner,methodNameSpinner, adaptedMethodNameSpinner;
    private Button counsellingButton;
    private EditText nameClientEditText, phoneNumberClientEditText, ageClientEditText, husbandNameClientEditText, youngestChildAgeYearClientEditText, youngestChildAgeMonthClientEditText;
    private Context context;
    public long clientId;
    public boolean husbandNameRequired, childAgeRequired, methodRequired, genderSpecified, paritySpecified, maritalStatusSpecified;
    private Calendar now = Calendar.getInstance();
    private Boolean isEditClient; 
    
    String clientName, clientPhoneNumber, clientAge, clientGender, clientMarried, clientParity, clientLifeStage, clientHusbandName, clientChildAgeYear, clientChildAgeMonth;
    String usingMethod, methodName;
    ArrayAdapter<CharSequence> cwfadapter, cwfadapter2, cwfadapter3, cwfadapter4, cwfadapter5, cwfadapter6, cwfadapter7;
    DbHelper db;
    String adaptedMethodName;
    private LinearLayout methodNameLayout, adaptedMethodLayout;
	private String toAppendClientName = String.valueOf(now.get(Calendar.YEAR))+String.valueOf(now.get(Calendar.MONTH))+
			String.valueOf(now.get(Calendar.DAY_OF_MONTH))+String.valueOf(now.get(Calendar.HOUR_OF_DAY))+
			String.valueOf(now.get(Calendar.MINUTE))+String.valueOf(now.get(Calendar.SECOND));
	//private Client client;
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clientreg);
        context = this;
        genderSpecified = paritySpecified = maritalStatusSpecified = false;

        sexSpinner = (Spinner) findViewById(R.id.clientreg_form_sex_spinner);
        marriedSpinner = (Spinner) findViewById(R.id.clientreg_form_married_spinner);
        paritySpinner = (Spinner) findViewById(R.id.clientreg_form_parity_spinner);
        plsSpinner = (Spinner) findViewById(R.id.clientreg_form_lifestage_spinner);
        usingMethodSpinner = (Spinner) findViewById(R.id.clientreg_form_using_method_spinner);
        methodNameSpinner = (Spinner) findViewById(R.id.clientreg_form_method_name_spinner);
        adaptedMethodNameSpinner = (Spinner) findViewById(R.id.adapted_method_spinner);
        
        counsellingButton = (Button) findViewById(R.id.submit_btn);
        nameClientEditText = (EditText) findViewById(R.id.clientreg_form_name_field);
        phoneNumberClientEditText = (EditText) findViewById(R.id.clientreg_form_mobile_field);
        ageClientEditText = (EditText) findViewById(R.id.clientreg_form_age_field);

        husbandNameRequired = childAgeRequired = methodRequired = false;
        husbandNameClientEditText = (EditText) findViewById(R.id.clientreg_form_husband_name_field);
        youngestChildAgeYearClientEditText = (EditText) findViewById(R.id.clientreg_form_age_youngest_child_field_years);
        youngestChildAgeMonthClientEditText = (EditText) findViewById(R.id.clientreg_form_age_youngest_child_field_months);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

		//disable client name & phone number & husband name as per client request
		nameClientEditText.setText(prefs.getString("prefUsername", "")+"Client"+toAppendClientName);
		phoneNumberClientEditText.setText("99999999999");
		husbandNameClientEditText.setText("");
		nameClientEditText.setEnabled(false);
		phoneNumberClientEditText.setEnabled(false);
		husbandNameClientEditText.setEnabled(false);
		
        cwfadapter = ArrayAdapter.createFromResource(this,
		        R.array.sex, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		cwfadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		sexSpinner.setAdapter(cwfadapter);

		cwfadapter2 = ArrayAdapter.createFromResource(this,
		        R.array.yesno, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		cwfadapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		marriedSpinner.setAdapter(cwfadapter2);

		cwfadapter3 = ArrayAdapter.createFromResource(this,
		        R.array.parity, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		cwfadapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		paritySpinner.setAdapter(cwfadapter3);

		cwfadapter4 = ArrayAdapter.createFromResource(this,
		        R.array.lifestage, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		cwfadapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		plsSpinner.setAdapter(cwfadapter4);

        cwfadapter5 = ArrayAdapter.createFromResource(this,
                R.array.method, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        cwfadapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        usingMethodSpinner.setAdapter(cwfadapter5);

        cwfadapter6 = ArrayAdapter.createFromResource(this,
                R.array.methodName, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        cwfadapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        methodNameSpinner.setAdapter(cwfadapter6);
       
        //Adapted method name spinner
        cwfadapter7 = ArrayAdapter.createFromResource(this,
                R.array.methodName, android.R.layout.simple_spinner_item);
        cwfadapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adaptedMethodNameSpinner.setAdapter(cwfadapter7);

        db = new DbHelper(context);
        
        // name,phone, sex & husbandName auto generating as per client request, can changeable later.
        sexSpinner.setSelection(1);
        sexSpinner.setEnabled(false);
        
        usingMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // position 0 is null
            // position 1 is yes
            // position 2 is no
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (position == 1) {
                    methodNameSpinner.setEnabled(true);
                    methodNameSpinner.setClickable(true);
                } else {
                    methodNameSpinner.setEnabled(false);
                    methodNameSpinner.setClickable(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        marriedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // position 0 is null
            // position 1 is yes
            // position 2 is no
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (position == 1) {
                    maritalStatusSpecified = true;
                    if (genderSpecified) {
                        maritalStatusSpecified = true;
                        husbandNameClientEditText.setFocusableInTouchMode(true);
                    }
                } else {
                    maritalStatusSpecified = false;
                    husbandNameClientEditText.setFocusable(false);
                    husbandNameClientEditText.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        paritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // position 0 is null
            // position 1 is yes
            // position 2 is no
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (position != 0) {
                    paritySpecified = true;
                    if (genderSpecified) {
                        youngestChildAgeYearClientEditText.setFocusableInTouchMode(true);
                        youngestChildAgeMonthClientEditText.setFocusableInTouchMode(true);
                    }
                } else {
                    paritySpecified = false;
                    youngestChildAgeYearClientEditText.setFocusable(false);
                    youngestChildAgeMonthClientEditText.setFocusable(false);
                    youngestChildAgeYearClientEditText.setText("");
                    youngestChildAgeMonthClientEditText.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        sexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // position 0 is null
            // position 1 is yes
            // position 2 is no
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (position == 1) {
                    genderSpecified = true;
                    if (maritalStatusSpecified) {
                        husbandNameClientEditText.setFocusableInTouchMode(true);
                        husbandNameRequired = true;
                    }
                    if (paritySpecified) {
                        youngestChildAgeYearClientEditText.setFocusableInTouchMode(true);
                        youngestChildAgeMonthClientEditText.setFocusableInTouchMode(true);
                        childAgeRequired = true;
                    }
                } else {
                    genderSpecified = false;
                    husbandNameClientEditText.setFocusable(false);
                    youngestChildAgeYearClientEditText.setFocusable(false);
                    youngestChildAgeMonthClientEditText.setFocusable(false);
                    husbandNameClientEditText.setText("");
                    youngestChildAgeYearClientEditText.setText("");
                    youngestChildAgeMonthClientEditText.setText("");
                    husbandNameRequired = false;
                    childAgeRequired = false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        counsellingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Course c = (Course) db.getCourse(db.getCourseID("us-counsel"), db.getUserId(prefs.getString("prefUsername", "")));
                clientName = (String) nameClientEditText.getText().toString().trim();
                clientPhoneNumber = (String) phoneNumberClientEditText.getText().toString();
                clientAge = (String) ageClientEditText.getText().toString();
                clientGender = (String) sexSpinner.getSelectedItem().toString();
                clientMarried = (String) marriedSpinner.getSelectedItem().toString();
                clientParity = (String) paritySpinner.getSelectedItem().toString();
                clientLifeStage = (String) plsSpinner.getSelectedItem().toString();
                usingMethod = (String) usingMethodSpinner.getSelectedItem().toString();
                methodName = (String) methodNameSpinner.getSelectedItem().toString();
                clientHusbandName = (String) husbandNameClientEditText.getText().toString().trim();
                clientChildAgeYear = (String) youngestChildAgeYearClientEditText.getText().toString().trim();
                clientChildAgeMonth = (String) youngestChildAgeMonthClientEditText.getText().toString().trim();
                adaptedMethodName = (String) adaptedMethodNameSpinner.getSelectedItem().toString(); 
                
                if (sexSpinner.getSelectedItemPosition() == 1) {
                    if (marriedSpinner.getSelectedItemPosition() == 1) {
                        husbandNameRequired = true;
                    } else {
                        husbandNameRequired = false;
                    }
                    if (paritySpinner.getSelectedItemPosition() != 0) {
                        childAgeRequired = true;
                    } else {
                        childAgeRequired = false;
                    }
                } else {
                    husbandNameRequired = false;
                    childAgeRequired = false;
                }
                if (usingMethodSpinner.getSelectedItemPosition() == 1) {
                    methodRequired = true;
                } else
                    methodRequired = false;

                if (verificationClientData()) {
                    Client client = new Client();
                    //client.setClientName(clientName);
                    //client.setClientMobileNumber(Long.parseLong(clientPhoneNumber));
                    // name,phone, sex & husbandName auto generating as per client request, can changeable later.
                    if (isEditClient != null && isEditClient) {
                    	client.setClientName(clientName);
                    }
                    else {
                    	client.setClientName(prefs.getString("prefUsername", "")+"Client"+toAppendClientName);
                    }
                   	client.setClientMobileNumber(Long.parseLong("99999999999"));
                    client.setClientAge(Integer.parseInt(clientAge));
                    client.setClientGender(clientGender);
                    client.setClientMaritalStatus(clientMarried);
                    client.setClientParity(clientParity);
                    client.setClientLifeStage(clientLifeStage);
                    client.setHealthWorker(prefs.getString("prefUsername", "")); //USER


                    if (childAgeRequired) {
                        client.setAgeYoungestChild(Integer.parseInt(clientChildAgeMonth) + Integer.parseInt(clientChildAgeYear)*12);
                    }
                    if (husbandNameRequired) {
                    	// name,phone, sex & husbandName auto generating as per client request, can changeable later.
                        //client.setHusbandName(clientHusbandName);
                    	if (isEditClient != null && isEditClient) {
                    		client.setHusbandName(clientHusbandName);
                    	}
                    	else {
                    		client.setHusbandName("Clinet"+toAppendClientName+"Husband");
                    	}
                    }
                    if (methodRequired) {
                        client.setMethodName(methodName);
                    }

                    client.setAdaptedMethodName(adaptedMethodName);
			
                    
                    SharedPreferences.Editor editor = prefs.edit();
                    
                    if (isEditClient != null && isEditClient) {
                        client.setClientId(clientId);
                        client.setClientServerId(prefs.getLong("prefClientServerID", 0L));
                        db.addOrUpdateClient(client);
                        editor.putLong("prefClientServerID", client.getClientServerId());
                    } else {
                        client.setClientId(db.addClient(client));
                    }

                    DatabaseManager.getInstance().closeDatabase();
                    editor.putLong("prefClientLocalID", client.getClientId());
                    editor.commit();

                    Intent i = new Intent(ClientRegActivity.this, ClientInfoActivity.class);
                    Bundle tb = new Bundle();
                    tb.putSerializable(Course.TAG, c);
                    tb.putInt(MobileLearning.UJJWAL_COMPONENT_TAG, MobileLearning.CLIENT_COUNSELLING_COMPONENT);
                    if (isEditClient != null && isEditClient) {
                    	tb.putBoolean("isFromClientReg", true);
                    }
                    i.putExtras(tb);
                    startActivity(i);
                    ClientRegActivity.this.finish();
                } else
                    return;
            }
        });
	}

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Bundle bundle=intent.getExtras();
        clientId = bundle.getLong("localClientID");
        //long clientId = prefs.getLong("prefClientLocalID", 0L);
        isEditClient = bundle.getBoolean("editClient");
        
        if (clientId > 0) {
        	Client client = db.getClient(clientId);
            if(isEditClient!=null && isEditClient) {
            	long clientServerId=prefs.getLong("prefClientServerID", 0L);
            	if(clientServerId > 0)
            		client = db.getServerClient(clientServerId);
            }
            nameClientEditText.setText(client.getClientName());
            phoneNumberClientEditText.setText(Long.toString(client.getClientMobileNumber()));
            ageClientEditText.setText(Integer.toString(client.getClientAge()));
            int spinnerPosition;
            spinnerPosition = cwfadapter.getPosition(client.getClientGender());
            sexSpinner.setSelection(spinnerPosition);
            spinnerPosition = cwfadapter2.getPosition(client.getClientMaritalStatus());
            marriedSpinner.setSelection(spinnerPosition);
            spinnerPosition = cwfadapter3.getPosition(client.getClientParity());
            paritySpinner.setSelection(spinnerPosition);
            spinnerPosition = cwfadapter4.getPosition(client.getClientLifeStage());
            plsSpinner.setSelection(spinnerPosition);
            methodNameLayout = (LinearLayout)findViewById(R.id.if_using_method_layout);
            adaptedMethodLayout= (LinearLayout)findViewById(R.id.adapted_method_layout);
            
            if(isEditClient!=null && isEditClient) {
            	methodNameLayout.setVisibility(View.GONE);
            	adaptedMethodLayout.setVisibility(View.VISIBLE);
            	spinnerPosition = cwfadapter7.getPosition(client.getAdaptedMethodName());
                adaptedMethodNameSpinner.setSelection(spinnerPosition);
            	adaptedMethodNameSpinner.setEnabled(true);
            	adaptedMethodNameSpinner.setClickable(true);
            }
            else {
            	methodNameLayout.setVisibility(View.VISIBLE);
            	adaptedMethodLayout.setVisibility(View.GONE);
	            if (client.getMethodName() != null && client.getMethodName().length() != 0) {
	                usingMethodSpinner.setSelection(1);
	                spinnerPosition = cwfadapter6.getPosition(client.getMethodName());
	                methodNameSpinner.setSelection(spinnerPosition);
	                methodNameSpinner.setEnabled(true);
	                methodNameSpinner.setClickable(true);
	            } else {
	                usingMethodSpinner.setSelection(0);
	                methodNameSpinner.setSelection(0);
	                methodNameSpinner.setEnabled(false);
	                methodNameSpinner.setClickable(false);
	            }
            }
            husbandNameClientEditText.setText(client.getHusbandName());

            if (client.getAgeYoungestChild() / 12 != 0) {
                youngestChildAgeYearClientEditText.setText(Long.toString(client.getAgeYoungestChild() / 12));
            } else {
                youngestChildAgeYearClientEditText.setText("");
            }
            if (client.getAgeYoungestChild() % 12 != 0) {
                youngestChildAgeMonthClientEditText.setText(Long.toString(client.getAgeYoungestChild() % 12));
            } else {
                youngestChildAgeMonthClientEditText.setText("");
            }
        }
    }

    public boolean verificationClientData() {
    	/* As per clientRequirement
        if (clientName.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_name);
            return false;
        }
        if (clientPhoneNumber.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_phone_number);
            return false;
        }*/
        if (clientPhoneNumber.length() != 11) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_format_phone_number);
            return false;
        }
    /*    if (clientGender.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_gender);
            return false;
        }*/
        if (clientMarried.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_marital_status);
            return false;
        }
        if (clientAge.length() == 0 || Integer.parseInt(clientAge) > 100) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_age);
            return false;
        }
        if (clientParity.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_parity);
            return false;
        }
        if (clientLifeStage.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_lifestage);
            return false;
        }

        // name,phone, sex & husbandName auto generating as per client request, can changeable later.
        /*
        if (husbandNameRequired && clientHusbandName.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_husband_name);
            return false;
        }*/
        if (childAgeRequired) {
            if (clientChildAgeYear.length() == 0 && clientChildAgeMonth.length() == 0) {
                UIUtils.showAlert(context, R.string.error, R.string.error_register_no_child_age);
                return false;
            }
            if (clientChildAgeYear.length() == 0) {
                clientChildAgeYear = "0";
            }
            if (clientChildAgeMonth.length() == 0) {
                clientChildAgeMonth = "0";
            }
            if (Integer.parseInt(clientChildAgeMonth) > 11) {
                UIUtils.showAlert(context, R.string.error, R.string.error_register_no_child_age);
                return false;
            }
        }
        if (methodRequired && methodName.length() == 0) {
            UIUtils.showAlert(context, R.string.error, R.string.error_register_no_method);
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getInt("prefClientSessionActive", 0) == 1) {
//            if counselling is on(1) and we come back to the routing screen , save session
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("prefClientSessionActive", 0);
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