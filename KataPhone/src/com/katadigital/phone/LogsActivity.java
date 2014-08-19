package com.katadigital.phone;

import java.util.ArrayList;
import java.util.Date;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.katadigital.phone.adapters.LogsAdapter;
import com.katadigital.phone.entities.CallLogDto;

public class LogsActivity extends Fragment implements View.OnClickListener {
	View rootView;
	public Button all, missed;

	// private ArrayList<String> contactNameArray;
	// private ArrayList<String> contactCompanyArray;
	// private ArrayList<String> logsArray;
	// private ArrayList<String> callStatusArray;
	// private ArrayList<Date> callDateArray;
	// private ArrayList<Integer> callLogsID;
	// private ArrayList<String> callType;
	ArrayList<CallLogDto> callLogList = new ArrayList<CallLogDto>();
	private ProgressDialog progressDialog;
	ListView listView;

	public boolean allCallsIsActive = true;
	Button clearBtn, editBtn;
	boolean isEditing = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootView = inflater.inflate(R.layout.logs, container, false);
		System.out.println("On createview");
		isEditing = false;
		((MainActivity) getActivity()).currentTab = 1;
		// getActivity().getActionBar().show();
		// getActivity().getActionBar().setDisplayOptions(
		// ActionBar.DISPLAY_SHOW_CUSTOM);
		// // getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		// LayoutInflater inflator = (LayoutInflater) getActivity()
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View v = inflator.inflate(R.layout.logs_actionbar, null);
		// getActivity().getActionBar().setCustomView(v);

		initActionbar();
		editBtn = (Button) rootView.findViewById(R.id.logs_edit_button);
		clearBtn = (Button) rootView.findViewById(R.id.logs_clear_btn);
		clearBtn.setVisibility(View.INVISIBLE);

		all = (Button) rootView.findViewById(R.id.all);
		missed = (Button) rootView.findViewById(R.id.missed);

		// ((MainActivity) getActivity()).currentTab = 1;
		// getActivity().invalidateOptionsMenu();

		listView = (ListView) rootView.findViewById(R.id.logs_list);

		all.setOnClickListener(this);
		missed.setOnClickListener(this);

		((MainActivity) getActivity()).logsActivity = LogsActivity.this;

		clearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
				adb.setTitle("Call Logs");
				adb.setIcon(R.drawable.warning);
				adb.setMessage("Are you sure you want to delete all call logs? ");
				adb.setNegativeButton("No", null);
				adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub

