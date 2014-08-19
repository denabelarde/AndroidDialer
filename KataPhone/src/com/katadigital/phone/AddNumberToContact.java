package com.katadigital.phone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.katadigital.phone.AddToFavorite.loadContactDetails;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.entities.PhoneNumberDto;
import com.katadigital.phone.helpers.AlphabetListAdapter;
import com.katadigital.phone.helpers.QuickContactHelper;
import com.katadigital.phone.helpers.AlphabetListAdapter.Item;
import com.katadigital.phone.helpers.AlphabetListAdapter.Row;
import com.katadigital.phone.helpers.AlphabetListAdapter.Section;

public class AddNumberToContact extends Activity implements
		LoaderCallbacks<Cursor> {

	SimpleCursorAdapter mAdapter = null;
	private String mCurrentFilter = null;
	ArrayList<String> contactsArray;
	ListView contact_listview;
	EditText searchContacts;

	private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
			Contacts._ID, Contacts.DISPLAY_NAME, Contacts.LOOKUP_KEY };
	private String[] PHOTO_BITMAP_PROJECTION = new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO };
	QuickContactHelper quickContactHelper;
	private ProgressDialog progressDialog;
	String searchedName = "";
	String number = "";
	TextView addToNumberCaption;

	// ALPHABET LISTVIEW VARIABLES
	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private List<Object[]> alphabet = new ArrayList<Object[]>();
	private HashMap<String, Integer> sections = new HashMap<String, Integer>();
	private int addnumber_sideIndexHeight;
	private static float addnumber_sideIndexX;
	private static float addnumber_sideIndexY;
	private int indexListSize;
	ArrayList<Integer> contactDtoIDList = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_number_to_contact);
		contactsArray = new ArrayList<String>();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			number = extras.getString("number_to_add");
		}

		ActionBar ab = getActionBar();

		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);

		invalidateOptionsMenu();

		quickContactHelper = new QuickContactHelper(this);
		getLoaderManager().initLoader(0, null, AddNumberToContact.this);

		mAdapter = new IndexedListAdapter(this, R.layout.list_item_contacts,
				null, new String[] { ContactsContract.Contacts.DISPLAY_NAME,
						Contacts._ID }, new int[] { R.id.display_name });

		initComponents();

		addToNumberCaption = (TextView) findViewById(R.id.add_to_number_caption);
		addToNumberCaption.setText(getResources().getString(
				R.string.add_num_to_contacts)
				+ " " + number);
		searchContacts.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				AddNumberToContact.this.mAdapter.getFilter().filter(cs);
				searchedName = cs + "";
				System.out.println(cs + " <--- CS");
				getLoaderManager().restartLoader(0, null,
						AddNumberToContact.this);

				// mAdapter.notifyDataSetChanged();
				// mAdapter.getFilter().filter(cs);
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void afterTextChanged(Editable s) {
			}
		});

		contact_listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {

				if (contactDtoIDList.get(myItemInt) > 0) {

					new loadContactDetails(contactDtoIDList.get(myItemInt))
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				}

			}
		});
	}

	class IndexedListAdapter extends SimpleCursorAdapter implements
			SectionIndexer {

		AlphabetIndexer alphaIndexer;

		public IndexedListAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
		}

		@Override
		public Cursor swapCursor(Cursor c) {

			return super.swapCursor(c);
		}

		@Override
		public int getPositionForSection(int section) {
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}
	}

	// @Override
	// public void onListItemClick(ListView l, View v, int position, long id) {
	// /* Retrieving the phone numbers in order to see if we have more than one
	// */
	//
	// }

	public void initComponents() {
		searchContacts = (EditText) findViewById(R.id.addnumber_searchbar);
		contact_listview = (ListView) findViewById(R.id.add_numberlist);

	}

	class loadContactDetails extends AsyncTask<String, String, String> {
		long contactID;
		ContactDto contactDto;

		public loadContactDetails(long contactID) {
			this.contactID = contactID;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog.show(AddNumberToContact.this,
					"Please wait...", "Adding contact to favorites...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			quickContactHelper = new QuickContactHelper(AddNumberToContact.this);
			contactDto = new ContactDto();

			quickContactHelper = new QuickContactHelper(AddNumberToContact.this);
			contactDto = quickContactHelper.getContactDetails(contactID + "");

			EditContactsActivity.oldContactDto = contactDto;

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			Intent intent = new Intent(AddNumberToContact.this,
					EditContactsActivity.class);
			intent.putExtra("isFromContactDetails", false);
			intent.putExtra("unknownNumberToAdd", number);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_up,
					R.anim.slide_in_up_exit);
			progressDialog.dismiss();
			finish();
			super.onPostExecute(result);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_to_favorite, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLoadFinished(android.content.Loader<Cursor> loader,
			Cursor data) {
		// TODO Auto-generated method stub
		mAdapter.swapCursor(data);
		System.out.println("On loader finished");
		adapter = new AlphabetListAdapter();
		alphabet = new ArrayList<Object[]>();
		sections = new HashMap<String, Integer>();

		List<Row> rows = new ArrayList<Row>();
		int start = 0;
		int end = 0;
		String previousLetter = null;
		Object[] tmpIndexItem = null;
		Pattern numberPattern = Pattern.compile("[0-9]");
		contactDtoIDList = new ArrayList<Integer>();

		while (data.moveToNext()) {
			String contactDisplayName = data.getString(data
					.getColumnIndex(Contacts.DISPLAY_NAME));
			int contactid = data.getInt(data.getColumnIndex(Contacts._ID));
			String firstLetter = contactDisplayName.substring(0, 1)
					.toUpperCase();

			// Group numbers together in the scroller
			if (numberPattern.matcher(firstLetter).matches()) {
				firstLetter = "#";
			}

			// If we've changed to a new letter, add the previous letter to the
			// alphabet scroller
			if (previousLetter != null && !firstLetter.equals(previousLetter)) {
				end = rows.size() - 1;
				tmpIndexItem = new Object[3];
				tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
				tmpIndexItem[1] = start;
				tmpIndexItem[2] = end;
				alphabet.add(tmpIndexItem);

				start = end + 1;
			}

			// Check if we need to add a header row
			if (!firstLetter.equals(previousLetter)) {
				rows.add(new Section(firstLetter));
				sections.put(firstLetter, start);
				contactDtoIDList.add(0);
			}

			// Add the country to the list
			contactDtoIDList.add(contactid);
			rows.add(new Item(contactDisplayName));
			previousLetter = firstLetter;

		}

		if (previousLetter != null) {
			// Save the last letter
			tmpIndexItem = new Object[3];
			tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
			tmpIndexItem[1] = start;
			tmpIndexItem[2] = rows.size() - 1;
			alphabet.add(tmpIndexItem);
		}

		adapter.setRows(rows);
		contact_listview.setAdapter(adapter);
		// contact_listview.setFastScrollEnabled(true);
		updateList();
	}

	@Override
	public void onLoaderReset(android.content.Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		System.out.println("onLoader reset");
		mAdapter.swapCursor(null);
	}

	@Override
	public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		Uri baseUri;
		System.out.println("Oncreate Loader");
		if (mCurrentFilter != null) {
			baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
					Uri.encode(mCurrentFilter));
		} else {
			baseUri = Contacts.CONTENT_URI;
		}
		String selection = "";
		if (searchedName.isEmpty()) {
			selection = "((" + Contacts.DISPLAY_NAME + " NOTNULL)" + " AND ("
					+ Contacts.DISPLAY_NAME + " != '' ))";
		} else {
			System.out.println("Pasok sa searchedname not null");
			selection = "((" + Contacts.DISPLAY_NAME + " NOTNULL)" + " AND ("
					+ Contacts.DISPLAY_NAME + " != '' )) AND " + "(("
					+ Contacts.DISPLAY_NAME + " LIKE '%" + searchedName
					+ "%' ))";

			// selection= "((" + Contacts.DISPLAY_NAME + " NOTNULL)"
			// + " AND (" + Contacts.DISPLAY_NAME + " != '' )) AND ((" +
			// Contacts.DISPLAY_NAME + " LIKE 'Atmark%' ))";
		}

		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";

		return new CursorLoader(AddNumberToContact.this, baseUri,
				CONTACTS_SUMMARY_PROJECTION, selection, null, sortOrder);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		overridePendingTransition(0, R.anim.slide_out_down);
	}

	public void updateList() {
		LinearLayout addnumber_sideIndex = (LinearLayout) findViewById(R.id.addnumber_sideIndex);
		addnumber_sideIndex.removeAllViews();
		indexListSize = alphabet.size();
		if (indexListSize < 1) {
			return;
		}

		int indexMaxSize = (int) Math
				.floor(addnumber_sideIndex.getHeight() / 20);
		int tmpIndexListSize = indexListSize;
		while (tmpIndexListSize > indexMaxSize) {
			tmpIndexListSize = tmpIndexListSize / 2;
		}
		double delta;
		if (tmpIndexListSize > 0) {
			delta = indexListSize / tmpIndexListSize;
		} else {
			delta = 1;
		}

		TextView tmpTV;
		for (double i = 1; i <= indexListSize; i = i + delta) {
			Object[] tmpIndexItem = alphabet.get((int) i - 1);
			String tmpLetter = tmpIndexItem[0].toString();

			tmpTV = new TextView(AddNumberToContact.this);
			tmpTV.setText(tmpLetter);
			tmpTV.setGravity(Gravity.CENTER);
			tmpTV.setTextSize(10);
			tmpTV.setTextColor(getResources().getColor(R.color.phoneapp_blue));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1);
			tmpTV.setLayoutParams(params);
			addnumber_sideIndex.addView(tmpTV);
		}

		addnumber_sideIndexHeight = addnumber_sideIndex.getHeight();

		addnumber_sideIndex.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// now you know coordinates of touch
				addnumber_sideIndexX = event.getX();
				addnumber_sideIndexY = event.getY();

				// and can display a proper item it country list
				displayListItem();

				return false;
			}
		});
	}

	public void displayListItem() {
		LinearLayout addnumber_sideIndex = (LinearLayout) findViewById(R.id.addnumber_sideIndex);
		addnumber_sideIndexHeight = addnumber_sideIndex.getHeight();
		// compute number of pixels for every side index item
		double pixelPerIndexItem = (double) addnumber_sideIndexHeight
				/ indexListSize;

		// compute the item index for given event position belongs to
		int itemPosition = (int) (addnumber_sideIndexY / pixelPerIndexItem);

		// get the item (we can do it since we know item index)
		if (itemPosition < alphabet.size()) {
			Object[] indexItem = alphabet.get(itemPosition);
			int subitemPosition = sections.get(indexItem[0]);

			// ListView listView = (ListView) findViewById(android.R.id.list);
			contact_listview.setSelection(subitemPosition);
		}
	}

}
