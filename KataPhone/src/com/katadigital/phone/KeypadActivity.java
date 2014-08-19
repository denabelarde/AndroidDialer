package com.katadigital.phone;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.katadigital.phone.helpers.QuickContactHelper;

public class KeypadActivity extends Fragment implements View.OnClickListener {

	View rootView;
	LinearLayout one, two, three, four, five, six, seven, eight, nine, zero,
			asterisk, hash;
	Button call;
	public EditText keyPadTextView;
	Button addContacts;
	int count = 0;
	boolean clearIsPressed = false;
	Dialog selectModelDialog;
	ImageView clearIV;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 ((MainActivity) getActivity()).currentTab = 3;
		 ((MainActivity) getActivity()).currentFragment=this;
		// getActivity().invalidateOptionsMenu();
		// String model = Build.MODEL;
		// System.out.println("Keypad OncreateView");
		// if (model.equals("Venus 3")) {
		// rootView = inflater.inflate(R.layout.v3_keypad, container, false);
		// } else {
		rootView = inflater.inflate(R.layout.keypad, container, false);
		// }

		keyPadTextView = (EditText) rootView.findViewById(R.id.enteredDigits);
		keyPadTextView.setText("");
		keyPadTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				String textInBox = keyPadTextView.getText().toString();
				// addContacts.setVisibility(View.INVISIBLE);
				// if (textInBox.length() > 0) {
				// System.out.println("KeypadTextView ontextchanged true");
				// addContacts.setVisibility(View.VISIBLE);
				// } else {
				// addContacts.setVisibility(View.INVISIBLE);
				// }

