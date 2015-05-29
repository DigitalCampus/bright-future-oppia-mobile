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

import java.util.ArrayList;
import java.util.Locale;

import org.bright.future.oppia.mobile.learning.R;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.service.TrackerService;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class RoutingActivity extends AppActivity implements ScanMediaListener {

	public static final String TAG = RoutingActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private ArrayList<Course> courses = new ArrayList<Course>() ;
	private DbHelper db;
	private Context ctx;
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx=this;
		setContentView(R.layout.activity_routing);
		final String language = ctx.getResources().getConfiguration().locale.getLanguage(); 
        if(language.equals("ur")) { 
        	LinearLayout detailContainer = (LinearLayout) findViewById(R.id.routing_activity_layout); 
	        detailContainer.setTextDirection(View.TEXT_DIRECTION_RTL);
	        detailContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
		
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        if (prefs.getString("prefLanguage", "").equals("")) {
            Editor editor = prefs.edit();
            editor.putString("prefLanguage", Locale.getDefault().getLanguage());
            editor.commit();
        }
        Button mLearning = (Button) findViewById(R.id.button_learning);
		mLearning.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(RoutingActivity.this, OppiaMobileActivity.class);
				Bundle tb = new Bundle();
				tb.putInt(MobileLearning.UJJWAL_COMPONENT_TAG, MobileLearning.MOBILE_LEARNING_COMPONENT);
				i.putExtras(tb);
				startActivity(i);
			}
		});

		Button counselling = (Button) findViewById(R.id.button_counselling);
		counselling.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				startActivity(new Intent(RoutingActivity.this, ClientRegActivity.class));
                startActivity(new Intent(RoutingActivity.this, ClientListActivity.class));
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		db = new DbHelper(this);
		courses = db.getAllCourses();
		this.scanMedia();
         DatabaseManager.getInstance().closeDatabase();
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		UIUtils.showUserData(menu,this);
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
		Log.d(TAG,"selected:" + item.getItemId());
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
				Editor editor = prefs.edit();
				editor.putString("prefUsername", "");
				editor.putString("prefApiKey", "");
				editor.putInt("prefBadges", 0);
				editor.putInt("prefPoints", 0);
                editor.putLong("lastClientSync", 0L);
				editor.commit();

				// restart the app
				RoutingActivity.this.startActivity(new Intent(RoutingActivity.this, StartUpActivity.class));
				RoutingActivity.this.finish();

			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return; // do nothing
			}
		});
		builder.show();
	}
	
	private void scanMedia() {
		long now = System.currentTimeMillis()/1000;
		/*if (prefs.getLong("prefLastMediaScan", 0)+3600 > now) {
			Log.d(TAG,"no scan needed");
			LinearLayout ll = (LinearLayout) this.findViewById(R.id.home_messages);
			ll.setVisibility(View.GONE);
			return;
		}*/
		Log.d(TAG,"starting scan");
		ScanMediaTask task = new ScanMediaTask(this);
		Payload p = new Payload(this.courses);
		task.setScanMediaListener(this);
		task.execute(p);
	}
	
	public void scanStart() {
		TextView tv = (TextView) this.findViewById(R.id.home_message);
		tv.setText(this.getString(R.string.info_scan_media_start));
	}

	public void scanProgressUpdate(String msg) {
		TextView tv = (TextView) this.findViewById(R.id.home_message);
		tv.setText(this.getString(R.string.info_scan_media_checking, msg));
	}

	public void scanComplete(Payload response) {
		Editor e = prefs.edit();
		LinearLayout ll = (LinearLayout) this.findViewById(R.id.home_messages);
		TextView tv = (TextView) this.findViewById(R.id.home_message);
		Button btn = (Button) this.findViewById(R.id.message_action_button);
		
		if (response.getResponseData().size() > 0) {
			ll.setVisibility(View.VISIBLE);
			tv.setText(this.getString(R.string.info_scan_media_missing));
			btn.setText(this.getString(R.string.scan_media_download_button));
			btn.setTag(response.getResponseData());
			btn.setOnClickListener(new OnClickListener() {

				public void onClick(View view) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> m = (ArrayList<Object>) view.getTag();
					Intent i = new Intent(RoutingActivity.this, DownloadMediaActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(DownloadMediaActivity.TAG, m);
					i.putExtras(tb);
					startActivity(i);
				}
			});
			e.putLong("prefLastMediaScan", 0);
			e.commit();
		} else {
			ll.setVisibility(View.GONE);
			tv.setText("");
			btn.setText("");
			btn.setOnClickListener(null);
			btn.setTag(null);
			long now = System.currentTimeMillis()/1000;
			e.putLong("prefLastMediaScan", now);
			e.commit();
		}
	}

    @Override
    public void onResume() {
        super.onResume();
//        completeClientSession(prefs,db);
        if (prefs.getInt("prefClientSessionActive", 0) == 1) {
//            if counselling is on(1) and we come back to the routing screen , save session
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("prefClientSessionActive", 0);
//            db = new DbHelper(ctx);
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
    }
}
