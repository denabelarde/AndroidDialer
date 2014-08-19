package com.katadigital.phone;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
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
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
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
import com.katadigital.phone.helpers.QuickContactHelper;

public class AddContactActivity extends Activity {

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
	ContactDto contactDto = new ContactDto();
	int phoneNumberId = 0;
	int emailId = 0;
	int addressId = 0;

	boolean hasBirthday = false;
	boolean hasNote = false;
	QuickContactHelper quickContactHelper;

	public static void launch(Context c) {
		Intent intent = new Intent(c, AddContactActivity.class);
		c.startActivity(intent);
	}

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
		setContentView(R.layout.add_contacts);
		ActionBar ab = getActionBar();

		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setDisplayShowHomeEnabled(true);
		// ab.setIcon(R.drawable.dummy);
		ab.setCustomView(R.layout.addcontacts_actionbar);
		ab.setDisplayHomeAsUpEnabled(true);
		CropperActivity.selectedImage = null;
		// fillSpinnerList();
		contactDto = new ContactDto();
		imageView = (ImageView) this.findViewById(R.id.add_photo_image);
		quickContactHelper = new QuickContactHelper(this);
		Intent intent;
		if (getIntent().getExtras() != null) {
			intent = getIntent();
			Bundle bundle = intent.getExtras();
			final String numberFromKeypad = bundle
					.getString("preloaded_number");
			if (numberFromKeypad != null) {
				// mobileNumber.setText(numberFromKeypad);
				PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
				phoneNumberDto.setNumber(numberFromKeypad);
				if (getResources().getStringArray(R.array.numberspinner)[0] != null) {
					phoneNumberDto.setNumberType(getResources().getStringArray(
							R.array.numberspinner)[0]);
				}
				createLoadedNumberField(phoneNumberDto);

			}
		}

