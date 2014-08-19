package com.katadigital.phone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.katadigital.phone.entities.AddressDto;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.entities.EmailDto;
import com.katadigital.phone.entities.PhoneNumberDto;
import com.katadigital.phone.helpers.AlertDialogHelper;
import com.katadigital.phone.helpers.QuickContactHelper;

public class EditContactsActivity extends Activity {

	public static int count = 0;
	TextView currentDelete = null;

	ImageView imageView;

	String imagePath;

	ArrayList<String> countriesArray;

	private ProgressDialog progressDialog;
	boolean success = false;
	String returnmessage;
	ArrayList<String> numberSpinnerlist = new ArrayList<String>();
	ArrayList<String> emailSpinnerlist = new ArrayList<String>();
	ArrayList<String> addressSpinnerlist = new ArrayList<String>();

	int phoneNumberId = 0;
	int emailId = 0;
	int addressId = 0;
	public ContactDto contactDto = new ContactDto();
	public static ContactDto oldContactDto;

	boolean hasBirthday = false;
	boolean hasNote = false;
	EditText edit_first_name, edit_last_name, edit_company_name;
	QuickContactHelper quickContactHelper;
	AlertDialogHelper alertDialog = new AlertDialogHelper();
	boolean isFromContactDetailsPage = false;
	String unknownNumberToAdd = "";

