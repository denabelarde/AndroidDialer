package com.katadigital.phone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;
import com.katadigital.phone.helpers.AlertDialogHelper;
import com.katadigital.phone.helpers.MemoryManager;

public class CropperActivity extends Activity {

	CropImageView cropImageView;
	// Static final constants
	private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
	private static final int ROTATE_NINETY_DEGREES = 90;
	private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
	private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
	private static final int ON_TOUCH = 1;
	private final int PICTURE_TAKEN_FROM_CAMERA = 1;
	private final int PICTURE_TAKEN_FROM_GALLERY = 2;
	// Instance variables
	private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
	private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;

	static Bitmap croppedImage;
	static Bitmap selectedImage;
	Dialog selectPhotoDialog;

	String imagePath;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			java.util.Locale.getDefault());
	String currentDateandTime;
	AlertDialogHelper alertDialog = new AlertDialogHelper();
	MemoryManager memoryManager = new MemoryManager();
	File destination;
	File imgdir;

	String imagefilename;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_cropper);

		ActionBar ab = getActionBar();
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setDisplayShowHomeEnabled(true);
		// ab.setIcon(R.drawable.dummy);

		ab.setDisplayHomeAsUpEnabled(true);

		cropImageView = (CropImageView) findViewById(R.id.CropImageView);
		cropImageView.setFixedAspectRatio(true);
		cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES,
				DEFAULT_ASPECT_RATIO_VALUES);
		cropImageView.setGuidelines(1);
		if (selectedImage != null) {
			cropImageView.setImageBitmap(selectedImage);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt(ASPECT_RATIO_X, mAspectRatioX);
		outState.putInt(ASPECT_RATIO_Y, mAspectRatioY);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cropper, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.getfromgallery:
			// do something
			// new addContactAsync()
			// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			Intent intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, PICTURE_TAKEN_FROM_GALLERY);

			return true;
		case R.id.getfromcamera:
			// do something
			// new addContactAsync()
			// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			// Intent cameraIntent = new
			// Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// startActivityForResult(cameraIntent, PICTURE_TAKEN_FROM_CAMERA);

			currentDateandTime = sdf.format(new Date());
			String name = currentDateandTime;

			boolean proceedToInternal = false;
			boolean canTakePicture = false;
			if (memoryManager.isExternalStorageWritable() == true
					&& memoryManager.isExternalStorageReadable() == true) {
				if (memoryManager.checkSdCardMemmory(10) == true) {
					proceedToInternal = false;
					canTakePicture = true;

					memoryManager.createDIRNoMediaFile(
							getResources().getString(
									R.string.katadigital_maindirectory),
							getResources().getString(
									R.string.kata_image_directory), true);

					imgdir = new File(Environment.getExternalStorageDirectory()
							+ getResources().getString(
									R.string.kata_image_directory));

					destination = new File(
							Environment.getExternalStorageDirectory()
									+ getResources().getString(
											R.string.kata_image_directory),
							name + ".jpg");
					imagefilename = name + ".jpg";

				} else {
					proceedToInternal = true;
					canTakePicture = false;
				}
			} else {
				proceedToInternal = true;
				canTakePicture = false;
			}

			if (proceedToInternal == true) {
				if (memoryManager.checkInternalMemmory(10) == true) {
					canTakePicture = true;

					memoryManager.createDIRNoMediaFile(
							getResources().getString(
									R.string.katadigital_maindirectory),
							getResources().getString(
									R.string.kata_image_directory), false);

					imgdir = new File(Environment.getDataDirectory()
							+ getResources().getString(
									R.string.kata_image_directory));
					destination = new File(Environment.getDataDirectory()
							+ getResources().getString(
									R.string.kata_image_directory), name
							+ ".jpg");
					imagefilename = name + ".jpg";
				} else {
					canTakePicture = false;
				}
			}

			if (canTakePicture == true) {
				Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
				// ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				intent2.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(destination));
				startActivityForResult(intent2, PICTURE_TAKEN_FROM_CAMERA);
			} else {
				alertDialog.alertMessage("Error!", "Insufficent memmory!",
						CropperActivity.this);
			}

			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void rotateImage(View view) {
		cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
	}

	// public void changeImage(View view) {
	//
	// }

	public void cropImage(View view) {
		if (CropperActivity.selectedImage != null) {
			croppedImage = cropImageView.getCroppedImage();
			openDialog(croppedImage);
		} else {
			Toast.makeText(CropperActivity.this,
					"Please select an image using gallery or camera!!",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case PICTURE_TAKEN_FROM_CAMERA:
			if (resultCode == RESULT_OK) {

				// InputStream stream = null;
				// try {
				// if (selectedImage != null) {
				// selectedImage.recycle();
				// }
				//
				// stream = getContentResolver().openInputStream(
				// (Uri) data.getExtras().get("data"));
				//
				// selectedImage = BitmapFactory.decodeStream(stream);
				// cropImageView.setImageBitmap(selectedImage);
				// } catch (FileNotFoundException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				// selectedImage = (Bitmap) data.getExtras().get("data");
				// selectedImage = Bitmap.createScaledBitmap(selectedImage,
				// cropImageView.getWidth(), 200, false);
				// cropImageView.setImageBitmap(selectedImage);

				System.out
						.println("pasok sa on activity result sa shelfmgtactivity");
				FileInputStream in;
				try {
					File file = new File(imgdir + "/" + imagefilename);
					if (file.exists()) {
						System.out.println("image exist");
						in = new FileInputStream(imgdir + "/" + imagefilename);
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 3;

						Bitmap bmp = BitmapFactory.decodeStream(in, null,
								options);

						in.close();
						selectedImage = bmp;
						cropImageView.setImageBitmap(selectedImage);

						//
						// ((ShelfMgtQuestionFragment)
						// currentFragment).shelfImageList
						// .add(bmp);
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;

		case PICTURE_TAKEN_FROM_GALLERY:
			if (resultCode == RESULT_OK && null != data) {
				String path = getGalleryImagePath(data);
				System.out.println(path + " <----- Image Path");
				if (path == null) {
					Toast.makeText(CropperActivity.this,
							"Invalid image selected", Toast.LENGTH_LONG).show();
				} else {
					imagePath = path;
					setSelectedImage(path);
				}

			}
			break;

		}
	}

	private void setSelectedImage(String path) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inSampleSize = 3;
		BitmapFactory.decodeFile(path, options);
		// if (mScreenWidth == 320 && mScreenHeight == 480) {
		// options.inSampleSize = calculateImageSize(options,
		// IMG_MAX_SIZE_MDPI);
		// } else {
		// options.inSampleSize = calculateImageSize(options, IMG_MAX_SIZE);
		// }

		options.inJustDecodeBounds = false;
		selectedImage = BitmapFactory.decodeFile(path, options);

		// int nh = (int) ( selectedImage.getHeight() * (1024.0 /
		// selectedImage.getWidth()) );
		// selectedImage= Bitmap.createScaledBitmap(selectedImage, 512, nh,
		// true);

		cropImageView.setImageBitmap(selectedImage);

	}

	private String getGalleryImagePath(Intent data) {
		Uri imgUri = data.getData();
		String filePath = "";
		if (data.getType() == null) {
			// For getting images from gallery.
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
					null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			filePath = cursor.getString(columnIndex);
			cursor.close();
		}
		return filePath;
	}

	public void openDialog(Bitmap croppedImage) {
		LayoutInflater inflater = (LayoutInflater) getLayoutInflater();

		View customView = inflater.inflate(R.layout.cropper_dialog, null);
		ImageView croppedImageView = (ImageView) customView
				.findViewById(R.id.croppedImageView);
		Button setPhoto = (Button) customView.findViewById(R.id.set_photo);
		Button cancelPhoto = (Button) customView
				.findViewById(R.id.cancel_photo);

		cancelPhoto.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectPhotoDialog.dismiss();
			}
		});

		setPhoto.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (CropperActivity.selectedImage != null) {
					selectPhotoDialog.dismiss();
					Intent returnIntent = new Intent();
					returnIntent.putExtra("resultimage", imagePath);
					setResult(RESULT_OK, returnIntent);

					finish();
				} else {
					Toast.makeText(CropperActivity.this,
							"Please select an image!!", Toast.LENGTH_LONG)
							.show();
				}

			}
		});

		int nh = (int) (croppedImage.getHeight() * (256.0 / croppedImage
				.getWidth()));
		croppedImage = Bitmap.createScaledBitmap(croppedImage, 256, nh, true);
		croppedImageView.setImageBitmap(croppedImage);

		// Build the dialog
		selectPhotoDialog = new Dialog(this);
		// selectModelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		selectPhotoDialog.setContentView(customView);
		// selectPhotoDialog.getWindow().setSoftInputMode(
		// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		selectPhotoDialog.setTitle("Cropped Image");

		selectPhotoDialog.show();
	}

	public static void clearBitmaps() {
		if (selectedImage != null) {
			selectedImage.recycle();
			selectedImage = null;
		}

		if (croppedImage != null) {
			croppedImage.recycle();
			croppedImage = null;
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		File file = new File(imgdir + "/" + imagefilename);

		if (file.exists()) {
			System.out.println("Photo Exist. Ready to delete");
			file.delete();

		}
		super.onBackPressed();
	}
}
