package com.katadigital.phone;

import com.katadigital.phone.helpers.QuickContactHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FavoritesDetail extends Activity {

	String phoneNumber = null;
	String name3 = null;
	String numberType = null;
	String companyName = null;
	String givenName = null;
	String familyName = null;
	String email = null;
	String emailType = null;
	String orgName = null;
	String street = null;
	String neighborhood = null;
	String city = null;
	String zipCode = null;
	String country = null;
	int contactID;
	String fave_name, fave_number, fave_type, fave_company;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites_detail);

		ActionBar ab = getActionBar();

		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setDisplayShowHomeEnabled(true);
		// ab.setIcon(R.drawable.dummy);
		ab.setCustomView(R.layout.favoritesdetail_actionbar);
		ab.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();

		contactID = bundle.getInt("contactID");
		fave_name = bundle.getString("fave_name");
		fave_number = bundle.getString("fave_number");
		fave_type = bundle.getString("fave_type");
		fave_company = bundle.getString("fave_company");

		TextView nameText = (TextView) findViewById(R.id.fave_contact_name);
		nameText.setText(fave_name);

		TextView numberText = (TextView) findViewById(R.id.fave_number);
		numberText.setText(fave_number);

		TextView typeText = (TextView) findViewById(R.id.fave_number_type);
		typeText.setText(fave_type);

		TextView compText = (TextView) findViewById(R.id.fave_comp_name);
		compText.setText(fave_company);

		Button callBtn = (Button) findViewById(R.id.fave_detail_call_btn);
		callBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Intent callIntent = new Intent(Intent.ACTION_CALL);
				// callIntent.setData(Uri.parse("tel:" + fave_number));
				// startActivity(callIntent);
				startActivity(QuickContactHelper.callfromDefaultDialer(
						FavoritesDetail.this, fave_number));

			}
		});

		Button msgBtn = (Button) findViewById(R.id.fave_detail_msg_btn);
		msgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.setData(Uri.parse("sms:" + fave_number));
				startActivity(sendIntent);
			}
		});

		final Button sendMessage = (Button) findViewById(R.id.fave_send_msg);
		sendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.setData(Uri.parse("sms:" + fave_number));
				startActivity(sendIntent);
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

	}

	public void getOtherContactDetials(int id) {

		String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.TYPE };
		final Cursor phoneCursor = FavoritesDetail.this.getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						projection, ContactsContract.Data.CONTACT_ID + "=?",
						new String[] { String.valueOf(id) }, null);

		if (phoneCursor.moveToFirst() && phoneCursor.isLast()) {
			final int contactNumberColumnIndex = phoneCursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			phoneNumber = phoneCursor.getString(contactNumberColumnIndex);
			name3 = phoneCursor
					.getString(phoneCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			int numType = phoneCursor
					.getInt(phoneCursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
			switch (numType) {
			case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
				// do something with the Home number here...
				numberType = "Home";
				break;
			case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
				// do something with the Mobile number here...
				numberType = "Mobile";
				break;
			case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
				// do something with the Work number here...
				numberType = "Work";
				break;
			}
		}

		String nameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] nameWhereParams = new String[] {
				String.valueOf(id),
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
		Cursor nameCur = FavoritesDetail.this.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, nameWhere,
				nameWhereParams, null);
		if (nameCur.moveToNext()) {
			givenName = nameCur
					.getString(nameCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
			familyName = nameCur
					.getString(nameCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
		}
		nameCur.close();

		String emailWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] emailWhereParams = new String[] { String.valueOf(id),
				ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE };
		Cursor emailCur = FavoritesDetail.this.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, emailWhere,
				emailWhereParams, null);
		while (emailCur.moveToNext()) {
			email = emailCur
					.getString(emailCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		}
		emailCur.close();

		String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] orgWhereParams = new String[] { String.valueOf(id),
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
		Cursor orgCur = FavoritesDetail.this.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, orgWhere,
				orgWhereParams, null);
		while (orgCur.moveToNext()) {
			orgName = orgCur
					.getString(orgCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
			String title = orgCur
					.getString(orgCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
		}
		orgCur.close();

		String addressWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] addressWhereParams = new String[] {
				String.valueOf(id),
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
		Cursor addressCur = FavoritesDetail.this.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, addressWhere,
				addressWhereParams, null);
		while (addressCur.moveToNext()) {
			street = addressCur
					.getString(addressCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
			neighborhood = addressCur
					.getString(addressCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
			city = addressCur
					.getString(addressCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
			zipCode = addressCur
					.getString(addressCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
			country = addressCur
					.getString(addressCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
		}
		addressCur.close();
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
		// case R.id.logsdetail_edit:
		// // do something
		// getOtherContactDetials(contactID);
		// // EditContactsActivity.launch(FavoritesDetail.this, fave_name,
		// // givenName, familyName, fave_number, fave_company,
		// // fave_type, email, street, neighborhood, city, zipCode,
		// // country);
		// return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}