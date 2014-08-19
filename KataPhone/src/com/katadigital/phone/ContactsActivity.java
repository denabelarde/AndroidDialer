package com.katadigital.phone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.katadigital.phone.helpers.AlphabetListAdapter;
import com.katadigital.phone.helpers.AlphabetListAdapter.Item;
import com.katadigital.phone.helpers.AlphabetListAdapter.Row;
import com.katadigital.phone.helpers.AlphabetListAdapter.Section;
import com.katadigital.phone.helpers.QuickContactHelper;

public class ContactsActivity extends Fragment implements
		LoaderCallbacks<Cursor> {

	View rootView;
	// View headerView;
	SimpleCursorAdapter mAdapter = null;
	private String mCurrentFilter = null;
	ArrayList<String> contactsArray;
	ListView contact_listview;
	EditText searchContacts;
	TextView myNumber;
	// private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]
	// {
	// Contacts._ID, Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER,
	// Contacts.LOOKUP_KEY };
	private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
			Contacts._ID, Contacts.DISPLAY_NAME, Contacts.LOOKUP_KEY };
	private String[] PHOTO_BITMAP_PROJECTION = new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO };
	QuickContactHelper quickContactHelper;
	private ProgressDialog progressDialog;
	String searchedName = "";

	// ALPHABET LISTVIEW VARIABLES
	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private List<Object[]> alphabet = new ArrayList<Object[]>();
	private HashMap<String, Integer> sections = new HashMap<String, Integer>();
	private int sideIndexHeight;
	private static float sideIndexX;
	private static float sideIndexY;
	private int indexListSize;
	// ArrayList<String> contactsDisplayNames = new ArrayList<String>();
	// ArrayList<String> contactsDisplayNamesUpperCase = new
	// ArrayList<String>();
	ArrayList<Integer> contactDtoIDList = new ArrayList<Integer>();
	ImageView addContactBtn;
	RelativeLayout addContactBtnWrapper;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.contacts, container, false);
		contactsArray = new ArrayList<String>();