	public ArrayList<String> getCountries() {

		Locale[] locales = Locale.getAvailableLocales();
		ArrayList<String> countries = new ArrayList<String>();
		for (Locale locale : locales) {
			String country = locale.getDisplayCountry();
			if (country.trim().length() > 0 && !countries.contains(country)) {
				countries.add(country);
			}
		}
		Collections.sort(countries);
		for (String country : countries) {
			System.out.println("countries: " + country);
		}
		System.out.println("# countries found: " + countries.size());
		return countries;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_contacts);
		ActionBar ab = getActionBar();

		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);
		quickContactHelper = new QuickContactHelper(this);
		CropperActivity.selectedImage = null;
		// fillSpinnerList();
		contactDto = new ContactDto();
		imageView = (ImageView) this.findViewById(R.id.edit_photo_image);
		edit_first_name = (EditText) findViewById(R.id.edit_first_name);
		edit_last_name = (EditText) findViewById(R.id.edit_last_name);
		edit_company_name = (EditText) findViewById(R.id.edit_company_name);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			isFromContactDetailsPage = extras
					.getBoolean("isFromContactDetails");
			if (isFromContactDetailsPage == false) {
				unknownNumberToAdd = extras.getString("unknownNumberToAdd");
			}

		}

		final TextView deleteContact = (TextView) findViewById(R.id.edit_delete_contact);
		deleteContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder adb = new AlertDialog.Builder(
						EditContactsActivity.this);
				adb.setTitle("Delete Contact");
				adb.setIcon(R.drawable.warning);
				adb.setMessage("Are you sure you want to delete "
						+ oldContactDto.getDisplayName() + "?");
				adb.setNegativeButton("No", null);
				adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<android.content.ContentProviderOperation>();

						String[] args = new String[] { oldContactDto
								.getContactID() };
						ops.add(ContentProviderOperation
								.newDelete(RawContacts.CONTENT_URI)
								.withSelection(RawContacts.CONTACT_ID + "=?",
										args).build());
						try {
							getContentResolver().applyBatch(
									ContactsContract.AUTHORITY, ops);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (OperationApplicationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Toast.makeText(EditContactsActivity.this,
								"Contact deleted!", Toast.LENGTH_LONG).show();
						if (isFromContactDetailsPage == true) {
							ContactDetailActivity.contactIsDeleted = true;
						}

						onBackPressed();
					}

				});

				adb.show();
			}
		});
		deleteContact.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					deleteContact.setTextColor(Color.parseColor("#a9a9a9"));
					break;
				case MotionEvent.ACTION_UP:
					deleteContact.setTextColor(Color.parseColor("#015abb"));
					break;
				case MotionEvent.ACTION_CANCEL:
					deleteContact.setTextColor(Color.parseColor("#015abb"));
					break;
				}
				return false;
			}
		});

		final View addNumberField = findViewById(R.id.edit_add_phone_view);

		addNumberField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createNumberField();
			}
		});

		final View addEmailField = findViewById(R.id.edit_add_email_view);
		addEmailField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createEmailField();
			}
		});

		final View addBdayField = findViewById(R.id.edit_add_birthday_view);
		addBdayField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (hasBirthday == false) {
					createBirthdayField();
				}

			}
		});

		final View addAddressField = findViewById(R.id.edit_add_address_view);
		addAddressField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createAddressField();
			}
		});

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (contactDto.getProfilepic() == null) {
					Intent intent = new Intent(EditContactsActivity.this,
							CropperActivity.class);
					CropperActivity.clearBitmaps();
					startActivityForResult(intent, 1);
				} else {

					Intent intent = new Intent(EditContactsActivity.this,
							CropperActivity.class);
					startActivityForResult(intent, 1);
				}

			}

		});

		final View addNoteField = findViewById(R.id.edit_add_note_view);
		addNoteField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (hasNote == false) {
					createNoteField();
				}

			}
		});

		fillNewContactDto();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				imagePath = data.getStringExtra("resultimage");
				// photo = CropperActivity.croppedImage;
				contactDto.setProfilepic(CropperActivity.croppedImage);
				// imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(
				// contactDto.getProfilepic(), 1000));
				imageView.setImageBitmap(contactDto.getProfilepic());
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}

	}

	public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
		int targetWidth = 240;
		int targetHeight = 240;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2,
				((float) targetHeight - 1) / 2,
				(Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
				Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
				sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
				targetHeight), null);
		return targetBitmap;
	}

	public void createNumberField() {

		PhoneNumberDto phoneNumberDto = new PhoneNumberDto();

		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_number_fld_holder);
		Button myButton = new Button(EditContactsActivity.this);
		final Spinner spinner = new Spinner(EditContactsActivity.this);
		final EditText numberText = new EditText(EditContactsActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				contactDto.phoneNumbers.remove(spinner.getId());
				contactDto.phoneNumbers.add(spinner.getId(), null);
				innerLayout.removeAllViews();
			}
		});

		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 3) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);

					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}
				System.out.println(innerLayout.getChildCount()
						+ " <---- Child Count");
			}
		});

		spinner.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				EditContactsActivity.this, R.array.numberspinner,
				android.R.layout.simple_spinner_item);

		// ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		// this, R.layout.spinnertext_view, numberSpinnerlist);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setId(phoneNumberId);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();
				System.out.println(arg0.getId() + " <---Spinner id");

				contactDto.phoneNumbers.get(arg0.getId())
						.setNumberType(
								getResources().getStringArray(
										R.array.numberspinner)[index]);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		numberText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		numberText.setHint("Phone");
		numberText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_CLASS_PHONE);
		numberText.setId(phoneNumberId);
		numberText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredNumber = String.valueOf(charSequence);
				System.out.println(numberText.getId() + "<--- Numbertext id");
				contactDto.phoneNumbers.get(numberText.getId()).setNumber(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		phoneNumberDto.setId(phoneNumberId);
		phoneNumberDto.setNumberType(getResources().getStringArray(
				R.array.numberspinner)[spinner.getSelectedItemPosition()]);
		phoneNumberDto.setNumber("");

		contactDto.addPhoneNumbers(phoneNumberDto);
		phoneNumberId++;
		innerLayout.addView(numberText);

		// innerLayout.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.border_bottom));

		holderLayout.addView(innerLayout);
	}

	public void createEmailField() {
		EmailDto emailDto = new EmailDto();

		final Spinner spinner = new Spinner(EditContactsActivity.this);
		final EditText emailText = new EditText(EditContactsActivity.this);
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_email_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				contactDto.emails.remove(spinner.getId());
				contactDto.emails.add(spinner.getId(), null);
				innerLayout.removeAllViews();
			}
		});

		Button myButton = new Button(EditContactsActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 3) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}

			}
		});

		spinner.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				EditContactsActivity.this, R.array.emailspinner,
				android.R.layout.simple_spinner_item);
		// ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		// this, R.layout.spinnertext_view, emailSpinnerlist);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setId(emailId);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();

				// emails = getResources().getStringArray(R.array.emailspinner);
				// selected_email_type = emails[index];

				contactDto.emails.get(arg0.getId())
						.setEmailType(
								getResources().getStringArray(
										R.array.emailspinner)[index]);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		emailText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		emailText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		emailText.setHint("Email");
		emailText.setId(emailId);
		emailText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredEmail = String.valueOf(charSequence);
				contactDto.emails.get(emailText.getId()).setEmail(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		emailDto.setId(emailId);
		emailDto.setEmailType(getResources().getStringArray(
				R.array.emailspinner)[spinner.getSelectedItemPosition()]);

		emailDto.setEmail("");

		contactDto.addEmails(emailDto);
		emailId++;

		innerLayout.addView(emailText);

		holderLayout.addView(innerLayout);
	}

	public void createNoteField() {

		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_note_fld_holder);
		Button myButton = new Button(EditContactsActivity.this);

		final EditText noteText = new EditText(EditContactsActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				contactDto.setNote("");
				hasNote = false;
				innerLayout.removeAllViews();
			}
		});

		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (innerLayout.getChildCount() == 2) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}
			}
		});

		noteText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		noteText.setHint("Note");
		noteText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_CLASS_TEXT);
		noteText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredNumber = String.valueOf(charSequence);
				System.out.println(noteText.getId() + "<--- Numbertext id");
				contactDto.setNote(String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		innerLayout.addView(noteText);

		// innerLayout.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.border_bottom));
		hasNote = true;
		holderLayout.addView(innerLayout);
	}

	public void createBirthdayField() {
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_bDay_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hasBirthday = false;
				contactDto.setBirthday("");
				innerLayout.removeAllViews();
			}
		});

		Button myButton = new Button(EditContactsActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 2) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}

			}
		});

		final EditText bdayText = new EditText(EditContactsActivity.this);
		bdayText.setKeyListener(null);
		bdayText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		bdayText.setHint("MM - DD - YYYY");
		bdayText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredBday = String.valueOf(charSequence);
				contactDto.setBirthday(String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		bdayText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Calendar mcurrentDate = Calendar.getInstance();
				int mYear = mcurrentDate.get(Calendar.YEAR);
				int mMonth = mcurrentDate.get(Calendar.MONTH);
				int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog mDatePicker = new DatePickerDialog(
						EditContactsActivity.this,
						new DatePickerDialog.OnDateSetListener() {
							public void onDateSet(DatePicker datepicker,
									int selectedyear, int selectedmonth,
									int selectedday) {
								// TODO Auto-generated method stub

								String year1 = String.valueOf(selectedyear);
								String month1 = String
										.valueOf(selectedmonth + 1);
								String day1 = String.valueOf(selectedday);
								// SimpleDateFormat sdf = new
								// SimpleDateFormat("dd-MM-yyyy HH:mm"); // Set
								// your date format
								// String formattedDate =
								// sdf.format(callDateArray.get(i));
								bdayText.setText(month1 + "-" + day1 + "-"
										+ year1);
							}
						}, mYear, mMonth, mDay);
				mDatePicker.setTitle("Select date");
				mDatePicker.show();
			}
		});

		innerLayout.addView(bdayText);

		hasBirthday = true;
		holderLayout.addView(innerLayout);
	}

	public void createAddressField() {
		AddressDto addressDto = new AddressDto();
		final Spinner spinner2 = new Spinner(EditContactsActivity.this);
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_address_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.border_bottom));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		innerLayout.setGravity(Gravity.LEFT);
		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams2.setMargins(0, 0, 0, 5);
		innerLayout.setLayoutParams(layoutParams2);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 68);
		layoutParams.setMargins(0, 0, 0, 10);
		deleteBtn.setLayoutParams(layoutParams);

		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				contactDto.addresses.remove(spinner2.getId());
				contactDto.addresses.add(spinner2.getId(), null);
				innerLayout.removeAllViews();
			}
		});

		Button myButton = new Button(EditContactsActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 7) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}

			}
		});

		final EditText streetText = new EditText(EditContactsActivity.this);
		streetText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		streetText.setHint("Street");
		streetText.setId(addressId);
		streetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredStreet = String.valueOf(charSequence);
				contactDto.addresses.get(streetText.getId()).setStreetStr(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(streetText);

		final EditText neighborHoodText = new EditText(
				EditContactsActivity.this);
		neighborHoodText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		neighborHoodText.setHint("Neighborhood");
		neighborHoodText.setId(addressId);
		neighborHoodText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredNeighborhood = String.valueOf(charSequence);
				contactDto.addresses.get(neighborHoodText.getId())
						.setNeigborhoodStr(String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(neighborHoodText);

		final EditText cityText = new EditText(EditContactsActivity.this);
		cityText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		cityText.setHint("City");
		cityText.setId(addressId);
		cityText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredCity = String.valueOf(charSequence);
				contactDto.addresses.get(cityText.getId()).setCityStr(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(cityText);

		Spinner countrySpinner = new Spinner(EditContactsActivity.this);
		countrySpinner.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(countrySpinner);

		ArrayAdapter<CharSequence> countrySpinneradapter = ArrayAdapter
				.createFromResource(this, R.array.countries_array,
						android.R.layout.simple_spinner_item);

		countrySpinneradapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(countrySpinneradapter);
		countrySpinner.setId(addressId);
		countrySpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						int index = arg0.getSelectedItemPosition();

						// countries = getResources().getStringArray(
						// R.array.countries_array);
						// selected_country = countries[index];

						contactDto.addresses.get(arg0.getId()).setCountryStr(
								getResources().getStringArray(
										R.array.countries_array)[index]);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		final EditText zipText = new EditText(EditContactsActivity.this);
		zipText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		zipText.setHint("Postal");
		zipText.setId(addressId);
		zipText.setInputType(InputType.TYPE_CLASS_NUMBER);
		zipText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredZip = String.valueOf(charSequence);
				contactDto.addresses.get(zipText.getId()).setZipCodeStr(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(zipText);

		spinner2.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(spinner2);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.addressspinner,
				android.R.layout.simple_spinner_item);

		// ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		// this, R.layout.spinnertext_view, addressSpinnerlist);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
		spinner2.setId(addressId);
		spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();

				// address =
				// getResources().getStringArray(R.array.addressspinner);
				// selected_address_type = address[index];

				contactDto.addresses.get(arg0.getId())
						.setAddressType(
								getResources().getStringArray(
										R.array.addressspinner)[index]);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		addressDto.setId(addressId);
		addressDto.setAddressType(getResources().getStringArray(
				R.array.addressspinner)[spinner2.getSelectedItemPosition()]);

		addressDto.setCityStr("");
		addressDto.setCountryStr("");
		addressDto.setNeigborhoodStr("");
		addressDto.setZipCodeStr("");
		addressDto.setStreetStr("");
		contactDto.addAddresses(addressDto);
		// contactDto.addEmails(emailDto);
		addressId++;

		holderLayout.addView(innerLayout);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		EditContactsActivity.this.overridePendingTransition(0,
				R.anim.slide_out_down);
	}

	public void hideDeleteButton(View view) {
		if (currentDelete != null) {
			if (currentDelete.getVisibility() == View.VISIBLE) {
				Animation scale = new ScaleAnimation(1, 0, 1, 1,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);

				scale.setDuration(300);
				AnimationSet animSet = new AnimationSet(true);
				animSet.setFillEnabled(true);
				animSet.addAnimation(scale);
				currentDelete.startAnimation(animSet);
				currentDelete.setVisibility(View.GONE);

				currentDelete = null;
			} else {

				Animation scale = new ScaleAnimation(0, 1, 1, 1,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);

				scale.setDuration(300);
				AnimationSet animSet = new AnimationSet(true);
				animSet.setFillEnabled(true);
				animSet.addAnimation(scale);
				currentDelete.startAnimation(animSet);
				currentDelete.setVisibility(View.VISIBLE);

			}
		}

	}

	public void fillSpinnerList() {
		numberSpinnerlist = new ArrayList<String>();
		for (int x = 0; x < getResources()
				.getStringArray(R.array.numberspinner).length; x++) {
			numberSpinnerlist.add(getResources().getStringArray(
					R.array.numberspinner)[x]);
		}
		emailSpinnerlist = new ArrayList<String>();

		for (int x = 0; x < getResources().getStringArray(R.array.emailspinner).length; x++) {
			emailSpinnerlist.add(getResources().getStringArray(
					R.array.emailspinner)[x]);
		}

		addressSpinnerlist = new ArrayList<String>();
		for (int x = 0; x < getResources().getStringArray(
				R.array.addressspinner).length; x++) {
			addressSpinnerlist.add(getResources().getStringArray(
					R.array.addressspinner)[x]);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();

		getMenuInflater().inflate(R.menu.editcontact, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_contact_done:
			// do something
			new saveEditedContact()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void fillNewContactDto() {
		if (oldContactDto != null) {

			contactDto.setContactID(oldContactDto.getContactID());
			if (oldContactDto.getProfilepic() != null) {
				contactDto.setProfilepic(oldContactDto.getProfilepic());
				imageView.setImageBitmap(oldContactDto.getProfilepic());
			}
			contactDto.setDisplayName(oldContactDto.getDisplayName());
			edit_first_name.setText(oldContactDto.getFirstName());
			contactDto.setFirstName(oldContactDto.getFirstName());
			edit_last_name.setText(oldContactDto.getLastName());
			contactDto.setLastName(oldContactDto.getLastName());
			edit_company_name.setText(oldContactDto.getCompany());
			contactDto.setCompany(oldContactDto.getCompany());
			for (PhoneNumberDto phoneNumberDto : oldContactDto
					.getPhoneNumbers()) {
				PhoneNumberDto phoneNumberDto2 = new PhoneNumberDto();
				phoneNumberDto2.setNumber(phoneNumberDto.getNumber());
				phoneNumberDto2.setNumberType(phoneNumberDto.getNumberType());
				// contactDto.addPhoneNumbers(phoneNumberDto);
				createLoadedNumberField(phoneNumberDto2);

			}
			if (isFromContactDetailsPage == false) {
				PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
				phoneNumberDto.setNumber(unknownNumberToAdd);
				phoneNumberDto.setNumberType(getResources().getStringArray(
						R.array.numberspinner)[0]);
				createLoadedNumberField(phoneNumberDto);
			}

			for (EmailDto emailDto : oldContactDto.getEmails()) {
				EmailDto emailDto2 = new EmailDto();
				emailDto2.setEmail(emailDto.getEmail());
				emailDto2.setEmailType(emailDto.getEmailType());

				createLoadedEmailField(emailDto2);

			}

			for (AddressDto addressDto : oldContactDto.getAddresses()) {
				AddressDto addressDto2 = new AddressDto();
				addressDto2.setAddressType(addressDto.getAddressType());
				addressDto2.setCityStr(addressDto.getCityStr());
				addressDto2.setCountryStr(addressDto.getCountryStr());
				addressDto2.setNeigborhoodStr(addressDto.getNeigborhoodStr());
				addressDto2.setStreetStr(addressDto.getStreetStr());
				addressDto2.setZipCodeStr(addressDto.getZipCodeStr());
				createLoadedAddressField(addressDto2);
			}

			if (!oldContactDto.getNote().isEmpty()) {
				contactDto.setNote(oldContactDto.getNote());
				createLoadedNoteField(oldContactDto.getNote());
			}

			if (!oldContactDto.getBirthday().isEmpty()) {
				contactDto.setBirthday(oldContactDto.getBirthday());
				createLoadedBirthdayField(oldContactDto.getBirthday());
			}
		}

	}

	public void createLoadedNumberField(PhoneNumberDto phoneNumberDto) {
		System.out.println(phoneNumberDto.getNumber()
				+ " <---Phone number on createLoadedNumberField");
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_number_fld_holder);
		Button myButton = new Button(EditContactsActivity.this);
		final Spinner spinner = new Spinner(EditContactsActivity.this);
		final EditText numberText = new EditText(EditContactsActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				contactDto.phoneNumbers.remove(spinner.getId());
				contactDto.phoneNumbers.add(spinner.getId(), null);
				innerLayout.removeAllViews();
			}
		});

		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 3) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);

					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}
				System.out.println(innerLayout.getChildCount()
						+ " <---- Child Count");
			}
		});

		spinner.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				EditContactsActivity.this, R.array.numberspinner,
				android.R.layout.simple_spinner_item);

		// ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		// this, R.layout.spinnertext_view, numberSpinnerlist);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setId(phoneNumberId);
		int selecteditem = 0;
		for (int x = 0; x < getResources()
				.getStringArray(R.array.numberspinner).length; x++) {
			if (getResources().getStringArray(R.array.numberspinner)[x]
					.equalsIgnoreCase(phoneNumberDto.getNumberType())) {
				selecteditem = x;
				break;
			}

		}

		spinner.setSelection(selecteditem);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();
				System.out.println(arg0.getId() + " <---Spinner id");

				contactDto.phoneNumbers.get(arg0.getId())
						.setNumberType(
								getResources().getStringArray(
										R.array.numberspinner)[index]);

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		numberText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		numberText.setHint("Phone");
		numberText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_CLASS_PHONE);
		numberText.setId(phoneNumberId);
		numberText.setText(phoneNumberDto.getNumber());
		numberText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredNumber = String.valueOf(charSequence);
				System.out.println(numberText.getId() + "<--- Numbertext id");
				contactDto.phoneNumbers.get(numberText.getId()).setNumber(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		// phoneNumberDto.setId(phoneNumberId);
		// phoneNumberDto.setNumberType(getResources().getStringArray(
		// R.array.numberspinner)[spinner.getSelectedItemPosition()]);
		// phoneNumberDto.setNumber("");

		contactDto.addPhoneNumbers(phoneNumberDto);
		phoneNumberId++;
		innerLayout.addView(numberText);

		// innerLayout.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.border_bottom));

		holderLayout.addView(innerLayout);
	}

	public void createLoadedEmailField(EmailDto emailDto) {

		final Spinner spinner = new Spinner(EditContactsActivity.this);
		final EditText emailText = new EditText(EditContactsActivity.this);
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_email_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				contactDto.emails.remove(spinner.getId());
				contactDto.emails.add(spinner.getId(), null);
				innerLayout.removeAllViews();
			}
		});

		Button myButton = new Button(EditContactsActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 3) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}

			}
		});

		spinner.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				EditContactsActivity.this, R.array.emailspinner,
				android.R.layout.simple_spinner_item);
		// ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		// this, R.layout.spinnertext_view, emailSpinnerlist);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setId(emailId);
		int selecteditem = 0;
		for (int x = 0; x < getResources().getStringArray(R.array.emailspinner).length; x++) {
			if (getResources().getStringArray(R.array.emailspinner)[x]
					.equalsIgnoreCase(emailDto.getEmailType())) {
				selecteditem = x;
				break;
			}

		}
		spinner.setSelection(selecteditem);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();

				// emails = getResources().getStringArray(R.array.emailspinner);
				// selected_email_type = emails[index];

				contactDto.emails.get(arg0.getId())
						.setEmailType(
								getResources().getStringArray(
										R.array.emailspinner)[index]);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		emailText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		emailText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		emailText.setHint("Email");
		emailText.setId(emailId);
		emailText.setText(emailDto.getEmail());
		emailText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredEmail = String.valueOf(charSequence);

				// contactDto.emails.get(emailText.getId()).setEmail(
				// String.valueOf(charSequence));
				contactDto.getEmails().get(emailText.getId())
						.setEmail(String.valueOf(charSequence));

			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		// emailDto.setId(emailId);
		// emailDto.setEmailType(getResources().getStringArray(
		// R.array.emailspinner)[spinner.getSelectedItemPosition()]);

		// emailDto.setEmail("");
		contactDto.addEmails(emailDto);
		emailId++;

		innerLayout.addView(emailText);

		holderLayout.addView(innerLayout);
	}

	public void createLoadedAddressField(AddressDto addressDto) {

		final Spinner spinner2 = new Spinner(EditContactsActivity.this);
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_address_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.border_bottom));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		innerLayout.setGravity(Gravity.LEFT);
		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams2.setMargins(0, 0, 0, 5);
		innerLayout.setLayoutParams(layoutParams2);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 68);
		layoutParams.setMargins(0, 0, 0, 10);
		deleteBtn.setLayoutParams(layoutParams);

		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				contactDto.addresses.remove(spinner2.getId());
				contactDto.addresses.add(spinner2.getId(), null);
				innerLayout.removeAllViews();
			}
		});

		Button myButton = new Button(EditContactsActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 7) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}

			}
		});

		final EditText streetText = new EditText(EditContactsActivity.this);
		streetText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		streetText.setHint("Street");
		streetText.setId(addressId);
		streetText.setText(addressDto.getStreetStr());
		streetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredStreet = String.valueOf(charSequence);
				contactDto.addresses.get(streetText.getId()).setStreetStr(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(streetText);

		final EditText neighborHoodText = new EditText(
				EditContactsActivity.this);
		neighborHoodText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		neighborHoodText.setHint("Neighborhood");
		neighborHoodText.setId(addressId);
		neighborHoodText.setText(addressDto.getNeigborhoodStr());
		neighborHoodText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredNeighborhood = String.valueOf(charSequence);
				contactDto.addresses.get(neighborHoodText.getId())
						.setNeigborhoodStr(String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(neighborHoodText);

		final EditText cityText = new EditText(EditContactsActivity.this);
		cityText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		cityText.setHint("City");
		cityText.setId(addressId);
		cityText.setText(addressDto.getCityStr());
		cityText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredCity = String.valueOf(charSequence);
				contactDto.addresses.get(cityText.getId()).setCityStr(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(cityText);

		Spinner countrySpinner = new Spinner(EditContactsActivity.this);
		countrySpinner.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(countrySpinner);

		ArrayAdapter<CharSequence> countrySpinneradapter = ArrayAdapter
				.createFromResource(this, R.array.countries_array,
						android.R.layout.simple_spinner_item);

		countrySpinneradapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(countrySpinneradapter);
		countrySpinner.setId(addressId);
		int countrySelected = 0;
		for (int x = 0; x < getResources().getStringArray(
				R.array.countries_array).length; x++) {
			if (getResources().getStringArray(R.array.countries_array)[x]
					.equalsIgnoreCase(addressDto.getCountryStr())) {
				countrySelected = x;
				break;
			}

		}
		countrySpinner.setSelection(countrySelected);
		countrySpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						int index = arg0.getSelectedItemPosition();

						// countries = getResources().getStringArray(
						// R.array.countries_array);
						// selected_country = countries[index];

						contactDto.addresses.get(arg0.getId()).setCountryStr(
								getResources().getStringArray(
										R.array.countries_array)[index]);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		final EditText zipText = new EditText(EditContactsActivity.this);
		zipText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		zipText.setHint("Postal");
		zipText.setId(addressId);
		zipText.setText(addressDto.getZipCodeStr());
		zipText.setInputType(InputType.TYPE_CLASS_NUMBER);
		zipText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredZip = String.valueOf(charSequence);
				contactDto.addresses.get(zipText.getId()).setZipCodeStr(
						String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		innerLayout.addView(zipText);

		spinner2.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.addView(spinner2);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.addressspinner,
				android.R.layout.simple_spinner_item);

		// ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
		// this, R.layout.spinnertext_view, addressSpinnerlist);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
		spinner2.setId(addressId);
		int emailtype = 0;
		for (int x = 0; x < getResources().getStringArray(
				R.array.addressspinner).length; x++) {
			if (getResources().getStringArray(R.array.addressspinner)[x]
					.equalsIgnoreCase(addressDto.getAddressType())) {
				emailtype = x;
				break;
			}

		}
		spinner2.setSelection(emailtype);
		spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();

				// address =
				// getResources().getStringArray(R.array.addressspinner);
				// selected_address_type = address[index];

				contactDto.addresses.get(arg0.getId())
						.setAddressType(
								getResources().getStringArray(
										R.array.addressspinner)[index]);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// addressDto.setId(addressId);
		// addressDto.setAddressType(getResources().getStringArray(
		// R.array.addressspinner)[spinner2.getSelectedItemPosition()]);
		//
		// addressDto.setCityStr("");
		// addressDto.setCountryStr("");
		// addressDto.setNeigborhoodStr("");
		// addressDto.setZipCodeStr("");
		// addressDto.setStreetStr("");
		contactDto.addAddresses(addressDto);
		// contactDto.addEmails(emailDto);
		addressId++;

		holderLayout.addView(innerLayout);
	}

	public void createLoadedNoteField(String note) {

		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_note_fld_holder);
		Button myButton = new Button(EditContactsActivity.this);

		final EditText noteText = new EditText(EditContactsActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				contactDto.setNote("");
				hasNote = false;
				innerLayout.removeAllViews();
			}
		});

		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (innerLayout.getChildCount() == 2) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}
			}
		});

		noteText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		noteText.setHint("Note");
		noteText.setText(note);
		noteText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_CLASS_TEXT);
		noteText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredNumber = String.valueOf(charSequence);
				System.out.println(noteText.getId() + "<--- Numbertext id");
				contactDto.setNote(String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		innerLayout.addView(noteText);

		// innerLayout.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.border_bottom));
		hasNote = true;
		holderLayout.addView(innerLayout);
	}

	public void createLoadedBirthdayField(String birthday) {
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.edit_added_bDay_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				EditContactsActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(EditContactsActivity.this);
		deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		deleteBtn.setText("Delete");
		deleteBtn.setTextColor(Color.WHITE);
		deleteBtn.setGravity(Gravity.CENTER);
		deleteBtn.setBackgroundColor(Color.RED);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hasBirthday = false;
				contactDto.setBirthday("");
				innerLayout.removeAllViews();
			}
		});

		Button myButton = new Button(EditContactsActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(EditContactsActivity.this.getResources()
				.getDrawable(R.drawable.delete_item_img));
		innerLayout.addView(myButton);

		myButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (innerLayout.getChildCount() == 2) {
					innerLayout.addView(deleteBtn);

					Animation scale = new ScaleAnimation(0, 1, 1, 1,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scale.setDuration(300);
					AnimationSet animSet = new AnimationSet(true);
					animSet.setFillEnabled(true);
					animSet.addAnimation(scale);
					deleteBtn.startAnimation(animSet);
					currentDelete = deleteBtn;
				} else {
					if (deleteBtn.getVisibility() == View.VISIBLE) {

						Animation scale = new ScaleAnimation(1, 0, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.GONE);
						currentDelete = null;
					} else {
						Animation scale = new ScaleAnimation(0, 1, 1, 1,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);

						scale.setDuration(300);
						AnimationSet animSet = new AnimationSet(true);
						animSet.setFillEnabled(true);
						animSet.addAnimation(scale);
						deleteBtn.startAnimation(animSet);
						deleteBtn.setVisibility(View.VISIBLE);
						currentDelete = deleteBtn;
					}
				}

			}
		});

		final EditText bdayText = new EditText(EditContactsActivity.this);
		bdayText.setKeyListener(null);
		bdayText.setLayoutParams(new LinearLayout.LayoutParams(250,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		bdayText.setHint("MM - DD - YYYY");
		bdayText.setText(birthday);
		bdayText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {
				// enteredBday = String.valueOf(charSequence);
				contactDto.setBirthday(String.valueOf(charSequence));
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		bdayText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Calendar mcurrentDate = Calendar.getInstance();
				int mYear = mcurrentDate.get(Calendar.YEAR);
				int mMonth = mcurrentDate.get(Calendar.MONTH);
				int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog mDatePicker = new DatePickerDialog(
						EditContactsActivity.this,
						new DatePickerDialog.OnDateSetListener() {
							public void onDateSet(DatePicker datepicker,
									int selectedyear, int selectedmonth,
									int selectedday) {
								// TODO Auto-generated method stub

								String year1 = String.valueOf(selectedyear);
								String month1 = String
										.valueOf(selectedmonth + 1);
								String day1 = String.valueOf(selectedday);
								// SimpleDateFormat sdf = new
								// SimpleDateFormat("dd-MM-yyyy HH:mm"); // Set
								// your date format
								// String formattedDate =
								// sdf.format(callDateArray.get(i));
								bdayText.setText(month1 + "-" + day1 + "-"
										+ year1);
							}
						}, mYear, mMonth, mDay);
				mDatePicker.setTitle("Select date");
				mDatePicker.show();
			}
		});

		innerLayout.addView(bdayText);

		hasBirthday = true;
		holderLayout.addView(innerLayout);
	}

	class saveEditedContact extends AsyncTask<String, String, String> {
		boolean response = true;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			contactDto.setDisplayName(edit_first_name.getText().toString()
					+ " " + edit_last_name.getText().toString());
			contactDto.setFirstName(edit_first_name.getText().toString());
			contactDto.setLastName(edit_last_name.getText().toString());
			contactDto.setCompany(edit_company_name.getText().toString());
			progressDialog = ProgressDialog.show(EditContactsActivity.this,
					"Please wait...", "Loading Contact Details...");
			progressDialog.setCancelable(false);
			contactDto = quickContactHelper.removeNullValues(contactDto);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {

				quickContactHelper.updateContact(contactDto, oldContactDto);
			} catch (Exception e) {
				e.printStackTrace();
				response = false;
			}

			// for (EmailDto emailDto2 : contactDto.getEmails()) {
			// System.out.println(emailDto2.getEmail() + " contactdto email");
			// System.out.println(emailDto2.getEmailType()
			// + " contactdto emailType");
			// }
			// System.out
			// .println("==============================================");
			// for (EmailDto emailDto1 : oldContactDto.getEmails()) {
			// System.out.println(emailDto1.getEmail()
			// + " oldContactDto email");
			// System.out.println(emailDto1.getEmailType()
			// + " oldContactDto emailType");
			// }

			// for (AddressDto addressDto : oldContactDto.getAddresses()) {
			// System.out.println(addressDto.getStreetStr()
			// + " old contactDto Street");
			// }
			//
			// for (AddressDto addressDto2 : contactDto.getAddresses()) {
			// System.out.println(addressDto2.getStreetStr()
			// + " new contactDto Street");
			// }

			// System.out.println(contactDto.getDisplayName()
			// + " ContactDto displayname");
			// System.out.println(oldContactDto.getDisplayName()
			// + " oldContactDto displayname");
			//
			// System.out.println(contactDto.getFirstName()
			// + " ContactDto firstname");
			// System.out.println(oldContactDto.getFirstName()
			// + " oldContactDto firstname");
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			if (response == true) {

				Toast.makeText(EditContactsActivity.this, "Changes Saved",
						Toast.LENGTH_LONG).show();

				if (isFromContactDetailsPage == true) {
					ContactDetailActivity.contactDto = contactDto;
				}

				onBackPressed();
				// for (EmailDto emailDto1 : emails) {
				// System.out.println(emailDto1.getEmail()
				// + " oldContactDto email");
				// System.out.println(emailDto1.getEmail()
				// + " oldContactDto emailType");
				// }

			} else {
				alertDialog.alertMessage("Error", "Error Updating contact",
						EditContactsActivity.this);
			}
			
			
			super.onPostExecute(result);
		}

	}

}