		final View addNumberField = findViewById(R.id.add_phone_view);
		// addNumberField.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// createNumberField();
		// }
		// });
		addNumberField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createNumberField();
			}
		});

		final View addEmailField = findViewById(R.id.add_email_view);
		addEmailField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createEmailField();
			}
		});

		final View addBdayField = findViewById(R.id.add_birthday_view);
		addBdayField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (hasBirthday == false) {
					createBirthdayField();
				}

			}
		});

		final View addAddressField = findViewById(R.id.add_address_view);
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
					Intent intent = new Intent(AddContactActivity.this,
							CropperActivity.class);
					CropperActivity.clearBitmaps();
					startActivityForResult(intent, 1);
				} else {
					Intent intent = new Intent(AddContactActivity.this,
							CropperActivity.class);
					startActivityForResult(intent, 1);
				}

			}

		});

		final View addNoteField = findViewById(R.id.add_note_view);
		addNoteField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (hasNote == false) {
					createNoteField();
				}

			}
		});
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

		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.added_number_fld_holder);
		Button myButton = new Button(AddContactActivity.this);
		final Spinner spinner = new Spinner(AddContactActivity.this);
		final EditText numberText = new EditText(AddContactActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				AddContactActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(AddContactActivity.this);
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
		myButton.setBackgroundDrawable(AddContactActivity.this.getResources()
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
				AddContactActivity.this, R.array.numberspinner,
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

		final Spinner spinner = new Spinner(AddContactActivity.this);
		final EditText emailText = new EditText(AddContactActivity.this);
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.added_email_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				AddContactActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(AddContactActivity.this);
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

		Button myButton = new Button(AddContactActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(AddContactActivity.this.getResources()
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
				AddContactActivity.this, R.array.emailspinner,
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

		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.added_note_fld_holder);
		Button myButton = new Button(AddContactActivity.this);

		final EditText noteText = new EditText(AddContactActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				AddContactActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(AddContactActivity.this);
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
		myButton.setBackgroundDrawable(AddContactActivity.this.getResources()
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
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.added_bDay_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				AddContactActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(AddContactActivity.this);
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

		Button myButton = new Button(AddContactActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(AddContactActivity.this.getResources()
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

		final EditText bdayText = new EditText(AddContactActivity.this);
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
						AddContactActivity.this,
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
		final Spinner spinner2 = new Spinner(AddContactActivity.this);
		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.added_address_fld_holder);

		final LinearLayout innerLayout = new LinearLayout(
				AddContactActivity.this);
		innerLayout.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.border_bottom));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		innerLayout.setGravity(Gravity.LEFT);
		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams2.setMargins(0, 0, 0, 5);
		innerLayout.setLayoutParams(layoutParams2);

		final TextView deleteBtn = new TextView(AddContactActivity.this);
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

		Button myButton = new Button(AddContactActivity.this);
		myButton.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
		myButton.setBackgroundDrawable(AddContactActivity.this.getResources()
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

		final EditText streetText = new EditText(AddContactActivity.this);
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

		final EditText neighborHoodText = new EditText(AddContactActivity.this);
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

		final EditText cityText = new EditText(AddContactActivity.this);
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

		Spinner countrySpinner = new Spinner(AddContactActivity.this);
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

		final EditText zipText = new EditText(AddContactActivity.this);
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
		AddContactActivity.this.overridePendingTransition(0,
				R.anim.slide_out_down);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.addcontact, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.addcontact_done:
			// do something
			new addContactAsync()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			// addContact();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class addContactAsync extends AsyncTask<String, String, String> {
		final EditText fNameText = (EditText) findViewById(R.id.first_name);
		final EditText lNameText = (EditText) findViewById(R.id.last_name);
		final EditText companyName = (EditText) findViewById(R.id.company_name);

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog.show(AddContactActivity.this,
					"Please wait...", "Saving contact...");
			progressDialog.setCancelable(true);
			success = true;
			returnmessage = "";
			contactDto = quickContactHelper.removeNullValues(contactDto);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			contactDto.setDisplayName(String.valueOf(fNameText.getText()) + " "
					+ String.valueOf(lNameText.getText()));
			contactDto.setFirstName(String.valueOf(fNameText.getText()));
			contactDto.setLastName(String.valueOf(lNameText.getText()));
			contactDto.setCompany(String.valueOf(companyName.getText()));

			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.RawContacts.CONTENT_URI)
					.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
					.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
					.build());

			// ------------------------------------------------------ Names
			if (!contactDto.getDisplayName().isEmpty()) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
								contactDto.getDisplayName()).build());
			}

			// ------------------------------------------------------ Names
			if (!contactDto.getFirstName().isEmpty()) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
								contactDto.getFirstName()).build());
			}

			// ------------------------------------------------------ Names
			if (!contactDto.getLastName().isEmpty()) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
								contactDto.getLastName()).build());
			}

			// ------------------------------------------------------ PHOTO
			if (contactDto.getProfilepic() != null) {
				// Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable())
				// .getBitmap();
				System.out.println("Pasok sa Photos Creation");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				contactDto.getProfilepic().compress(Bitmap.CompressFormat.PNG,
						75, stream);
				byte[] image = stream.toByteArray();

				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Photo.PHOTO,
								image).build());

			}

			// ------------------------------------------------------ Email

			if (contactDto.emails.size() > 0) {
				for (int x = 0; x < contactDto.getEmails().size(); x++) {

					if (contactDto.getEmails().get(x) != null) {
						EmailDto emailDto = contactDto.getEmails().get(x);
						if (!emailDto.getEmail().isEmpty()) {
							if (emailDto.getEmailType()
									.equalsIgnoreCase("Home")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Email.DATA,
												emailDto.getEmail())
										.withValue(
												ContactsContract.CommonDataKinds.Email.TYPE,
												ContactsContract.CommonDataKinds.Email.TYPE_HOME)
										.build());
							} else if (emailDto.getEmailType()
									.equalsIgnoreCase("Work")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Email.DATA,
												emailDto.getEmail())
										.withValue(
												ContactsContract.CommonDataKinds.Email.TYPE,
												ContactsContract.CommonDataKinds.Email.TYPE_WORK)
										.build());
							} else if (emailDto.getEmailType()
									.equalsIgnoreCase("Other")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Email.DATA,
												emailDto.getEmail())
										.withValue(
												ContactsContract.CommonDataKinds.Email.TYPE,
												ContactsContract.CommonDataKinds.Email.TYPE_OTHER)
										.build());
							}
						}
					}

				}
			}

			// ------------------------------------------------------ Mobile
			// Number
			if (contactDto.phoneNumbers.size() > 0) {
				for (int x = 0; x < contactDto.getPhoneNumbers().size(); x++) {

					if (contactDto.getPhoneNumbers().get(x) != null) {
						PhoneNumberDto phoneNumberDto = contactDto
								.getPhoneNumbers().get(x);
						if (!phoneNumberDto.getNumber().isEmpty()) {
							if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Mobile")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
										.build());
							} else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Home")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
										.build());
							} else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Work")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
										.build());
							} else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Main")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
										.build());
							} else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Work Fax")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK)
										.build());
							}

							else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Home Fax")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME)
										.build());
							} else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Pager")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_PAGER)
										.build());
							} else if (phoneNumberDto.getNumberType()
									.equalsIgnoreCase("Other")) {
								ops.add(ContentProviderOperation
										.newInsert(
												ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(
												ContactsContract.Data.RAW_CONTACT_ID,
												0)
										.withValue(
												ContactsContract.Data.MIMETYPE,
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(
												ContactsContract.CommonDataKinds.Phone.NUMBER,
												phoneNumberDto.getNumber())
										.withValue(
												ContactsContract.CommonDataKinds.Phone.TYPE,
												ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
										.build());
							}
						}
					}

				}
			}

			// ------------------------------------------------------
			// Birthday
			if (!contactDto.getBirthday().isEmpty()) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Event.START_DATE,
								contactDto.getBirthday())
						.withValue(
								ContactsContract.CommonDataKinds.Event.TYPE,
								ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
						.build());
			}

			// ------------------------------------------------------
			// Company
			if (!contactDto.getCompany().isEmpty()) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.Organization.COMPANY,
								contactDto.getCompany())
						.withValue(
								ContactsContract.CommonDataKinds.Organization.TYPE,
								ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
						.build());
			}

			// ------------------------------------------------------
			// Note
			if (!contactDto.getNote().isEmpty()) {
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Note.NOTE,
								contactDto.getNote())

						.build());
			}

			// ------------------------------------------------------
			// Address

			// if (contactDto.phoneNumbers.size() > 0) {
			// for (int x = 0; x < contactDto.getPhoneNumbers().size(); x++) {
			//
			// if (contactDto.getPhoneNumbers().get(x) != null) {
			// PhoneNumberDto phoneNumberDto = contactDto
			// .getPhoneNumbers().get(x);
			if (contactDto.addresses.size() > 0) {
				for (int x = 0; x < contactDto.getAddresses().size(); x++) {
					if (contactDto.getAddresses().get(x) != null) {
						AddressDto addressDto = contactDto.getAddresses()
								.get(x);
						if (addressDto.getAddressType()
								.equalsIgnoreCase("Home")) {
							// if (city != null) {
							ops.add(ContentProviderOperation
									.newInsert(
											ContactsContract.Data.CONTENT_URI)
									.withValueBackReference(
											ContactsContract.Data.RAW_CONTACT_ID,
											0)
									.withValue(
											ContactsContract.Data.MIMETYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.STREET,
											addressDto.getStreetStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
											addressDto.getNeigborhoodStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.CITY,
											addressDto.getCityStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
											addressDto.getCountryStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
											addressDto.getZipCodeStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
									.build());
							// }

						} else if (addressDto.getAddressType()
								.equalsIgnoreCase("Work")) {
							// if (streetStr != null) {
							ops.add(ContentProviderOperation
									.newInsert(
											ContactsContract.Data.CONTENT_URI)
									.withValueBackReference(
											ContactsContract.Data.RAW_CONTACT_ID,
											0)
									.withValue(
											ContactsContract.Data.MIMETYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.STREET,
											addressDto.getStreetStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
											addressDto.getNeigborhoodStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.CITY,
											addressDto.getCityStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
											addressDto.getCountryStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
											addressDto.getZipCodeStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
									.build());
							// }

						} else if (addressDto.getAddressType()
								.equalsIgnoreCase("Other")) {
							// if (streetStr != null) {
							ops.add(ContentProviderOperation
									.newInsert(
											ContactsContract.Data.CONTENT_URI)
									.withValueBackReference(
											ContactsContract.Data.RAW_CONTACT_ID,
											0)
									.withValue(
											ContactsContract.Data.MIMETYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.STREET,
											addressDto.getStreetStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
											addressDto.getNeigborhoodStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.CITY,
											addressDto.getCityStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
											addressDto.getCountryStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER)
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
											addressDto.getZipCodeStr())
									.withValue(
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
											ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER)
									.build());
							// }

						}
					}

				}
			}

			// Asking the Contact provider to create a new contact
			try {

				ContentProviderResult[] results = getContentResolver()
						.applyBatch(ContactsContract.AUTHORITY, ops);
				int contactId = Integer.parseInt(results[0].uri
						.getLastPathSegment());
				// getContentResolver()
				// .applyBatch(ContactsContract.AUTHORITY, ops);
				contactDto.setContactID(contactId + "");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(fNameText.getWindowToken(), 0);

			} catch (Exception e) {
				e.printStackTrace();
				returnmessage = e.getMessage();
				success = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			if (success) {
				fNameText.setText("");
				lNameText.setText("");
				companyName.setText("");
				imageView.setImageResource(R.drawable.profile_pic_holder);

				onBackPressed();

				ContactDetailActivity.contactDto = contactDto;
				Intent intent = new Intent(AddContactActivity.this,
						ContactDetailActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_up,
						R.anim.slide_in_up_exit);

				Toast.makeText(AddContactActivity.this, "Contact Added",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(AddContactActivity.this,
						"Exception: " + returnmessage, Toast.LENGTH_SHORT)
						.show();
			}

			progressDialog.dismiss();
			super.onPostExecute(result);
		}

	}

	public void addContact() {

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

	public void createLoadedNumberField(PhoneNumberDto phoneNumberDto) {

		LinearLayout holderLayout = (LinearLayout) findViewById(R.id.added_number_fld_holder);
		Button myButton = new Button(AddContactActivity.this);
		final Spinner spinner = new Spinner(AddContactActivity.this);
		final EditText numberText = new EditText(AddContactActivity.this);

		final LinearLayout innerLayout = new LinearLayout(
				AddContactActivity.this);
		innerLayout.setOrientation(LinearLayout.HORIZONTAL);
		innerLayout.setGravity(Gravity.CENTER);

		final TextView deleteBtn = new TextView(AddContactActivity.this);
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
		myButton.setBackgroundDrawable(AddContactActivity.this.getResources()
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
				AddContactActivity.this, R.array.numberspinner,
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

		phoneNumberDto.setId(phoneNumberId);
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

}