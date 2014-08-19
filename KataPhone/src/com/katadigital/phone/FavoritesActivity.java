package com.katadigital.phone;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.katadigital.phone.adapters.FavoritesAdapter;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.entities.PhoneNumberDto;
import com.katadigital.phone.helpers.QuickContactHelper;

public class FavoritesActivity extends Fragment {

	View rootView;
	// private ArrayList<Integer> favoritesIdArray;
	// private ArrayList<String> favoritesArray;
	// private ArrayList<String> favoritesNumberArray;
	// private ArrayList<String> favoritesTypeArray;
	// private ArrayList<String> favoritesCompanyArray;
	ArrayList<ContactDto> favoriteList = new ArrayList<ContactDto>();
	ContactDto contactDto;
	Button editBtn;
	ImageView addBtn;
	RelativeLayout addBtnLinear;
	QuickContactHelper quickContactHelper;
	ListView listView;
	Fragment currentFragment;
	boolean isEditing = false;
	private ProgressDialog progressDialog;
	ImageView addFavoritesBtn;
	RelativeLayout addFavoritesBtnWrapper;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootView = inflater.inflate(R.layout.favorites, container, false);
		((MainActivity) getActivity()).currentTab = 0;
//		getActivity().getActionBar().show();
//		getActivity().getActionBar().setDisplayOptions(
//				ActionBar.DISPLAY_SHOW_CUSTOM);
//		// getActivity().getActionBar().setDisplayShowHomeEnabled(true);
//		LayoutInflater inflator = (LayoutInflater) getActivity()
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View v = inflator.inflate(R.layout.favorites_actionbar, null);
//		getActivity().getActionBar().setCustomView(v);
//
//		((MainActivity) getActivity()).currentTab = 0;
//		getActivity().invalidateOptionsMenu();