						getActivity().getContentResolver().delete(
								CallLog.Calls.CONTENT_URI, null, null);
						Toast.makeText(getActivity(), "All call logs deleted!",
								Toast.LENGTH_LONG).show();
						editBtn.performClick();
						refreshLogs();
					}

				});
				adb.show();
			}
		});

		editBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (editBtn.getText().equals("Edit")) {
					isEditing = true;
					if (allCallsIsActive == true) {
						getCallDetails();

						LogsAdapter adapter = new LogsAdapter(getActivity(),
								callLogList, LogsActivity.this, isEditing);

						listView.setAdapter(adapter);
					} else {
						getMissedCallDetails();
						LogsAdapter adapter = new LogsAdapter(getActivity(),
								callLogList, LogsActivity.this, isEditing);
						listView.setAdapter(adapter);
					}

					editBtn.setText("Done");
					clearBtn.setVisibility(View.VISIBLE);

					// RelativeLayout.LayoutParams params = new
					// RelativeLayout.LayoutParams(100, 40);
					// params.setMargins(15,15,0,0);
					// editBtn.setLayoutParams(params);
				} else {
					isEditing = false;
					if (allCallsIsActive == true) {
						getCallDetails();
						LogsAdapter adapter = new LogsAdapter(getActivity(),
								callLogList, LogsActivity.this, isEditing);
						listView.setAdapter(adapter);
					} else {
						getMissedCallDetails();
						LogsAdapter adapter = new LogsAdapter(getActivity(),
								callLogList, LogsActivity.this, isEditing);
						listView.setAdapter(adapter);
					}

					editBtn.setText("Edit");
					clearBtn.setVisibility(View.INVISIBLE);

					// RelativeLayout.LayoutParams params = new
					// RelativeLayout.LayoutParams(100, 40);
					// params.setMargins(10,15,0,0);
					// editBtn.setLayoutParams(params);
				}
			}
		});
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

		return rootView;
	}

	public void initActionbar() {
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
					.findViewById(R.id.logs_actionbar_wrapper);

			// Gets the layout params that will allow you to resize the layout
			LayoutParams params = relative.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = mActionBarSize;

		} else if (actionBarHeight > 0) {
			RelativeLayout relative = (RelativeLayout) rootView
					.findViewById(R.id.logs_actionbar_wrapper);

			// Gets the layout params that will allow you to resize the layout
			LayoutParams params = relative.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = actionBarHeight;
		}
	}

	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.all:
			if (isEditing == true) {
				editBtn.performClick();
			}
			all.setTextColor(getResources().getColor(R.color.White));
			all.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.leftselected));
			missed.setTextColor(getResources().getColor(R.color.phoneapp_blue));
			missed.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.rightunselected));

			new loadCallDetailsAsync()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			// LogsAdapter adapter = new LogsAdapter(getActivity(),
			// contactNameArray, contactCompanyArray, logsArray,
			// callStatusArray, callDateArray, false);
			// listView.setAdapter(adapter);
			allCallsIsActive = true;

			break;
		case R.id.missed:
			if (isEditing == true) {
				editBtn.performClick();
			}
			missed.setTextColor(Color.WHITE);
			missed.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.rightselected));
			all.setTextColor(Color.parseColor("#007AFF"));
			all.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.leftunselected));
			new loadMissedCallDetailsAsync()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			// LogsAdapter missedCallsAdapter = new LogsAdapter(getActivity(),
			// contactNameArray, contactCompanyArray, logsArray,
			// callStatusArray, callDateArray, false);
			// listView.setAdapter(missedCallsAdapter);
			allCallsIsActive = false;

			break;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		System.out.println("On activity created");

	}

	class loadCallDetailsAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			callLogList = new ArrayList<CallLogDto>();
			progressDialog = ProgressDialog.show(getActivity(),

			"Please wait...", "Loading All Calls...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			getCallDetails();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			LogsAdapter adapter = new LogsAdapter(getActivity(), callLogList,
					LogsActivity.this, isEditing);
			listView.setAdapter(adapter);
			progressDialog.dismiss();
			super.onPostExecute(result);
		}

	}

	class loadMissedCallDetailsAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			callLogList = new ArrayList<CallLogDto>();
			progressDialog = ProgressDialog.show(getActivity(),
					"Please wait...", "Loading Missed Calls...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			getMissedCallDetails();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			LogsAdapter missedCallsAdapter = new LogsAdapter(getActivity(),
					callLogList, LogsActivity.this, isEditing);
			listView.setAdapter(missedCallsAdapter);
			progressDialog.dismiss();
			super.onPostExecute(result);
		}

	}

	private void getCallDetails() {
		Cursor managedCursor = null;
		try {
			StringBuffer sb = new StringBuffer();
			String sortOrder = CallLog.Calls.DATE + " COLLATE LOCALIZED DESC";
			managedCursor = getActivity().getContentResolver().query(
					CallLog.Calls.CONTENT_URI, null, null, null, sortOrder);
			int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
			int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
			int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
			int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
			int id = managedCursor.getColumnIndex(CallLog.Calls._ID);
			int numbertype = managedCursor
					.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE);
			int contactName = managedCursor
					.getColumnIndex(CallLog.Calls.CACHED_NAME);
			sb.append("Call Log :");

			if (managedCursor.moveToFirst()) {
				do {
					CallLogDto callLogDto = new CallLogDto();
					String phNumber = managedCursor.getString(number);
					String callType = managedCursor.getString(type);
					String callDate = managedCursor.getString(date);
					String number_type = managedCursor.getString(numbertype);
					String callID = managedCursor.getString(id);
					String contact_name = managedCursor.getString(contactName)
							+ "";
					Date callDayTime = new Date(Long.valueOf(callDate));
					System.out.println(phNumber + "<-----Number");
					System.out.println(callDayTime + "<-----DAy time");
					System.out.println(number_type + "<-----NumberType");
					System.out.println(callID + "<-----callID");
					System.out.println(contact_name + "<-----contactName");
					// String callDuration = managedCursor.getString(duration);
					String dir = null;
					int dircode = Integer.parseInt(callType);
					switch (dircode) {
					case CallLog.Calls.OUTGOING_TYPE:
						dir = "OUTGOING";
						break;
					case CallLog.Calls.INCOMING_TYPE:
						dir = "INCOMING";
						break;
					case CallLog.Calls.MISSED_TYPE:
						dir = "MISSED";
						break;
					}

					if (dir != null) {
						if (!phNumber.equalsIgnoreCase("-1")) {

							if (contact_name.equalsIgnoreCase("null")) {
								callLogDto.setContactName(phNumber);
								callLogDto.setNumberType("unknown");
							} else {
								callLogDto.setContactName(contact_name);
								callLogDto.setNumberType(getActivity()
										.getResources().getStringArray(
												R.array.numberspinner)[Integer
										.parseInt(number_type)]);
							}
							callLogDto.setCallType(dir);
							callLogDto.setCallLogID(callID);
							callLogDto.setCallDate(callDayTime);
							callLogDto.setNumber(phNumber);

						} else {
							callLogDto.setContactName("Unknown");
							callLogDto.setCallType(dir);
							callLogDto.setCallLogID(callID);
							callLogDto.setCallDate(callDayTime);
							callLogDto.setNumber(phNumber);
							callLogDto.setNumberType("unknown");
						}
						callLogList.add(callLogDto);
					}

				} while (managedCursor.moveToNext());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (managedCursor != null && !managedCursor.isClosed()) {
					managedCursor.close();
				}
			} catch (Exception ex) {
			}
		}

	}

	private void getMissedCallDetails() {
		Cursor managedCursor = null;
		try {
			StringBuffer sb = new StringBuffer();
			String sortOrder = CallLog.Calls.DATE + " COLLATE LOCALIZED DESC";
			managedCursor = getActivity().getContentResolver().query(
					CallLog.Calls.CONTENT_URI, null, null, null, sortOrder);
			int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
			int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
			int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
			int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
			int id = managedCursor.getColumnIndex(CallLog.Calls._ID);
			int numbertype = managedCursor
					.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE);
			int contactName = managedCursor
					.getColumnIndex(CallLog.Calls.CACHED_NAME);
			sb.append("Call Log :");

			if (managedCursor.moveToFirst()) {
				do {
					CallLogDto callLogDto = new CallLogDto();
					String phNumber = managedCursor.getString(number);
					String callType = managedCursor.getString(type);
					String callDate = managedCursor.getString(date);
					String number_type = managedCursor.getString(numbertype);
					String callID = managedCursor.getString(id);
					String contact_name = managedCursor.getString(contactName)
							+ "";
					Date callDayTime = new Date(Long.valueOf(callDate));
					System.out.println(phNumber + "<-----Number");
					System.out.println(callDayTime + "<-----DAy time");
					System.out.println(number_type + "<-----NumberType");
					System.out.println(callID + "<-----callID");
					System.out.println(contact_name + "<-----contactName");
					// String callDuration = managedCursor.getString(duration);
					String dir = null;
					int dircode = Integer.parseInt(callType);
					switch (dircode) {
					case CallLog.Calls.OUTGOING_TYPE:
						dir = "OUTGOING";
						break;
					case CallLog.Calls.INCOMING_TYPE:
						dir = "INCOMING";
						break;
					case CallLog.Calls.MISSED_TYPE:
						dir = "MISSED";
						break;
					}

					if (dir != null) {
						if (dir.equalsIgnoreCase("MISSED")) {
							if (!phNumber.equalsIgnoreCase("-1")) {

								if (contact_name.equalsIgnoreCase("null")) {
									callLogDto.setContactName(phNumber);
									callLogDto.setNumberType("unknown");
								} else {
									callLogDto.setContactName(contact_name);
									callLogDto
											.setNumberType(getActivity()
													.getResources()
													.getStringArray(
															R.array.numberspinner)[Integer
													.parseInt(number_type)]);
								}
								callLogDto.setCallType(dir);
								callLogDto.setCallLogID(callID);
								callLogDto.setCallDate(callDayTime);
								callLogDto.setNumber(phNumber);

							} else {
								callLogDto.setContactName("Unknown");
								callLogDto.setCallType(dir);
								callLogDto.setCallLogID(callID);
								callLogDto.setCallDate(callDayTime);
								callLogDto.setNumber(phNumber);
								callLogDto.setNumberType("unknown");
							}

							callLogList.add(callLogDto);
						}

					}

				} while (managedCursor.moveToNext());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (managedCursor != null && !managedCursor.isClosed()) {
					managedCursor.close();
				}
			} catch (Exception ex) {
			}
		}
	}

	public void getContactInformation(String number) {
		// String contactId =
		// cursorTwo.getString(cursorTwo.getColumnIndex(ContactsContract.Contacts._ID));

		// Cursor phones =
		// getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
		// null,
		// ContactsContract.CommonDataKinds.Phone.NUMBER + " = " + number, null,
		// null);
		// while (phones.moveToNext()) {
		// int id = phones.getColumnIndex(ContactsContract.Contacts._ID);
		// int name =
		// phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		//
		// System.out.println("names: " + name);
		//
		// }

		Uri lookupUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		Cursor cur = getActivity().getContentResolver().query(
				uri,
				new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME,
						ContactsContract.PhoneLookup.NUMBER,
						ContactsContract.PhoneLookup._ID }, null, null, null);

		if (cur.moveToNext()) {
			int id = cur.getColumnIndex(ContactsContract.Contacts._ID);
			int name = cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			int num = cur.getColumnIndex(ContactsContract.PhoneLookup.NUMBER);

			String contactName = cur.getString(name);
			String numbers = cur.getString(num);
			String id2 = cur.getString(id);

			System.out.println("names: " + contactName);
			System.out.println("number:" + numbers);
			System.out.println("id2:" + id2);

		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		refreshLogs();
		super.onResume();
	}

	public void refreshLogs() {
		if (allCallsIsActive == true) {
			new loadCallDetailsAsync()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new loadMissedCallDetailsAsync()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

}
