package com.katadigital.phone;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost.OnTabChangeListener;

import com.katadigital.phone.callsmsblocker.callBlockerService.CallBlockerService;
import com.katadigital.phone.callsmsblocker.objects.BlockedContact;

//implements ActionBar.TabListener
public class MainActivity extends FragmentActivity {
	public FragmentTabHost mTabHost;
	int currentTab = 0;

	// Variables for logsActivity
	LogsActivity logsActivity;
	String editbtn_text = "";
	boolean hasClearbtn = false;
	public static Context applicationContext;
	public static HashMap<String, BlockedContact> blackList;
	Fragment currentFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_tab_main);
		applicationContext = this;
		// ActionBar ab = getActionBar();
		// ab.setDisplayOptions(
		// ActionBar.DISPLAY_SHOW_CUSTOM);
		// ab.setDisplayHomeAsUpEnabled(true);
		// ab.setDisplayShowHomeEnabled(true);
		// ab.setDisplayShowTitleEnabled(false);
		// ab.setLogo(R.drawable.ic_launcher);
		// ab.setIcon(R.drawable.dummy);

		// LayoutInflater inflator = (LayoutInflater) this
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View v = inflator.inflate(R.layout.contacts_actionbar, null);
		// ab.setCustomView(R.layout.contacts_actionbar);

		// getActivity().getActionBar().setDisplayOptions(
		// ActionBar.DISPLAY_SHOW_CUSTOM);
		// getActivity().getActionBar().setCustomView(R.layout.contacts_actionbar);

		// ab.setCustomView(v);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		mTabHost.addTab(
				mTabHost.newTabSpec("favorites").setIndicator("",
						getResources().getDrawable(R.drawable.tab_favorites)),
				FavoritesActivity.class, null);

		mTabHost.addTab(
				mTabHost.newTabSpec("logs").setIndicator("",
						getResources().getDrawable(R.drawable.tab_logs)),
				LogsActivity.class, null);

		mTabHost.addTab(
				mTabHost.newTabSpec("contacts").setIndicator("",
						getResources().getDrawable(R.drawable.tab_contacts)),
				ContactsActivity.class, null);

		mTabHost.addTab(
				mTabHost.newTabSpec("keypad").setIndicator("",
						getResources().getDrawable(R.drawable.tab_keypad)),
				KeypadActivity.class, null);

		mTabHost.addTab(
				mTabHost.newTabSpec("voicemail").setIndicator("",
						getResources().getDrawable(R.drawable.tab_voicemail)),
				VoicemailActivity.class, null);

		mTabHost.getTabWidget().getChildAt(0).getLayoutParams().height = Integer
				.parseInt(getResources().getString(R.string.tabs_size));
		mTabHost.getTabWidget().getChildAt(1).getLayoutParams().height = Integer
				.parseInt(getResources().getString(R.string.tabs_size));
		mTabHost.getTabWidget().getChildAt(2).getLayoutParams().height = Integer
				.parseInt(getResources().getString(R.string.tabs_size));
		mTabHost.getTabWidget().getChildAt(3).getLayoutParams().height = Integer
				.parseInt(getResources().getString(R.string.tabs_size));
		mTabHost.getTabWidget().getChildAt(4).getLayoutParams().height = Integer
				.parseInt(getResources().getString(R.string.tabs_size));

		mTabHost.setCurrentTab(2);
		
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				if(currentTab==3){
					((KeypadActivity)currentFragment).keyPadTextView.setText("");
				}
			}
		});
		initBlocker();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		if (currentTab == 0) {
			getMenuInflater().inflate(R.menu.favorites, menu);
		} else if (currentTab == 1) {

		} else if (currentTab == 2) {
			getMenuInflater().inflate(R.menu.contacts, menu);
		} else if (currentTab == 3) {

		} else if (currentTab == 4) {

		}
		return true;
	}

	// Methods of contacts Tab

	public void openAddContacts() {
		Intent intent = new Intent(this, AddContactActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_up, R.anim.slide_in_up_exit);
	}

	public void openAddFavorites() {
		Intent intent = new Intent(this, AddToFavorite.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_up, R.anim.slide_in_up_exit);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

//		if (item.getItemId() == R.id.addcontact) {
//			openAddContacts();
//		} else if (item.getItemId() == R.id.add_favorites) {
//			openAddFavorites();
//		}

		return super.onOptionsItemSelected(item);
	}

	public void initBlocker() {
		loadData();
		stopService(new Intent(MainActivity.this, CallBlockerService.class));
		Intent i = new Intent(MainActivity.this, CallBlockerService.class);
		startService(i);
	}

	public void loadData() {
		try {
			FileInputStream fis = openFileInput("CallBlocker.data");
			ObjectInputStream objeto = new ObjectInputStream(fis);
			blackList = (HashMap<String, BlockedContact>) objeto.readObject();
			fis.close();
			objeto.close();
		} catch (Exception e) {
			blackList = new HashMap<String, BlockedContact>();
			// Log.e("Error", e.getMessage());
		}
	}
	


}
