package com.katadigital.phone;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.katadigital.phone.adapters.AddressesAdapter;
import com.katadigital.phone.adapters.EmailsAdapter;
import com.katadigital.phone.adapters.PhoneNumbersAdapter;
import com.katadigital.phone.adapters.PhoneNumbersDialogAdapter;
import com.katadigital.phone.callsmsblocker.notificationCenter.CallBlockerToastNotification;
import com.katadigital.phone.callsmsblocker.objects.BlockedContact;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.entities.PhoneNumberDto;
import com.katadigital.phone.helpers.ListViewHelper;
import com.katadigital.phone.helpers.QuickContactHelper;

public class ContactDetailActivity extends Activity {

	// String name, fname, lname, phoneNumber, orgName, numType, email, street,
	// neighborhood, city, zip, country;
	ImageView detail_contact_img;
	QuickContactHelper quickContactHelper;
	public static ContactDto contactDto;
	PhoneNumbersAdapter phoneNumberAdapter;
	EmailsAdapter emailsAdapter;
	AddressesAdapter addressAdapter;
	ListView phoneNumbersListView, emailsListView, addressesListView;
	Dialog selectNumberDialog;
	ArrayAdapter<String> listadapter;
	String selectedNumber = "";
	Button addToFave;
	private ProgressDialog progressDialog;
	static boolean contactIsDeleted = false;
	Button blockCaller;
	Boolean isContactBlocked = false;