		// addBtn = (ImageView) v.findViewById(R.id.fave_add_btn);
		initActionbar();
		// addBtnLinear = (RelativeLayout) v.findViewById(R.id.add_btn_linear);
		return rootView;
	}
	
	public void initActionbar() {
		editBtn = (Button) rootView.findViewById(R.id.fave_edit_btn);
		addFavoritesBtn = (ImageView) rootView.findViewById(R.id.favorites_addbtn);
		addFavoritesBtnWrapper = (RelativeLayout) rootView
				.findViewById(R.id.favorites_addbtn_wrapper);

		addFavoritesBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addFavoritesBtn
							.setBackgroundResource(R.drawable.ic_add_contact_shaded);
					addFavoritesBtnWrapper.setBackgroundColor(getResources()
							.getColor(R.color.holo_light_blue));
					break;
				case MotionEvent.ACTION_UP:
					addFavoritesBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addFavoritesBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				case MotionEvent.ACTION_CANCEL:
					addFavoritesBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addFavoritesBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				}
				return false;
			}
		});

		addFavoritesBtnWrapper.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addFavoritesBtn
							.setBackgroundResource(R.drawable.ic_add_contact_shaded);
					addFavoritesBtnWrapper.setBackgroundColor(getResources()
							.getColor(R.color.holo_light_blue));
					break;
				case MotionEvent.ACTION_UP:
					addFavoritesBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addFavoritesBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				case MotionEvent.ACTION_CANCEL:
					addFavoritesBtn
							.setBackgroundResource(R.drawable.ic_add_contact);
					addFavoritesBtnWrapper.setBackgroundColor(getResources()
							.getColor(android.R.color.transparent));
					break;
				}
				return false;
			}
		});

		addFavoritesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openAddFavorites();
			}
		});

		addFavoritesBtnWrapper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openAddFavorites();
			}
		});
		
		editBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (editBtn.getText().equals(
						getActivity().getResources().getString(
								R.string.edit_string))) {
					isEditing = true;
					FavoritesAdapter adapter = new FavoritesAdapter(
							getActivity(), favoriteList, currentFragment,
							isEditing);
					listView.setAdapter(adapter);
					editBtn.setText(getActivity().getResources().getString(
							R.string.done_string));

					// RelativeLayout.LayoutParams params = new
					// RelativeLayout.LayoutParams(100, 40);
					// params.setMargins(15,15,0,0);
					// editBtn.setLayoutParams(params);
				} else {
					isEditing = false;
					FavoritesAdapter adapter = new FavoritesAdapter(
							getActivity(), favoriteList, currentFragment,
							isEditing);
					listView.setAdapter(adapter);
					editBtn.setText(getActivity().getResources().getString(
							R.string.edit_string));

					// RelativeLayout.LayoutParams params = new
					// RelativeLayout.LayoutParams(100, 40);
					// params.setMargins(10,15,0,0);
					// editBtn.setLayoutParams(params);
				}
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
					.findViewById(R.id.favorites_actionbar);

			// Gets the layout params that will allow you to resize the layout
			LayoutParams params = relative.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = mActionBarSize;

		} else if (actionBarHeight > 0) {
			RelativeLayout relative = (RelativeLayout) rootView
					.findViewById(R.id.favorites_actionbar);

			// Gets the layout params that will allow you to resize the layout
			LayoutParams params = relative.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = actionBarHeight;
		}
	}
	public void openAddFavorites() {
		Intent intent = new Intent(getActivity(), AddToFavorite.class);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_in_up_exit);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		listView = (ListView) rootView.findViewById(R.id.favor_list);

		// final Button addBtn = (Button)
		// rootView.findViewById(R.id.fave_add_btn);
		// addBtn.setOnTouchListener(new View.OnTouchListener() {
		// @Override
		// public boolean onTouch(View view, MotionEvent motionEvent) {
		// switch (motionEvent.getAction()) {
		// case MotionEvent.ACTION_DOWN:
		// addBtnLinear.setBackgroundColor(getResources().getColor(
		// R.color.holo_light_blue));
		// break;
		// case MotionEvent.ACTION_UP:
		// addBtnLinear.setBackgroundColor(getResources().getColor(
		// android.R.color.transparent));
		// break;
		// case MotionEvent.ACTION_CANCEL:
		// addBtnLinear.setBackgroundColor(getResources().getColor(
		// android.R.color.transparent));
		// break;
		// }
		// return false;
		// }
		// });

		// final Button editBtn = (Button) rootView
		// .findViewById(R.id.fave_edit_btn);
		
		// editBtn.setOnTouchListener(new View.OnTouchListener() {
		// @Override
		// public boolean onTouch(View view, MotionEvent motionEvent) {
		// switch (motionEvent.getAction()) {
		// case MotionEvent.ACTION_DOWN:
		// editBtn.setTextColor(Color.parseColor("#a9a9a9"));
		// break;
		// case MotionEvent.ACTION_UP:
		// editBtn.setTextColor(Color.parseColor("#007aff"));
		// break;
		// case MotionEvent.ACTION_CANCEL:
		// editBtn.setTextColor(Color.parseColor("#007aff"));
		// break;
		// }
		// return false;
		// }
		// });

	}

	public void getFavoriteContacts(Boolean editing) {

		new loadAllFavorites(editing)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void getContactsNumbers() {
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cursorTwo = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null, "DISPLAY_NAME = '" + contactDto.getDisplayName() + "'",
				null, null);
		while (cursorTwo.moveToNext()) {
			String contactId = cursorTwo.getString(cursorTwo
					.getColumnIndex(ContactsContract.Contacts._ID));

			Cursor phones = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					Phone.CONTACT_ID + " = " + contactId, null, null);
			while (phones.moveToNext()) {
				String number = phones.getString(phones
						.getColumnIndex(Phone.NUMBER));
				int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				String callType = null;
				switch (type) {
				case Phone.TYPE_HOME:
					// do something with the Home number here...
					callType = "Home";
					break;
				case Phone.TYPE_MOBILE:
					// do something with the Mobile number here...
					callType = "Mobile";
					break;
				case Phone.TYPE_WORK:
					// do something with the Work number here...
					callType = "Work";
					break;
				}
				System.out.println(number + " <---Phonenumber");
				if (!number.isEmpty() && !callType.isEmpty()) {
					PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
					phoneNumberDto.setNumber(number);
					contactDto.addPhoneNumbers(phoneNumberDto);
				}

			}
			phones.close();
		}
		cursorTwo.close();
	}

	public void getCompanyName() {

		String orgName = null;
		String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] orgWhereParams = new String[] { contactDto.getContactID(),
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
		Cursor orgCur = getActivity().getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, orgWhere,
				orgWhereParams, null);
		orgCur.moveToFirst();
		int i = orgCur.getCount();
		if (!orgCur.isAfterLast()) {
			orgName = orgCur
					.getString(orgCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
			String title = orgCur
					.getString(orgCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
			if (orgName != null) {
				contactDto.setCompany(orgName);
			}

		} else {

		}
		orgCur.close();
	}

	public void refreshAdapter() {
		Toast.makeText(getActivity(), "TEST", Toast.LENGTH_SHORT).show();
	}

	class loadAllFavorites extends AsyncTask<String, String, String> {
		Boolean editing;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog.show(getActivity(),

			"Please wait...", "Loading Favorites...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		public loadAllFavorites(Boolean editing) {
			this.editing = editing;
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			favoriteList = new ArrayList<ContactDto>();
			quickContactHelper = new QuickContactHelper(getActivity());

			String[] projection = new String[] { ContactsContract.Contacts._ID,
					ContactsContract.Contacts.DISPLAY_NAME,
					ContactsContract.Contacts.STARRED };
			String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
					+ " COLLATE LOCALIZED ASC";
			Cursor cursor = getActivity().getContentResolver().query(
					ContactsContract.Contacts.CONTENT_URI, projection,
					"starred=?", new String[] { "1" }, sortOrder);

			int id = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			// int name = cursor
			// .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			// int number = cursor
			// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			ArrayList<String> contactIDList = new ArrayList<String>();
			if (cursor.moveToFirst()) {
				do {

					contactIDList.add(cursor.getString(id));

				} while (cursor.moveToNext());
			}
			System.out.println("Loading favorites onbackground");
			cursor.close();

			for (String stringID : contactIDList) {
				contactDto = new ContactDto();
				contactDto = quickContactHelper.getContactDetails(stringID);

				System.out.println(contactDto.getDisplayName()
						+ " <---Favename");
				favoriteList.add(contactDto);
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			FavoritesAdapter adapter = new FavoritesAdapter(getActivity(),
					favoriteList, currentFragment, editing);
			listView.setAdapter(adapter);
			if (listView.getCount() > 0) {
				editBtn.setVisibility(1);

			} else {
				adapter = new FavoritesAdapter(getActivity(), favoriteList,
						currentFragment, false);
				listView.setAdapter(adapter);
				editBtn.setVisibility(View.INVISIBLE);
			}
			System.out.println("Loading favorites onpostexecute");

			super.onPostExecute(result);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		currentFragment = this;
		getFavoriteContacts(isEditing);
		super.onResume();
	}

}
