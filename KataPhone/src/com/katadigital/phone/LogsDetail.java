package com.katadigital.phone;

import com.katadigital.phone.helpers.QuickContactHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LogsDetail extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logs_detail);

		ActionBar ab = getActionBar();

		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setDisplayShowHomeEnabled(true);
		// ab.setIcon(R.drawable.dummy);
		ab.setCustomView(R.layout.logsdetail_actionbar);
		ab.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();

		final String number = bundle.getString("logs_number");
		String callDate = bundle.getString("logs_date");
		String callTime = bundle.getString("logs_time");

		TextView numberText = (TextView) findViewById(R.id.logs_number_called);
		numberText.setText(number);

		TextView dateText = (TextView) findViewById(R.id.logs_date_called);
		dateText.setText(callDate);

		TextView timeText = (TextView) findViewById(R.id.logs_time_called);
		timeText.setText(callTime);

		final Button callNumber = (Button) findViewById(R.id.logs_call_number);
		callNumber.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Intent callIntent = new Intent(Intent.ACTION_CALL);
				// callIntent.setData(Uri.parse("tel:" + number));
				// startActivity(callIntent);
				startActivity(QuickContactHelper.callfromDefaultDialer(
						LogsDetail.this, number));
			}
		});

		final Button sendMessage = (Button) findViewById(R.id.logs_send_msg);
		sendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.setData(Uri.parse("sms:" + number));
				startActivity(sendIntent);
			}
		});

		final Button createNewContact = (Button) findViewById(R.id.logs_create_new_contact);
		createNewContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LogsDetail.this,
						AddContactActivity.class);
				intent.putExtra("preloaded_number", number);
				startActivity(intent);

				overridePendingTransition(R.anim.slide_in_up,
						R.anim.slide_in_up_exit);
			}
		});

		final Button addNumberToContacts = (Button) findViewById(R.id.logs_add_to_contacts);
		addNumberToContacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LogsDetail.this,
						AddNumberToContact.class);
				intent.putExtra("number_to_add", number);
				startActivity(intent);

				overridePendingTransition(R.anim.slide_in_up,
						R.anim.slide_in_up_exit);
			}
		});

		sendMessage.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sendMessage.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					sendMessage.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					sendMessage.setTextColor(Color.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});
		callNumber.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					callNumber.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					callNumber.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					callNumber.setTextColor(Color.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});

		createNewContact.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					createNewContact.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					createNewContact.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					createNewContact.setTextColor(Color.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});

		addNumberToContacts.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addNumberToContacts.setTextColor(Color
							.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					addNumberToContacts.setTextColor(Color
							.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					addNumberToContacts.setTextColor(Color
							.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();

		getMenuInflater().inflate(R.menu.logsdetail, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		// do something

		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		finish();
		overridePendingTransition(0, R.anim.slide_out_down);
	}
}