	// public static void launch(Context c, String name, String fname,
	// String lname, String phoneNumber, String orgName,
	// String numberType, String email, String street,
	// String neighborhood, String city, String zip, String country) {
	// Intent intent = new Intent(c, ContactDetailActivity.class);
	//
	// Bundle extras = new Bundle();
	// if (name != "") {
	// extras.putString("name", name);
	// }
	// if (fname != "") {
	// extras.putString("fname", fname);
	// }
	// if (lname != "") {
	// extras.putString("lname", lname);
	// }
	// if (phoneNumber != "") {
	// extras.putString("phoneNumber", phoneNumber);
	// }
	// if (orgName != "") {
	// extras.putString("orgName", orgName);
	// }
	// if (numberType != "") {
	// extras.putString("numberType", numberType);
	// }
	// if (email != "") {
	// extras.putString("email", email);
	// }
	// if (street != "") {
	// extras.putString("street", street);
	// }
	// if (neighborhood != "") {
	// extras.putString("neighborhood", neighborhood);
	// }
	// if (city != "") {
	// extras.putString("city", city);
	// }
	// if (zip != "") {
	// extras.putString("zip", zip);
	// }
	// if (country != "") {
	// extras.putString("country", country);
	// }
	//
	// intent.putExtras(extras);
	//
	// c.startActivity(intent);
	//
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_detail);

		ActionBar ab = getActionBar();

		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);
		contactIsDeleted = false;
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		quickContactHelper = new QuickContactHelper(this);
		if (bundle != null) {
			selectedNumber = bundle.getString("selectednumber");
			((TextView) findViewById(R.id.logsexisting_calltime))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.logsexisting_calltime))
					.setText(bundle.getString("transactiondate"));
		} else {
			selectedNumber = "";
			((TextView) findViewById(R.id.logsexisting_calltime))
					.setVisibility(View.GONE);
		}
		System.out
				.println(contactDto.getContactID() + " <----- CONTACT ID NYA");
		phoneNumbersListView = (ListView) findViewById(R.id.contactnumber_lv);
		emailsListView = (ListView) findViewById(R.id.email_lv);
		addressesListView = (ListView) findViewById(R.id.address_lv);

		phoneNumbersListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {
				// System.out.println("Item Clicked");
				// Intent callIntent = new Intent(Intent.ACTION_CALL);
				// callIntent.setData(Uri.parse("tel:"
				// + contactDto.getPhoneNumbers().get(myItemInt)
				// .getNumber()));
				// startActivity(callIntent);
				startActivity(QuickContactHelper.callfromDefaultDialer(
						ContactDetailActivity.this, contactDto
						.getPhoneNumbers().get(myItemInt).getNumber()));
				
			}
		});

		emailsListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {

				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[] { contactDto
						.getEmails().get(myItemInt).getEmail() });

				try {
					startActivity(Intent.createChooser(i, "Send Email"));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(ContactDetailActivity.this,
							"There are no email clients installed.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		final Button sendMessage = (Button) findViewById(R.id.detail_send_msg);
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

		final Button shareContact = (Button) findViewById(R.id.detail_share_contact);
		shareContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// // String[] projection = new String[] { Phone.DISPLAY_NAME,
				// // Phone.NUMBER,
				// // Phone.TYPE };
				// final Cursor mCursor = getContentResolver()
				// .query(Phone.CONTENT_URI,
				// null,
				// Data.CONTACT_ID + "=?",
				// new String[] { String.valueOf(contactDto
				// .getContactID()) }, null);
				// // Cursor mCursor;
				// // The index of the lookup key column in the cursor
				// int mLookupKeyIndex;
				// // The index of the contact's _ID value
				// int mIdIndex;
				// // The lookup key from the Cursor
				// String mCurrentLookupKey;
				// // The _ID value from the Cursor
				// long mCurrentId;
				// // A content URI pointing to the contact
				// Uri mSelectedContactUri;
				// mLookupKeyIndex =
				// mCursor.getColumnIndex(Contacts.LOOKUP_KEY);
				// // Gets the lookup key value
				// mCurrentLookupKey = mCursor.getString(mLookupKeyIndex);
				// // Gets the _ID column index
				// mIdIndex = mCursor.getColumnIndex(Contacts._ID);
				// mCurrentId = mCursor.getLong(mIdIndex);
				// mSelectedContactUri = Contacts.getLookupUri(mCurrentId,
				// mCurrentLookupKey);
				String[] PROJECTION = new String[] { ContactsContract.Contacts.LOOKUP_KEY };
				Cursor c = getContentResolver().query(
						ContactsContract.Data.CONTENT_URI, PROJECTION,
						ContactsContract.Data.CONTACT_ID + "=?",
						new String[] { contactDto.getContactID() }, null);
				c.moveToFirst();
				System.out.println(c.getString(0));
				Uri uri = Uri.withAppendedPath(
						ContactsContract.Contacts.CONTENT_VCARD_URI,
						c.getString(0));
				// AssetFileDescriptor fd;
				// try {
				// fd = getContentResolver().openAssetFileDescriptor(
				// uri, "r");
				// FileInputStream fis = fd.createInputStream();
				// byte[] b = new byte[(int) fd.getDeclaredLength()];
				// fis.read(b);
				// String vCard = new String(b);
				// System.out.println(vCard+ " <---VCARD");
				// } catch (FileNotFoundException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }

				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/x-vcard");
				i.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(i, "Send Contact"));
			}
		});
		shareContact.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					shareContact.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					shareContact.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					shareContact.setTextColor(Color.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});
		addToFave = (Button) findViewById(R.id.detail_add_to_fave);
		addToFave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (quickContactHelper.isContactFavorite(contactDto
						.getDisplayName()) == true) {
					ContentValues values = new ContentValues();
					String[] fv = new String[] { contactDto.getDisplayName() };
					values.put(ContactsContract.Contacts.STARRED, 0);
					getContentResolver().update(
							ContactsContract.Contacts.CONTENT_URI, values,
							ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);

					Toast.makeText(ContactDetailActivity.this,
							"Removed from Favorites", Toast.LENGTH_SHORT)
							.show();

					invalidateOptionsMenu();
				} else {
					if (contactDto.getPhoneNumbers().size() != 0) {
						System.out.println(contactDto.getDisplayName());
						ContentValues values = new ContentValues();
						String[] fv = new String[] { contactDto
								.getDisplayName() };
						values.put(ContactsContract.Contacts.STARRED, 1);
						getContentResolver().update(
								ContactsContract.Contacts.CONTENT_URI, values,
								ContactsContract.Contacts.DISPLAY_NAME + "= ?",
								fv);

						Toast.makeText(ContactDetailActivity.this,
								"Added to Favorites", Toast.LENGTH_SHORT)
								.show();

						invalidateOptionsMenu();
					} else {
						Toast.makeText(
								ContactDetailActivity.this,
								"Cannot add to favorites no contact numbers available",
								Toast.LENGTH_SHORT).show();
					}

				}

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

		blockCaller = (Button) findViewById(R.id.detail_block_caller);

		blockCaller.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (isContactBlocked == true) {
					unblockContact();
				} else {
					blockContact();
				}

			}
		});
		blockCaller.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					blockCaller.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					blockCaller.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					blockCaller.setTextColor(Color.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});

		isContactBlocked = checkIfContactIsBlocked();
		if (isContactBlocked == true) {
			blockCaller.setText(getResources().getString(
					R.string.unblock_caller_string));
		} else {
			blockCaller.setText(getResources().getString(
					R.string.block_caller_string));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();

		getMenuInflater().inflate(R.menu.contactdetails, menu);
		if (quickContactHelper.isContactFavorite(contactDto.getDisplayName()) == true) {
			menu.getItem(0).setIcon(R.drawable.ic_action_star_dark);
			addToFave.setText(getResources().getString(
					R.string.remove_from_favorites));
		} else {
			menu.getItem(0).setIcon(R.drawable.ic_action_star);
			addToFave.setText(getResources().getString(
					R.string.add_to_favorites));
		}

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

			new loadContactDetails()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			// finish();
			// ContactDetailActivity.this.overridePendingTransition(0,
			// R.anim.slide_out_down);
			//
			return true;
		case R.id.add_to_favorites:
			if (quickContactHelper.isContactFavorite(contactDto
					.getDisplayName()) == true) {
				ContentValues values = new ContentValues();
				String[] fv = new String[] { contactDto.getDisplayName() };
				values.put(ContactsContract.Contacts.STARRED, 0);
				getContentResolver().update(
						ContactsContract.Contacts.CONTENT_URI, values,
						ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);

				Toast.makeText(ContactDetailActivity.this,
						"Removed from Favorites", Toast.LENGTH_SHORT).show();

				invalidateOptionsMenu();
			} else {
				if (contactDto.getPhoneNumbers().size() != 0) {
					ContentValues values = new ContentValues();
					String[] fv = new String[] { contactDto.getDisplayName() };
					values.put(ContactsContract.Contacts.STARRED, 1);
					getContentResolver().update(
							ContactsContract.Contacts.CONTENT_URI, values,
							ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);

					Toast.makeText(ContactDetailActivity.this,
							"Added to Favorites", Toast.LENGTH_SHORT).show();

					invalidateOptionsMenu();
				} else {
					Toast.makeText(
							ContactDetailActivity.this,
							"Cannot add to favorites no contact numbers available",
							Toast.LENGTH_SHORT).show();
				}

			}

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
					Toast.makeText(ContactDetailActivity.this,
							"Number is empty!", Toast.LENGTH_LONG).show();
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

	public void removeEmptyDataInContactDto() {
		System.out.println(contactDto.getPhoneNumbers().size()
				+ " <---PhoneNumbers Size");
		ArrayList<PhoneNumberDto> dummyPhoneNumberList = new ArrayList<PhoneNumberDto>();
		for (int x = 0; x < contactDto.getPhoneNumbers().size(); x++) {
			System.out.println("Fucker");
			if (contactDto.getPhoneNumbers().get(x) != null) {
				if (!contactDto.getPhoneNumbers().get(x).getNumber().isEmpty()
						|| !contactDto.getPhoneNumbers().get(x).getNumberType()
								.isEmpty()) {
					dummyPhoneNumberList.add(contactDto.getPhoneNumbers()
							.get(x));
				}
			}
		}

		contactDto.phoneNumbers.clear();
		contactDto.phoneNumbers.addAll(dummyPhoneNumberList);

		for (int x = 0; x < contactDto.getEmails().size(); x++) {
			if (contactDto.getEmails().get(x) == null) {
				contactDto.getEmails().remove(x);
			} else {
				if (contactDto.getEmails().get(x).getEmail().isEmpty()
						|| contactDto.getEmails().get(x).getEmailType()
								.isEmpty()) {
					contactDto.getEmails().remove(x);
				}
			}

		}

		for (int x = 0; x < contactDto.getAddresses().size(); x++) {
			if (contactDto.getAddresses().get(x) == null) {
				contactDto.getAddresses().remove(x);
			} else {
				if ((contactDto.getAddresses().get(x).getCityStr().isEmpty()
						&& contactDto.getAddresses().get(x).getCountryStr()
								.isEmpty()
						&& contactDto.getAddresses().get(x).getNeigborhoodStr()
								.isEmpty()
						&& contactDto.getAddresses().get(x).getStreetStr()
								.isEmpty() && contactDto.getAddresses().get(x)
						.getZipCodeStr().isEmpty())
						|| contactDto.getAddresses().get(x).getAddressType()
								.isEmpty()) {
					contactDto.getAddresses().remove(x);
				}
			}

		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (contactIsDeleted == false) {
			((TextView) findViewById(R.id.detail_contact_name))
					.setText(contactDto.getFirstName() + " "
							+ contactDto.getLastName());

			contactDto = quickContactHelper.removeNullValues(contactDto);

			System.out.println(contactDto.getPhoneNumbers().size()
					+ " <-----Phone Numbers size %%%%%%");
			phoneNumberAdapter = new PhoneNumbersAdapter(this,
					contactDto.getPhoneNumbers(), selectedNumber);
			phoneNumbersListView.setAdapter(phoneNumberAdapter);

			emailsAdapter = new EmailsAdapter(this, contactDto.getEmails());
			emailsListView.setAdapter(emailsAdapter);

			addressAdapter = new AddressesAdapter(this,
					contactDto.getAddresses());
			addressesListView.setAdapter(addressAdapter);

			ListViewHelper
					.setListViewHeightBasedOnChildren(phoneNumbersListView);
			ListViewHelper.setListViewHeightBasedOnChildren(emailsListView);
			ListViewHelper.setListViewHeightBasedOnChildren(addressesListView);

			((TextView) findViewById(R.id.detail_comp_name)).setText(contactDto
					.getCompany());
			((TextView) findViewById(R.id.detail_notes)).setText(contactDto
					.getNote());
			((TextView) findViewById(R.id.detail_bday)).setText(contactDto
					.getBirthday());

			detail_contact_img = (ImageView) findViewById(R.id.detail_contact_img);

			if (contactDto.getProfilepic() != null) {
				detail_contact_img.setImageBitmap(contactDto.getProfilepic());
			}
		} else {
			contactIsDeleted = false;
			finish();
		}

		super.onResume();
	}

	class loadContactDetails extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			System.out.println("Showing log details");
			progressDialog = ProgressDialog.show(ContactDetailActivity.this,
					"Please wait...", "Loading Contact Details...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			EditContactsActivity.oldContactDto = contactDto;

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Intent intent = new Intent(ContactDetailActivity.this,
					EditContactsActivity.class);
			intent.putExtra("isFromContactDetails", true);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_up,
					R.anim.slide_in_up_exit);
			super.onPostExecute(result);
		}
	}

	public boolean checkIfContactIsBlocked() {
		boolean result = false;
		for (PhoneNumberDto phoneNumberDto : contactDto.getPhoneNumbers()) {
			if (!phoneNumberDto.getNumber().isEmpty()) {
				if (MainActivity.blackList.containsKey(phoneNumberDto
						.getNumber()) == true) {
					result = true;
					break;
				}
			}

		}
		return result;
	}

	public void blockContact() {
		for (PhoneNumberDto phoneNumberDto : contactDto.getPhoneNumbers()) {
			if (!phoneNumberDto.getNumber().isEmpty()) {
				int numberType = 0;
				for (int x = 0; x < getResources().getStringArray(
						R.array.numberspinner).length; x++) {
					if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							getResources()
									.getStringArray(R.array.numberspinner)[x])) {
						numberType = x;
					}
				}
				BlockedContact cn = new BlockedContact(
						contactDto.getDisplayName(),
						phoneNumberDto.getNumber(), numberType, true, true);
				System.out.println(cn.getNumber() + " <---number");
				System.out.println(cn.getName() + " <---name");
				MainActivity.blackList.put(cn.getNumber(), cn);
			}

		}
		if (contactDto.getPhoneNumbers().isEmpty()) {
			CallBlockerToastNotification
					.showDefaultShortNotification("No phone numbers to block!");
		} else {
			saveData();
			isContactBlocked = true;
			blockCaller.setText(getResources().getString(
					R.string.unblock_caller_string));
			CallBlockerToastNotification
					.showDefaultShortNotification("Contact added to blacklist successfully");
		}
	}

	public void saveData() {
		try {
			FileOutputStream fos = openFileOutput("CallBlocker.data",
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(MainActivity.blackList);
			fos.close();
			oos.close();
			// CallBlockerToastNotification
			// .showDefaultShortNotification("Saving Data...");
			((MainActivity) MainActivity.applicationContext).initBlocker();
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
		}
	}

	public void unblockContact() {
		for (PhoneNumberDto phoneNumberDto : contactDto.getPhoneNumbers()) {
			if (!phoneNumberDto.getNumber().isEmpty()) {
				System.out.println(phoneNumberDto.getNumber()
						+ " <-- number to unblock");
				MainActivity.blackList.remove(phoneNumberDto.getNumber());
			}

		}

		try {
			saveData();
			blockCaller.setText(getResources().getString(
					R.string.block_caller_string));
			isContactBlocked = false;
			CallBlockerToastNotification
					.showDefaultShortNotification("Contact removed from blacklist successfully");
		} catch (Exception e) {
			e.printStackTrace();
			CallBlockerToastNotification
					.showDefaultShortNotification("Failed to unblock contact");
		}

	}
}
