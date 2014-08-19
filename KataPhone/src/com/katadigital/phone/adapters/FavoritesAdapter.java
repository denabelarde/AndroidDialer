package com.katadigital.phone.adapters;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.katadigital.phone.ContactDetailActivity;
import com.katadigital.phone.FavoritesActivity;
import com.katadigital.phone.MainActivity;
import com.katadigital.phone.R;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.entities.PhoneNumberDto;
import com.katadigital.phone.helpers.AlertDialogHelper;
import com.katadigital.phone.helpers.QuickContactHelper;

public class FavoritesAdapter extends BaseAdapter {

	private Context context;

	boolean editIsPressed = false;

	View rowView;

	ArrayList<ContactDto> favoriteList;
	Fragment fragment;
	Dialog selectNumberDialog;
	private ProgressDialog progressDialog;
	AlertDialogHelper alertDialog;

	public FavoritesAdapter(Context context,
			ArrayList<ContactDto> favoriteList, Fragment fragment,
			boolean editpress) {

		super();
		this.context = context;
		this.favoriteList = favoriteList;
		this.fragment = fragment;
		editIsPressed = editpress;
		alertDialog = new AlertDialogHelper();
	}

	public void updateResults(ArrayList<String> results) {

		// Triggers the list update
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return favoriteList.size();
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
		rowView = inflater
				.inflate(R.layout.favor_custom_list, viewGroup, false);
		TextView name = (TextView) rowView
				.findViewById(R.id.favor_item_textview);
		TextView numberType = (TextView) rowView
				.findViewById(R.id.favor_number_type);
		ImageView contactProfile = (ImageView) rowView
				.findViewById(R.id.logs_item_imageview);
		final ContactDto contactDto = favoriteList.get(i);
		contactProfile.setImageBitmap(contactDto.getProfilepic());

		if (contactDto != null) {
			name.setText(contactDto.getDisplayName());
			if (contactDto.getPhoneNumbers().size() > 0) {

				for (PhoneNumberDto phoneNumberDto : contactDto
						.getPhoneNumbers()) {
					if (!phoneNumberDto.getNumber().isEmpty()) {
						numberType.setText(phoneNumberDto.getNumberType());
						break;
					}
				}

			}

			Button faveInfoBtn = (Button) rowView
					.findViewById(R.id.favor_item_info_btn);

			faveInfoBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					new loadContactDetails(contactDto)
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					// System.out.println(favoritesNumberArray.get(i));
					//
					// Intent intent = new Intent(context,
					// FavoritesDetail.class);
					// Bundle extras = new Bundle();
					// if (favoritesArray.get(i) != "") {
					// extras.putString("fave_name", favoritesArray.get(i));
					// }
					// if (favoritesNumberArray.get(i) != "") {
					// extras.putString("fave_number",
					// favoritesNumberArray.get(i));
					// }
					// if (favoritesTypeArray.get(i) != "") {
					// extras.putString("fave_type", favoritesTypeArray.get(i));
					// }
					// if (favoritesCompanyArray.get(i) != "") {
					// extras.putString("fave_company",
					// favoritesCompanyArray.get(i));
					// }
					// if (favoritesIdArray.get(i) != null) {
					// extras.putInt("contactID", favoritesIdArray.get(i));
					// }
					//
					// intent.putExtras(extras);
					// context.startActivity(intent);
				}
			});

			if (editIsPressed == true) {
				LinearLayout myLayout = (LinearLayout) rowView
						.findViewById(R.id.favor_delete_btn_layout);

				Button myButton = new Button(context);
				myButton.setLayoutParams(new LinearLayout.LayoutParams(Integer
						.parseInt(((MainActivity) context).getResources()
								.getString(R.string.delete_btn_size)), Integer
						.parseInt(((MainActivity) context).getResources()
								.getString(R.string.delete_btn_size))));
				myButton.setBackgroundDrawable(context.getResources()
						.getDrawable(R.drawable.delete_item_img));
				myLayout.addView(myButton);

				myButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						AlertDialog.Builder adb = new AlertDialog.Builder(
								context);
						adb.setTitle("Favorites");
						adb.setIcon(R.drawable.warning);
						adb.setMessage("Are you sure you want to remove "
								+ contactDto.getDisplayName()
								+ " from favorites? ");
						adb.setNegativeButton("No", null);
						adb.setPositiveButton("Yes",
								new AlertDialog.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub

										ContentValues values = new ContentValues();

										String[] fv = new String[] { contactDto
												.getDisplayName() };

										values.put(
												ContactsContract.Contacts.STARRED,
												0);
										context.getContentResolver()
												.update(ContactsContract.Contacts.CONTENT_URI,
														values,
														ContactsContract.Contacts.DISPLAY_NAME
																+ "= ?", fv);
										((FavoritesActivity) fragment)
												.getFavoriteContacts(true);
										alertDialog
												.alertMessage(
														"Favorites Delete",
														"Contact removed from favorites!",
														context);
										System.out.println("Delete clicked");

									}

								});

						adb.show();

						// FavoritesActivity favor = new FavoritesActivity();
						// favor.refreshAdapter();
					}
				});
			}

			View listItem = rowView.findViewById(R.id.favor_list_item);
			listItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (contactDto.getPhoneNumbers().size() > 1) {
						openCallDialog(contactDto);
					} else if (contactDto.getPhoneNumbers().size() == 1) {
						try {
							if (!contactDto.getPhoneNumbers().get(0)
									.getNumber().isEmpty()) {
//								Intent callIntent = new Intent(
//										Intent.ACTION_CALL);
//
//								callIntent.setData(Uri.parse("tel:"
//										+ contactDto.getPhoneNumbers().get(0)
//												.getNumber()));
//								((MainActivity) context)
//										.startActivity(callIntent);
								context.startActivity(QuickContactHelper
										.callfromDefaultDialer(context, contactDto
												.getPhoneNumbers().get(0)
												.getNumber()));
							} else {
								Toast.makeText(((MainActivity) context),
										"Number is empty!", Toast.LENGTH_LONG)
										.show();
							}
						} catch (Exception e) {

						}
					}

				}
			});
		}
		return rowView;
	}

	public void openCallDialog(final ContactDto selectedContactDto) {
		LayoutInflater inflater = (LayoutInflater) ((MainActivity) context)
				.getLayoutInflater();
		PhoneNumbersDialogAdapter numbersAdapter = new PhoneNumbersDialogAdapter(
				((MainActivity) context), selectedContactDto.getPhoneNumbers());
		View customView = inflater.inflate(R.layout.numberlist_dialog, null);

		ListView numberlist = (ListView) customView
				.findViewById(R.id.numberlist);
		numberlist.setAdapter(numbersAdapter);
		numberlist.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {
				if (!selectedContactDto.getPhoneNumbers().get(myItemInt)
						.getNumber().isEmpty()) {
//					Intent callIntent = new Intent(Intent.ACTION_CALL);
//
//					callIntent.setData(Uri.parse("tel:"
//							+ selectedContactDto.getPhoneNumbers()
//									.get(myItemInt).getNumber()));
//					((MainActivity) context).startActivity(callIntent);
					selectNumberDialog.dismiss();
					context.startActivity(QuickContactHelper
							.callfromDefaultDialer(context, selectedContactDto
									.getPhoneNumbers().get(myItemInt)
									.getNumber()));
					
				} else {
					Toast.makeText(((MainActivity) context),
							"Number is empty!", Toast.LENGTH_LONG).show();
				}

			}
		});

		// Build the dialog
		selectNumberDialog = new Dialog(((MainActivity) context));
		// selectModelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		selectNumberDialog.setContentView(customView);
		selectNumberDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		selectNumberDialog.setTitle("CALL CONTACT");

		selectNumberDialog.show();
	}

	class loadContactDetails extends AsyncTask<String, String, String> {

		ContactDto contactDto;

		public loadContactDetails(ContactDto contactDto) {
			this.contactDto = contactDto;
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

			if (contactDto != null) {

				ContactDetailActivity.contactDto = contactDto;

			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();

			Intent intent = new Intent(context, ContactDetailActivity.class);
			context.startActivity(intent);
			((MainActivity) context).overridePendingTransition(
					R.anim.slide_in_up, R.anim.slide_in_up_exit);

			super.onPostExecute(result);
		}
	}
}