//		if(((MainActivity) getActivity()).currentTab ==3){
//			((KeypadActivity)((MainActivity) getActivity()).currentFragment).keyPadTextView.setText("");
//		}
		
		
		((MainActivity) getActivity()).currentTab = 2;

		// getActivity().getActionBar().show();
		// getActivity().getActionBar().setDisplayOptions(
		// ActionBar.DISPLAY_SHOW_CUSTOM);
		// getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		// getActivity().getActionBar().setIcon(R.drawable.dummy);
		//
		// getActivity().getActionBar().setCustomView(R.layout.contacts_actionbar);
		//
		// ((MainActivity) getActivity()).currentTab = 2;
		// getActivity().invalidateOptionsMenu();
		// getActivity().getActionBar().hide();

		initActionbar();
		return rootView;
	}

	public void initActionbar() {

		addContactBtn = (ImageView) rootView.findViewById(R.id.contacts_addbtn);
		addContactBtnWrapper = (RelativeLayout) rootView
				.findViewById(R.id.contacts_addbtn_wrapper);

		addContactBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addContactBtn
							.setBackgroundResource(R.drawable.ic_add_contact_shaded);
					addContactBtnWrapper.setBackgroundColor(getResources()
							.getColor(R.color.holo_light_blue));
					break;
				case MotionEvent.ACTION_UP:
					addContactBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addContactBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				case MotionEvent.ACTION_CANCEL:
					addContactBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addContactBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				}
				return false;
			}
		});

		addContactBtnWrapper.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addContactBtn
							.setBackgroundResource(R.drawable.ic_add_contact_shaded);
					addContactBtnWrapper.setBackgroundColor(getResources()
							.getColor(R.color.holo_light_blue));
					break;
				case MotionEvent.ACTION_UP:
					addContactBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addContactBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				case MotionEvent.ACTION_CANCEL:
					addContactBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addContactBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				}
				return false;
			}
		});

		addContactBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openAddContacts();
			}
		});

		addContactBtnWrapper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openAddContacts();
			}
		});
		int mActionBarSize = 0;
		final TypedArray styledAttributes = getActivity().getTheme()
				.obtainStyledAttributes(
						new int[] { android.R.attr.actionBarSize });
		mActionBarSize = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();
		System.out.println(mActionBarSize + "<--actionbar size");

		TypedValue tv = new TypedValue();
		int actionBarHeight = 0;
		if (getActivity().getTheme().resolveAttribute(
				android.R.attr.actionBarSize, tv, true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
					getResources().getDisplayMetrics());
			System.out.println(actionBarHeight + "<--actionBarHeight");
		}
		if (mActionBarSize > 0) {
			RelativeLayout relative = (RelativeLayout) rootView
					.findViewById(R.id.contacts_actionbar);

			// Gets the layout params that will allow you to resize the layout
			LayoutParams params = relative.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = mActionBarSize;

		} else if (actionBarHeight > 0) {
			RelativeLayout relative = (RelativeLayout) rootView
					.findViewById(R.id.contacts_actionbar);

			// Gets the layout params that will allow you to resize the layout
			LayoutParams params = relative.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = actionBarHeight;
		}
	}

	public void openAddContacts() {
		Intent intent = new Intent(getActivity(), AddContactActivity.class);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_up,
				R.anim.slide_in_up_exit);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		quickContactHelper = new QuickContactHelper(getActivity());
		getLoaderManager().initLoader(0, null, this);

		mAdapter = new IndexedListAdapter(this.getActivity(),
				R.layout.list_item_contacts, null, new String[] {
						ContactsContract.Contacts.DISPLAY_NAME, Contacts._ID },
				new int[] { R.id.display_name });

		initComponents();

		// contact_listview.addHeaderView(headerView);

		// contact_listview.setTextFilterEnabled(true);

		searchContacts.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				ContactsActivity.this.mAdapter.getFilter().filter(cs);
				searchedName = cs + "";
				System.out.println(cs + " <--- CS");
				getLoaderManager()
						.restartLoader(0, null, ContactsActivity.this);

			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void afterTextChanged(Editable s) {
			}
		});

		searchContacts.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (event != null
						&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					InputMethodManager in = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);

					// NOTE: In the author's example, he uses an identifier
					// called searchBar. If setting this code on your EditText
					// then use v.getWindowToken() as a reference to your
					// EditText is passed into this callback as a TextView

					in.hideSoftInputFromWindow(
							searchContacts.getApplicationWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					// Must return true here to consume event
					return true;

				}
				return false;
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
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

		return new CursorLoader(getActivity(), baseUri,
				CONTACTS_SUMMARY_PROJECTION, selection, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
	public void onLoaderReset(Loader<Cursor> loader) {
		System.out.println("onLoader reset");
		mAdapter.swapCursor(null);
	}

	class IndexedListAdapter extends SimpleCursorAdapter implements
			SectionIndexer {

		// AlphabetIndexer alphaIndexer;

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
		searchContacts = (EditText) rootView.findViewById(R.id.search_contacts);
		contact_listview = (ListView) rootView.findViewById(R.id.contact_list);
		// LayoutInflater inflater = (LayoutInflater) getActivity()
		// .getLayoutInflater();
		// headerView = inflater.inflate(R.layout.contacts_listview_header,
		// null);
		// myNumber = (TextView) headerView.findViewById(R.id.mynumber);
		// TelephonyManager tm = (TelephonyManager) getActivity()
		// .getSystemService(Context.TELEPHONY_SERVICE);
		// String number = tm.getLine1Number();
		//
		// // TextView currNumber = (TextView) rootView
		// // .findViewById(R.id.my_number_field);
		//
		// if (number != null) {
		// myNumber.setText("My Number: " + number);
		// } else {
		// myNumber.setText("My Number: No number");
		// }
	}

	class loadContactDetails extends AsyncTask<String, String, String> {
		long contactID;

		public loadContactDetails(long contactID) {
			this.contactID = contactID;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog.show(getActivity(),
					"Please wait...", "Loading Contact Details...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			quickContactHelper = new QuickContactHelper(getActivity());

			ContactDetailActivity.contactDto = quickContactHelper
					.getContactDetails(contactID + "");
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			Intent intent = new Intent(getActivity(),
					ContactDetailActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_up,
					R.anim.slide_in_up_exit);

			super.onPostExecute(result);
		}
	}

	public void updateList() {
		LinearLayout sideIndex = (LinearLayout) getActivity().findViewById(
				R.id.sideIndex);
		sideIndex.removeAllViews();
		indexListSize = alphabet.size();
		if (indexListSize < 1) {
			return;
		}

		int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
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

			tmpTV = new TextView(getActivity());
			tmpTV.setText(tmpLetter);
			tmpTV.setGravity(Gravity.CENTER);
			tmpTV.setTextSize(10);
			tmpTV.setTextColor(getActivity().getResources().getColor(
					R.color.phoneapp_blue));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1);
			tmpTV.setLayoutParams(params);
			sideIndex.addView(tmpTV);
		}

		sideIndexHeight = sideIndex.getHeight();

		sideIndex.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// now you know coordinates of touch
				sideIndexX = event.getX();
				sideIndexY = event.getY();

				// and can display a proper item it country list
				displayListItem();

				return false;
			}
		});
	}

	public void displayListItem() {
		LinearLayout sideIndex = (LinearLayout) getActivity().findViewById(
				R.id.sideIndex);
		sideIndexHeight = sideIndex.getHeight();
		// compute number of pixels for every side index item
		double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

		// compute the item index for given event position belongs to
		int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

		// get the item (we can do it since we know item index)
		if (itemPosition < alphabet.size()) {
			Object[] indexItem = alphabet.get(itemPosition);
			int subitemPosition = sections.get(indexItem[0]);

			// ListView listView = (ListView) findViewById(android.R.id.list);
			contact_listview.setSelection(subitemPosition);
		}
	}

}
