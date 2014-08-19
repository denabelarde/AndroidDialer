package com.katadigital.phone;

import com.katadigital.phone.adapters.LogsExistingAdapter;
import com.katadigital.phone.adapters.PhoneNumbersAdapter;
import com.katadigital.phone.adapters.PhoneNumbersDialogAdapter;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.helpers.ListViewHelper;
import com.katadigital.phone.helpers.QuickContactHelper;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;
import android.provider.ContactsContract;

public class LogsDetailExisting extends Activity {

	// String name, fname, lname, phoneNumber, orgName, numType, email, street,
	// neighborhood, city, zip, country;
	ImageView detail_contact_img;
	QuickContactHelper quickContactHelper;
	public static ContactDto contactDto;
	LogsExistingAdapter phoneNumberAdapter;
	ListView phoneNumbersListview;
	Dialog selectNumberDialog;
	ArrayAdapter<String> listadapter;

	// public static void launch(Context c, String name, String fname,

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs_detail_existing);

		ActionBar ab = getActionBar();

		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String number = "";
		String callDate = "";
		String callTime = "";
		if (bundle != null) {
			number = bundle.getString("logs_number");
			callDate = bundle.getString("logs_date");
			callTime = bundle.getString("logs_time");
		}

		TextView callTimeText = (TextView) findViewById(R.id.logsexisting_calltime);
		callTimeText.setText(callDate + "\n" + callTime);

		System.out
				.println(contactDto.getContactID() + " <----- CONTACT ID NYA");
		phoneNumbersListview = (ListView) findViewById(R.id.logsexisting_lv);
		((TextView) findViewById(R.id.logsexisting_contact_name))
				.setText(contactDto.getFirstName() + " "
						+ contactDto.getLastName());

		phoneNumberAdapter = new LogsExistingAdapter(this,
				contactDto.getPhoneNumbers(), number);
		phoneNumbersListview.setAdapter(phoneNumberAdapter);
		phoneNumbersListview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {
				System.out.println("Item Clicked");
				// Intent callIntent = new Intent(Intent.ACTION_CALL);
				// callIntent.setData(Uri.parse("tel:"
				// + contactDto.getPhoneNumbers().get(myItemInt)
				// .getNumber()));
				// startActivity(callIntent);
				startActivity(QuickContactHelper.callfromDefaultDialer(
						LogsDetailExisting.this, contactDto.getPhoneNumbers()
								.get(myItemInt).getNumber()));
			}
		});

		ListViewHelper.setListViewHeightBasedOnChildren(phoneNumbersListview);
		// ((TextView) findViewById(R.id.detail_number)).setText(phoneNumber);
		// ((TextView) findViewById(R.id.detail_number_type)).setText(numType);
		((TextView) findViewById(R.id.logsexisting_comp_name))
				.setText(contactDto.getCompany());
		((TextView) findViewById(R.id.logsexisting_notes)).setText(contactDto
				.getNote());

		detail_contact_img = (ImageView) findViewById(R.id.logsexisting_contact_img);

		if (contactDto.getProfilepic() != null) {
			detail_contact_img.setImageBitmap(contactDto.getProfilepic());
		}

		final Button sendMessage = (Button) findViewById(R.id.logsexisting_send_msg);
		sendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openSendMessageDialog();
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

		final Button addToFave = (Button) findViewById(R.id.logsexisting_add_to_fave);
		addToFave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ContentValues values = new ContentValues();
				String[] fv = new String[] { contactDto.getFirstName() };
				values.put(ContactsContract.Contacts.STARRED, 1);
				getContentResolver().update(
						ContactsContract.Contacts.CONTENT_URI, values,
						ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);

				Toast.makeText(LogsDetailExisting.this, "Added to Favorites",
						Toast.LENGTH_SHORT).show();
			}
		});
		addToFave.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addToFave.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					addToFave.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					addToFave.setTextColor(Color.parseColor("#015abb"));
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

		getMenuInflater().inflate(R.menu.contactdetails, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contact_details_edit:
			// do something
			// EditContactsActivity.launch(ContactDetailActivity.this, name,
			// fname, lname, phoneNumber, orgName, numType, email, street,
			// neighborhood, city, zip, country);
			return true;
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

	public void openSendMessageDialog() {
		LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
		PhoneNumbersDialogAdapter numbersAdapter = new PhoneNumbersDialogAdapter(
				this, contactDto.getPhoneNumbers());
		View customView = inflater.inflate(R.layout.numberlist_dialog, null);

		ListView numberlist = (ListView) customView
				.findViewById(R.id.numberlist);
		numberlist.setAdapter(numbersAdapter);
		numberlist.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {
				if (!contactDto.getPhoneNumbers().get(myItemInt).getNumber()
						.isEmpty()) {
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					sendIntent.setData(Uri.parse("sms:"
							+ contactDto.getPhoneNumbers().get(myItemInt)
									.getNumber()));
					startActivity(sendIntent);
				} else {
					Toast.makeText(LogsDetailExisting.this, "Number is empty!",
							Toast.LENGTH_LONG).show();
				}

			}
		});

		// Build the dialog
		selectNumberDialog = new Dialog(this);
		// selectModelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		selectNumberDialog.setContentView(customView);
		selectNumberDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		selectNumberDialog.setTitle("SEND MESSAGE");

		selectNumberDialog.show();
	}

	public void fillDialogListWithNumbers() {

	}

}
