package com.katadigital.phone.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.katadigital.phone.ContactDetailActivity;
import com.katadigital.phone.FavoritesActivity;
import com.katadigital.phone.LogsActivity;
import com.katadigital.phone.LogsDetail;
import com.katadigital.phone.MainActivity;
import com.katadigital.phone.R;
import com.katadigital.phone.entities.CallLogDto;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.helpers.AlertDialogHelper;
import com.katadigital.phone.helpers.QuickContactHelper;

public class LogsAdapter extends BaseAdapter {

	private Context context;
	boolean editIsOn = false;
	QuickContactHelper quickContactHelper;
	private ProgressDialog progressDialog;
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	ArrayList<CallLogDto> callLogsList;
	Fragment logsFragment;
	AlertDialogHelper alertDialog;

	public LogsAdapter(Context context, ArrayList<CallLogDto> callLogsList,
			Fragment logsFragment, boolean edit) {

		super();
		this.context = context;
		this.callLogsList = callLogsList;
		quickContactHelper = new QuickContactHelper(context);
		this.logsFragment = logsFragment;
		editIsOn = edit;
		alertDialog = new AlertDialogHelper();
	}

	@Override
	public int getCount() {
		return callLogsList.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(final int i, View view, ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.logs_custom_list, viewGroup,
				false);

		TextView textView1 = (TextView) rowView
				.findViewById(R.id.logs_item_text_view);
		TextView dateTextView = (TextView) rowView
				.findViewById(R.id.logs_item_date);
		TextView numberType = (TextView) rowView
				.findViewById(R.id.call_log_numbertype);
		ImageView callStatusIv = (ImageView) rowView
				.findViewById(R.id.logs_item_imageview);

		View listItem = rowView.findViewById(R.id.logs_list_item);

		final CallLogDto callLogDto = callLogsList.get(i);

		numberType.setText(callLogDto.getNumberType());

		listItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				Intent callIntent = new Intent(Intent.ACTION_CALL);
//				callIntent.setData(Uri.parse("tel:"
//						+ String.valueOf(callLogDto.getNumber())));
//				context.startActivity(callIntent);
				context.startActivity(QuickContactHelper.callfromDefaultDialer(context, callLogDto.getNumber()));
				
			}
		});

		if (callLogsList.get(i).getNumber() != null) {
			textView1.setText(callLogDto.getContactName());
			// Date d = Calendar.getInstance().getTime(); // Current time
			// Set
			// your
			// date
			// format
			String formattedDate = sdf.format(callLogDto.getCallDate());
			dateTextView.setText(formattedDate);

			if (callLogDto.getCallType().equalsIgnoreCase("INCOMING")) {
				callStatusIv.setImageResource(R.drawable.incoming_call);
			} else if (callLogDto.getCallType().equalsIgnoreCase("OUTGOING")) {
				callStatusIv.setImageResource(R.drawable.outgoing_call);
			} else {
				callStatusIv.setImageResource(R.drawable.missedcall);
			}
		} else {
			textView1.setText(" ");
		}

		Button logInfoBtn = (Button) rowView
				.findViewById(R.id.logs_item_info_btn);

		logInfoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new loadContactDetails(callLogDto)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		if (editIsOn == true) {
			LinearLayout myLayout = (LinearLayout) rowView
					.findViewById(R.id.logs_delete_btn_layout);

			Button myButton = new Button(context);
			myButton.setLayoutParams(new LinearLayout.LayoutParams(Integer
					.parseInt(((MainActivity) context).getResources()
							.getString(R.string.delete_btn_size)), Integer
					.parseInt(((MainActivity) context).getResources()
							.getString(R.string.delete_btn_size))));
			myButton.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.delete_item_img));
			myLayout.addView(myButton);

			myButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub

					AlertDialog.Builder adb = new AlertDialog.Builder(context);
					adb.setTitle("Call Logs");
					adb.setIcon(R.drawable.warning);
					adb.setMessage("Are you sure you want to remove "
							+ callLogDto.getContactName() + " from call logs? ");
					adb.setNegativeButton("No", null);
					adb.setPositiveButton("Yes",
							new AlertDialog.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

									int res = context
											.getContentResolver()
											.delete(android.provider.CallLog.Calls.CONTENT_URI,
													"_ID = "
															+ callLogDto
																	.getCallLogID(),
													null);
									if (res == 1) {
										// Log delete
//										if (((LogsActivity) logsFragment).allCallsIsActive == true) {
//											((LogsActivity) logsFragment).all
//													.performClick();
//										} else {
//											((LogsActivity) logsFragment).missed
//													.performClick();
//										}
										((LogsActivity) logsFragment).refreshLogs();
										alertDialog.alertMessage("Delete Log",
												"Number deleted!", context);
									} else {
										// Log not Delete
										alertDialog.alertMessage("Error",
												"No such number in call logs!",
												context);
									}

									System.out.println("Delete clicked");

								}

							});

					adb.show();

					// String strUriCalls = "Content://Call_log/Calls";
					// Uri UriCalls = Uri.parse(strUriCalls);
					// Cursor c = context.getContentResolver().query(UriCalls,
					// null, null, null, null);
					//
					// if (c.getCount() <= 0) {
					// Toast.makeText(context, "Call log empty",
					// Toast.LENGTH_SHORT).show();
					// }

					// while (c.moveToNext()) {
					// String strNumber = logsArray.get(i);
					// String queryString = "NUMBER='" + strNumber + "'";
					// Log.v("Number", queryString);
					//
					// int i = context.getContentResolver().delete(UriCalls,
					// queryString, null);
					//
					// if (i >= 1) {
					//
					// } else {
					//
					// }
					// }
				}
			});
		}

		return rowView;
	}

	class loadContactDetails extends AsyncTask<String, String, String> {
		CallLogDto callLogDto;
		String contactId = "";

		public loadContactDetails(CallLogDto callLogDto) {
			this.callLogDto = callLogDto;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			System.out.println("Showing log details");
			progressDialog = ProgressDialog.show(context, "Please wait...",
					"Loading Contact Details...");
			progressDialog.setCancelable(false);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			ContactDto contactDto = new ContactDto();

			contactId = quickContactHelper.getContactInformation(callLogDto
					.getNumber());

			System.out.println(contactId + " <--Contactid");
			if (!contactId.isEmpty()) {
				contactDto = quickContactHelper.getContactDetails(contactId);

				ContactDetailActivity.contactDto = contactDto;

				// LogsDetailExisting.contactDto = contactDto;
				// if (contactDto.getProfilepic() != null) {
				// System.out.println("May laman ang profpic");
				// }
				//
				// Intent intent = new Intent(context,
				// LogsDetailExisting.class);
				// Bundle extras = new Bundle();
				//
				// System.out.println(logsArray.get(i) + " <---- NUMBER!!");
				// if (logsArray.get(i) != "") {
				// extras.putString("logs_number", logsArray.get(i));
				// }
				// if (callDateArray.get(i) != null) {
				// SimpleDateFormat sdf = new SimpleDateFormat(
				// "dd-MM-yyyy"); // Set
				// // your
				// // date
				// // format
				// String formattedDate = sdf.format(callDateArray.get(i));
				// extras.putString("logs_date", formattedDate);
				//
				// SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
				// String formattedTime = sdf2.format(callDateArray.get(i));
				// extras.putString("logs_time", formattedTime);
				// }
				//
				// intent.putExtras(extras);
				// context.startActivity(intent);
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			if (!contactId.isEmpty()) {
				Intent intent = new Intent(context, ContactDetailActivity.class);
				intent.putExtra("selectednumber", callLogDto.getNumber());
				intent.putExtra("transactiondate",
						sdf.format(callLogDto.getCallDate()));
				System.out
						.println(callLogDto.getCallDate() + " <---- CALLDATE");
				context.startActivity(intent);
				((MainActivity) context).overridePendingTransition(
						R.anim.slide_in_up, R.anim.slide_in_up_exit);

			} else {
				Bundle extras = new Bundle();
				Intent intent = new Intent(context, LogsDetail.class);
				System.out.println(callLogDto.getNumber() + " <---- NUMBER!!");
				if (callLogDto.getNumber() != "") {
					extras.putString("logs_number", callLogDto.getNumber());
				}
				if (callLogDto.getCallDate() != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy"); // Set
																				// your
																				// date
																				// format
					String formattedDate = sdf.format(callLogDto.getCallDate());
					extras.putString("logs_date", formattedDate);

					SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
					String formattedTime = sdf2
							.format(callLogDto.getCallDate());
					extras.putString("logs_time", formattedTime);
				}
				intent.putExtras(extras);
				context.startActivity(intent);
				((MainActivity) context).overridePendingTransition(
						R.anim.slide_in_up, R.anim.slide_in_up_exit);
			}

			super.onPostExecute(result);
		}
	}

}