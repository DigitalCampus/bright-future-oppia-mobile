package org.digitalcampus.oppia.activity;

import java.util.ArrayList;

import org.bright.future.oppia.mobile.learning.R;
import org.digitalcampus.oppia.adapter.ClientListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Client;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.service.TrackerService;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ClientListActivity extends AppActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = CourseIndexActivity.class.getSimpleName();
    private ArrayList<Client> clients;
    private SharedPreferences prefs;
    private AlertDialog aDialog;
    private DbHelper db;
    private Button clientRegistrationButton;
    private ListView listView;
    private Context context;
    private TextView noClientsText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        clientRegistrationButton = (Button) findViewById(R.id.list_create_client);
        noClientsText = (TextView)findViewById(R.id.tv_no_clients);
        listView = (ListView) findViewById(R.id.list_lv_clients);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        clientRegistrationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//            startActivity(new Intent(ClientListActivity.this, ClientRegActivity.class));
            Intent i = new Intent(ClientListActivity.this, ClientRegActivity.class);
            Bundle tb = new Bundle();
            tb.putLong("localClientID", 0L);
            i.putExtras(tb);
            startActivity(i);
            ClientListActivity.this.finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
            Client client = (Client) listView.getItemAtPosition(position);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("prefClientLocalID",client.getClientId() );
            editor.commit();
            startActivity(new Intent(ClientListActivity.this, ClientInfoActivity.class));
//            ClientListActivity.this.finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        db = new DbHelper(this);
        clients = db.getAllClients(prefs.getString("prefUsername", ""));
        DatabaseManager.getInstance().closeDatabase();
        Log.i("info", Integer.toString(clients.size()) );
        if (clients.size() < 1) {
            listView.setVisibility(View.GONE);
            noClientsText.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            noClientsText.setVisibility(View.GONE);
        }
        ClientListAdapter cla = new ClientListAdapter(this, clients);
        listView.setAdapter(cla);
    }

    @Override
    public void onPause() {
        if (aDialog != null) {
            aDialog.dismiss();
            aDialog = null;
        }
        super.onPause();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("prefPoints")
                || key.equalsIgnoreCase("prefBadges")) {
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UIUtils.showUserData(menu, this);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.d(TAG, "selected:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menu_download:
                startActivity(new Intent(this, TagSelectActivity.class));
                return true;
            case R.id.menu_settings:
                Intent i = new Intent(this, PrefsActivity.class);
                Bundle tb = new Bundle();
                ArrayList<Lang> langs = new ArrayList<Lang>();
                tb.putSerializable("langs", langs);
                i.putExtras(tb);
                startActivity(i);
                return true;
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.menu_logout:
                logout();
                return true;
        }
        return true;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // wipe user prefs
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("prefUsername", "");
                editor.putString("prefApiKey", "");
                editor.putInt("prefBadges", 0);
                editor.putInt("prefPoints", 0);
                editor.putLong("lastClientSync", 0L);
                editor.commit();

                // restart the app
                ClientListActivity.this.startActivity(new Intent(ClientListActivity.this, StartUpActivity.class));
                ClientListActivity.this.finish();

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return; // do nothing
            }
        });
        builder.show();
    }
    @Override
    public void onResume() {
        super.onResume();
        // start a new tracker service
        Intent service = new Intent(this, TrackerService.class);
        Bundle tb = new Bundle();
        tb.putBoolean("backgroundData", true);
        service.putExtras(tb);
        this.startService(service);

        if (prefs.getInt("prefClientSessionActive", 0) == 1) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("prefClientSessionActive", 0);
            db = new DbHelper(context);
            db.addEndClientSession(prefs.getLong("prefClientSessionId",0L), System.currentTimeMillis()/1000);
            editor.putLong("prefClientSessionId", 0L);
            editor.commit();
            DatabaseManager.getInstance().closeDatabase();
        }
    }
}