				// while
				// (keyPadTextView.measureText(MyTextString)>mTextView.getWidth()
				// ) //maybe adjust for padding/margins etc
				// {
				// keyPadTextView.setTextSize(... currentsize - 1);
				// }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		keyPadTextView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int inType = keyPadTextView.getInputType(); // backup the input
															// type
				keyPadTextView.setInputType(InputType.TYPE_NULL); // disable
																	// soft
																	// input
				keyPadTextView.onTouchEvent(event); // call native handler
				keyPadTextView.setInputType(inType); // restore input type
				return true; // consume touch even
			}
		});

		addContacts = (Button) rootView.findViewById(R.id.keypad_add_contact);
		addContacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				openDialog();
			}
		});

		clearIV = (ImageView) rootView.findViewById(R.id.clear_image_view);

		clearIV.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				mAutoDecrement = true;
				repeatUpdateHandler.post(new RptUpdater());
				return false;
			}
		});
		clearIV.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					String textInBox = keyPadTextView.getText().toString();
					if (textInBox.length() > 0) {
						clearIV.setPressed(true);
						String newText = textInBox.substring(0,
								textInBox.length() - 1);
						keyPadTextView.setText(newText);
					}
					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					clearIV.setPressed(false);
					mAutoDecrement = false;
					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
					// clearIV.setPressed(false);
					mAutoDecrement = false;
					return false;
				} else {
					return false;
				}
			}
		});

		one = (LinearLayout) rootView.findViewById(R.id.btn_one);
		two = (LinearLayout) rootView.findViewById(R.id.btn_two);
		three = (LinearLayout) rootView.findViewById(R.id.btn_three);
		four = (LinearLayout) rootView.findViewById(R.id.btn_four);
		five = (LinearLayout) rootView.findViewById(R.id.btn_five);
		six = (LinearLayout) rootView.findViewById(R.id.btn_six);
		seven = (LinearLayout) rootView.findViewById(R.id.btn_seven);
		eight = (LinearLayout) rootView.findViewById(R.id.btn_eight);
		nine = (LinearLayout) rootView.findViewById(R.id.btn_nine);
		zero = (LinearLayout) rootView.findViewById(R.id.btn_zero);
		asterisk = (LinearLayout) rootView.findViewById(R.id.btn_asterisk);
		hash = (LinearLayout) rootView.findViewById(R.id.btn_hash);
		call = (Button) rootView.findViewById(R.id.call_btn);

		one.setOnClickListener(this);
		two.setOnClickListener(this);
		three.setOnClickListener(this);
		four.setOnClickListener(this);
		five.setOnClickListener(this);
		six.setOnClickListener(this);
		seven.setOnClickListener(this);
		eight.setOnClickListener(this);
		nine.setOnClickListener(this);
		call.setOnClickListener(this);

		zero.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				String textInBox = keyPadTextView.getText().toString();
				if (textInBox.length() > 0) {
					String newText = textInBox.substring(0,
							textInBox.length() - 1);
					keyPadTextView.setText(newText);
				}
				keyPadTextView.setVisibility(View.VISIBLE);
				clearIV.setVisibility(View.VISIBLE);
				addContacts.setVisibility(View.VISIBLE);
				keyPadTextView.setText(keyPadTextView.getText() + "+");
				return false;
			}
		});
		zero.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					keyPadTextView.setVisibility(View.VISIBLE);
					clearIV.setVisibility(View.VISIBLE);
					addContacts.setVisibility(View.VISIBLE);
					keyPadTextView.setText(keyPadTextView.getText() + "0");

					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
					return false;
				} else {
					return false;
				}
			}
		});

		asterisk.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				String textInBox = keyPadTextView.getText().toString();
				if (textInBox.length() > 0) {
					String newText = textInBox.substring(0,
							textInBox.length() - 1);
					keyPadTextView.setText(newText);
				}
				keyPadTextView.setVisibility(View.VISIBLE);
				clearIV.setVisibility(View.VISIBLE);
				addContacts.setVisibility(View.VISIBLE);
				keyPadTextView.setText(keyPadTextView.getText() + ",");
				return false;
			}
		});
		asterisk.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					keyPadTextView.setVisibility(View.VISIBLE);
					clearIV.setVisibility(View.VISIBLE);
					addContacts.setVisibility(View.VISIBLE);
					keyPadTextView.setText(keyPadTextView.getText() + "*");
					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

					return false;
				} else {
					return false;
				}
			}
		});

		hash.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				String textInBox = keyPadTextView.getText().toString();
				if (textInBox.length() > 0) {
					String newText = textInBox.substring(0,
							textInBox.length() - 1);
					keyPadTextView.setText(newText);
				}
				keyPadTextView.setVisibility(View.VISIBLE);
				clearIV.setVisibility(View.VISIBLE);
				addContacts.setVisibility(View.VISIBLE);
				keyPadTextView.setText(keyPadTextView.getText() + ";");
				return false;
			}
		});
		hash.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					keyPadTextView.setVisibility(View.VISIBLE);
					clearIV.setVisibility(View.VISIBLE);
					addContacts.setVisibility(View.VISIBLE);
					keyPadTextView.setText(keyPadTextView.getText() + "#");

					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					return false;
				} else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
					return false;
				} else {
					return false;
				}
			}
		});
		keyPadTextView.setVisibility(View.INVISIBLE);
		clearIV.setVisibility(View.INVISIBLE);
		addContacts.setVisibility(View.INVISIBLE);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		System.out.println("here at onclick");
		keyPadTextView.setVisibility(View.VISIBLE);
		clearIV.setVisibility(View.VISIBLE);
		addContacts.setVisibility(View.VISIBLE);
		switch (view.getId()) {
		case R.id.btn_one:
			keyPadTextView.setText(keyPadTextView.getText() + "1");

			break;
		case R.id.btn_two:

			keyPadTextView.setText(keyPadTextView.getText() + "2");
			break;
		case R.id.btn_three:

			keyPadTextView.setText(keyPadTextView.getText() + "3");
			break;
		case R.id.btn_four:

			keyPadTextView.setText(keyPadTextView.getText() + "4");
			break;
		case R.id.btn_five:

			keyPadTextView.setText(keyPadTextView.getText() + "5");
			break;
		case R.id.btn_six:

			keyPadTextView.setText(keyPadTextView.getText() + "6");
			break;
		case R.id.btn_seven:

			keyPadTextView.setText(keyPadTextView.getText() + "7");
			break;
		case R.id.btn_eight:

			keyPadTextView.setText(keyPadTextView.getText() + "8");
			break;
		case R.id.btn_nine:

			keyPadTextView.setText(keyPadTextView.getText() + "9");
			break;
		case R.id.call_btn:

			// Intent callIntent = new Intent(Intent.ACTION_CALL);
			// callIntent.setData(Uri.parse(String.valueOf("tel:"
			// + keyPadTextView.getText())));
			// startActivity(callIntent);
			startActivity(QuickContactHelper.callfromDefaultDialer(
					getActivity(), keyPadTextView.getText().toString()));
			// ;

			break;
		}
	}

	// public static Intent callfromDefaultDialer(Context ctxt, String no) {
	//
	// List<Intent> targetedShareIntents = new ArrayList<Intent>();
	//
	// Intent i = new Intent();
	// i.setAction(Intent.ACTION_CALL);
	// //i.addCategory(Intent.ACTION_DEFAULT);
	// i.setData(Uri.parse("tel:" + no));
	// PackageManager pm = ctxt.getPackageManager();
	// List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
	// for (ResolveInfo info : list) {
	// String pkgnam = info.activityInfo.packageName;
	// Intent targetedShareIntent = new Intent(Intent.ACTION_CALL);
	// if (pkgnam.toLowerCase().equals("com.android.phone")) {
	// targetedShareIntent.setData(Uri.parse("tel:" + no));
	// targetedShareIntent.setClassName(pkgnam, info.activityInfo.name);
	// targetedShareIntents.add(targetedShareIntent);
	// //return targetedShareIntent;
	// //i.setClassName(pkgnam, info.activityInfo.name);
	// //return i;
	// }
	// }
	//
	// Intent chooserIntent =
	// Intent.createChooser(targetedShareIntents.remove(0),
	// "Select app to Call");
	// chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
	// targetedShareIntents.toArray(new
	// Parcelable[targetedShareIntents.size()]));
	//
	// return chooserIntent;
	// }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		// getActivity().getActionBar().hide();

		super.onActivityCreated(savedInstanceState);
		System.out.println(keyPadTextView.getText() + " <---Keypad Text");
		keyPadTextView.setText("");
	}

	private Handler repeatUpdateHandler = new Handler();

	private boolean mAutoDecrement = false;
	// speed of deletion
	int REP_DELAY = 30;
	int mValue = 0;

	class RptUpdater implements Runnable {
		public void run() {
			if (mAutoDecrement) {
				decrement();
				repeatUpdateHandler.postDelayed(new RptUpdater(), REP_DELAY);
			}
			// else if( mAutoDecrement ){
			// decrement();
			// repeatUpdateHandler.postDelayed( new RptUpdater(), REP_DELAY );
			// }
		}
	}

	public void decrement() {
		// mValue--;
		// keyPadTextView.setText( ""+mValue );

		String textInBox = keyPadTextView.getText().toString();
		if (textInBox.length() > 0) {
			String newText = textInBox.substring(0, textInBox.length() - 1);
			keyPadTextView.setText(newText);
		} else {
			mAutoDecrement = false;
		}

		if (keyPadTextView.getText().toString().isEmpty()) {
			keyPadTextView.setVisibility(View.INVISIBLE);
			clearIV.setVisibility(View.INVISIBLE);
			addContacts.setVisibility(View.INVISIBLE);
		}
	}

	public void openDialog() {
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getLayoutInflater();

		View customView = inflater.inflate(R.layout.keypad_addcontact_dialog,
				null);

		TextView textBtn1, textBtn2, textBtn3;
		textBtn1 = (TextView) customView.findViewById(R.id.keypad_dialogbtn1);
		textBtn2 = (TextView) customView.findViewById(R.id.keypad_dialogbtn2);
		textBtn3 = (TextView) customView.findViewById(R.id.keypad_dialogbtn3);

		textBtn1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String text = String.valueOf(keyPadTextView.getText());
				Intent intent = new Intent(getActivity(),
						AddContactActivity.class);
				Bundle extras = new Bundle();

				if (text != "") {
					extras.putString("preloaded_number", text);
				}
				selectModelDialog.dismiss();
				intent.putExtras(extras);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_up,
						R.anim.slide_in_up_exit);
			}
		});

		textBtn2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String text = String.valueOf(keyPadTextView.getText());
				Intent intent = new Intent(getActivity(),
						AddNumberToContact.class);
				selectModelDialog.dismiss();
				intent.putExtra("number_to_add", text);
				startActivity(intent);

				getActivity().overridePendingTransition(R.anim.slide_in_up,
						R.anim.slide_in_up_exit);

			}
		});

		textBtn3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectModelDialog.dismiss();

			}
		});

		// Build the dialog
		selectModelDialog = new Dialog(getActivity(), R.style.DialogSlideAnim);
		selectModelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		selectModelDialog.setContentView(customView);
		selectModelDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		WindowManager.LayoutParams wmlp = selectModelDialog.getWindow()
				.getAttributes();

		wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

		selectModelDialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		// LayoutParams params = getActivity().getWindow().getAttributes();
		// params.height = LayoutParams.FILL_PARENT;
		// getActivity().getWindow().setAttributes(
		// (android.view.WindowManager.LayoutParams) params);

		// selectModelDialog.getWindow().setSoftInputMode(
		// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		selectModelDialog.setTitle("Actions");

		selectModelDialog.show();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		System.out.println(keyPadTextView.getText());
		super.onStart();
	}
	


}
