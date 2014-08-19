package com.katadigital.phone.helpers;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.widget.ImageView;

import com.katadigital.phone.EditContactsActivity;
import com.katadigital.phone.R;
import com.katadigital.phone.entities.AddressDto;
import com.katadigital.phone.entities.ContactDto;
import com.katadigital.phone.entities.EmailDto;
import com.katadigital.phone.entities.PhoneNumberDto;

public final class QuickContactHelper {

	private static final String[] PHOTO_ID_PROJECTION = new String[] { ContactsContract.Contacts.PHOTO_ID };

	private static final String[] PHOTO_BITMAP_PROJECTION = new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO };
	ContactDto contactDto;
	private ImageView badge;

	// private String phoneNumber;
	Context context;
	private ContentResolver contentResolver;

	// public QuickContactHelper(Context context, ImageView badge,
	// String phoneNumber) {
	//
	// this.badge = badge;
	// this.phoneNumber = phoneNumber;
	// contentResolver = context.getContentResolver();
	//
	// }

	public QuickContactHelper(Context context) {
		this.context = context;
		contentResolver = context.getContentResolver();

	}

	public Bitmap addThumbnail(String id) {
		Bitmap thumbnail = null;
		final Integer thumbnailId = fetchThumbnailId(id);
		if (thumbnailId != null) {
			thumbnail = fetchThumbnail(thumbnailId);

		}

		return thumbnail;

	}

	public Integer fetchThumbnailId(String id) {

		// final Uri uri = Uri.withAppendedPath(
		// ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
		// Uri.encode(phoneNumber));
		// final Cursor cursor = contentResolver.query(uri, PHOTO_ID_PROJECTION,
		// null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

		Cursor cursor = context
				.getContentResolver()
				.query(ContactsContract.Data.CONTENT_URI,
						null,
						ContactsContract.Data.CONTACT_ID
								+ "="
								+ id
								+ " AND "
								+ ContactsContract.Data.MIMETYPE
								+ "='"
								+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
								+ "'", null, null);

		try {
			Integer thumbnailId = null;
			if (cursor.moveToFirst()) {
				thumbnailId = cursor.getInt(cursor
						.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
			}
			return thumbnailId;
		} finally {
			cursor.close();
		}

	}

	public Bitmap fetchThumbnail(final int thumbnailId) {

		final Uri uri = ContentUris.withAppendedId(
				ContactsContract.Data.CONTENT_URI, thumbnailId);
		final Cursor cursor = contentResolver.query(uri,
				PHOTO_BITMAP_PROJECTION, null, null, null);

		try {
			Bitmap thumbnail = null;
			if (cursor.moveToFirst()) {
				final byte[] thumbnailBytes = cursor.getBlob(0);
				if (thumbnailBytes != null) {
					thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes,
							0, thumbnailBytes.length);
				}
			}
			return thumbnail;
		} finally {
			cursor.close();
		}

	}

	// CONTACTS OPERATION
	public boolean updateContact(String name, String number, String email,
			String ContactId, Context context) {
		boolean success = true;
		String phnumexp = "^[0-9]*$";

		try {
			name = name.trim();
			email = email.trim();
			number = number.trim();

			if (name.equals("") && number.equals("") && email.equals("")) {
				success = false;
			} else if ((!number.equals("")) && (!match(number, phnumexp))) {
				success = false;
			} else if ((!email.equals("")) && (!isEmailValid(email))) {
				success = false;
			} else {
				ContentResolver contentResolver = context.getContentResolver();

				String where = ContactsContract.Data.CONTACT_ID + " =? AND "
						+ ContactsContract.Data.MIMETYPE + " =?";

				String[] emailParams = new String[] {
						ContactId,
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE };
				String[] nameParams = new String[] {
						ContactId,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
				String[] numberParams = new String[] {
						ContactId,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE };

				ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<android.content.ContentProviderOperation>();

				if (!email.equals("")) {
					ops.add(android.content.ContentProviderOperation
							.newUpdate(
									android.provider.ContactsContract.Data.CONTENT_URI)
							.withSelection(where, emailParams)
							.withValue(Email.DATA, email).build());

				}

				if (!name.equals("")) {
					ops.add(android.content.ContentProviderOperation
							.newUpdate(
									android.provider.ContactsContract.Data.CONTENT_URI)
							.withSelection(where, nameParams)
							.withValue(StructuredName.DISPLAY_NAME, name)
							.build());

				}

				if (!number.equals("")) {

					ops.add(android.content.ContentProviderOperation
							.newUpdate(
									android.provider.ContactsContract.Data.CONTENT_URI)
							.withSelection(where, numberParams)
							.withValue(Phone.NUMBER, number).build());
				}
				contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
			}
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	private boolean isEmailValid(String email) {
		String emailAddress = email.toString().trim();
		if (emailAddress == null)
			return false;
		else if (emailAddress.equals(""))
			return false;
		else if (emailAddress.length() <= 6)
			return false;
		else {
			String expression = "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_][a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$";
			CharSequence inputStr = emailAddress;
			Pattern pattern = Pattern.compile(expression,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			if (matcher.matches())
				return true;
			else
				return false;
		}
	}

	private boolean match(String stringToCompare, String regularExpression) {
		boolean success = false;
		Pattern pattern = Pattern.compile(regularExpression);
		Matcher matcher = pattern.matcher(stringToCompare);
		if (matcher.matches())
			success = true;
		return success;
	}

	public ContactDto getContactDetails(String contactID) {
		ContactDto contactDto = new ContactDto();
		contactDto.setContactID(contactID);
		String[] projection = new String[] { Phone.DISPLAY_NAME, Phone.NUMBER,
				Phone.TYPE };
		final Cursor phoneCursor = context.getContentResolver().query(
				Phone.CONTENT_URI, projection, Data.CONTACT_ID + "=?",
				new String[] { String.valueOf(contactID) }, null);

		if (phoneCursor.moveToFirst()) {

			do {

				int contactNumberColumnIndex = phoneCursor
						.getColumnIndex(Phone.NUMBER);

				PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
				contactDto.setDisplayName(phoneCursor.getString(phoneCursor
						.getColumnIndex(Phone.DISPLAY_NAME)) == null ? ""
						: phoneCursor.getString(phoneCursor
								.getColumnIndex(Phone.DISPLAY_NAME)));

				phoneNumberDto.setNumber(phoneCursor
						.getString(contactNumberColumnIndex) == null ? ""
						: phoneCursor.getString(contactNumberColumnIndex));
				if (!phoneNumberDto.getNumber().isEmpty()) {
					int numType = phoneCursor.getInt(phoneCursor
							.getColumnIndex(Phone.TYPE));

					switch (numType) {
					case Phone.TYPE_MOBILE:
						// do something with the Home number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[0]);
						break;
					case Phone.TYPE_WORK:
						// do something with the Mobile number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[1]);
						break;
					case Phone.TYPE_HOME:
						// do something with the Work number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[2]);
						break;
					case Phone.TYPE_MAIN:
						// do something with the Work number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[3]);
						break;
					case Phone.TYPE_FAX_WORK:
						// do something with the Work number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[4]);
						break;
					case Phone.TYPE_FAX_HOME:
						// do something with the Work number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[5]);
						break;
					case Phone.TYPE_PAGER:
						// do something with the Work number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[6]);
						break;
					case Phone.TYPE_OTHER:
						// do something with the Work number here...
						phoneNumberDto.setNumberType(context.getResources()
								.getStringArray(R.array.numberspinner)[7]);
						break;
					}
					contactDto.addPhoneNumbers(phoneNumberDto);
				}

			} while (phoneCursor.moveToNext());
		}
		phoneCursor.close();
		// POSSIBLE ADDING OF PROFILE PIC
		// int thumbnailId = 0;
		// thumbnailId = phoneCursor.getInt(phoneCursor
		// .getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
		//
		// final Uri uri = ContentUris.withAppendedId(
		// ContactsContract.Data.CONTENT_URI, thumbnailId);
		// final Cursor cursor = getActivity().getContentResolver()
		// .query(uri, PHOTO_BITMAP_PROJECTION, null, null,
		// null);
		// try {
		//
		// if (cursor.moveToFirst()) {
		// final byte[] thumbnailBytes = cursor.getBlob(0);
		// if (thumbnailBytes != null) {
		// contactDto.setProfilepic(BitmapFactory
		// .decodeByteArray(thumbnailBytes, 0,
		// thumbnailBytes.length));
		//
		// }
		// }
		// } finally {
		// cursor.close();
		// }

		String nameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] nameWhereParams = new String[] {
				String.valueOf(contactID),
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
		Cursor nameCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, nameWhere,
				nameWhereParams, null);
		if (nameCur.moveToNext()) {

			contactDto
					.setFirstName(nameCur.getString(nameCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)) == null ? ""
							: nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
			contactDto
					.setLastName(nameCur.getString(nameCur
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)) == null ? ""
							: nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));

		}
		nameCur.close();

		String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] noteWhereParams = new String[] { String.valueOf(contactID),
				ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };
		Cursor noteCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, noteWhere,
				noteWhereParams, null);
		if (noteCur.moveToNext()) {

			contactDto
					.setNote(noteCur.getString(noteCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)) == null ? ""
							: noteCur.getString(noteCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)));

		}
		noteCur.close();

		String email = null;
		int emailType = 0;

		String emailWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] emailWhereParams = new String[] { String.valueOf(contactID),
				ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE };
		Cursor emailCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, emailWhere,
				emailWhereParams, null);
		while (emailCur.moveToNext()) {
			do {
				EmailDto emailDto = new EmailDto();
				emailDto.setEmail(emailCur.getString(emailCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)) == null ? ""
						: emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));

				if (!emailDto.getEmail().isEmpty()) {
					emailType = emailCur
							.getInt(emailCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

					System.out.println(email + " <---- Email baby");

					switch (emailType) {

					case Email.TYPE_HOME:
						// do something with the Work number here...

						emailDto.setEmailType(context.getResources()
								.getStringArray(R.array.emailspinner)[0]);
						break;
					case Email.TYPE_WORK:
						// do something with the Mobile number here...
						emailDto.setEmailType(context.getResources()
								.getStringArray(R.array.emailspinner)[1]);
						break;

					case Email.TYPE_OTHER:
						// do something with the Work number here...
						emailDto.setEmailType(context.getResources()
								.getStringArray(R.array.emailspinner)[2]);
						break;
					}

					contactDto.addEmails(emailDto);
				}

			} while (emailCur.moveToNext());

		}
		emailCur.close();

		// String orgName = null;

		String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] orgWhereParams = new String[] { String.valueOf(contactID),
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
		Cursor orgCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, orgWhere,
				orgWhereParams, null);
		while (orgCur.moveToNext()) {

			contactDto
					.setCompany(orgCur.getString(orgCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA)) == null ? ""
							: orgCur.getString(orgCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA)));
			// String title = orgCur.getString(orgCur
			// .getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
		}
		orgCur.close();

		String addressWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] addressWhereParams = new String[] {
				String.valueOf(contactID),
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
		Cursor addressCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, addressWhere,
				addressWhereParams, null);
		while (addressCur.moveToNext()) {

			do {
				AddressDto addressDto = new AddressDto();

				addressDto
						.setStreetStr(addressCur.getString(addressCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) == null ? ""
								: addressCur.getString(addressCur
										.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
				addressDto
						.setNeigborhoodStr(addressCur.getString(addressCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD)) == null ? ""
								: addressCur.getString(addressCur
										.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD)));
				addressDto
						.setCityStr(addressCur.getString(addressCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) == null ? ""
								: addressCur.getString(addressCur
										.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
				addressDto
						.setZipCodeStr(addressCur.getString(addressCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) == null ? ""
								: addressCur.getString(addressCur
										.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
				addressDto
						.setCountryStr(addressCur.getString(addressCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) == null ? ""
								: addressCur.getString(addressCur
										.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));

				if (!addressDto.getCityStr().isEmpty()
						&& !addressDto.getCountryStr().isEmpty()
						&& !addressDto.getNeigborhoodStr().isEmpty()
						&& !addressDto.getStreetStr().isEmpty()
						&& !addressDto.getZipCodeStr().isEmpty()) {
					int addressType = addressCur
							.getInt(addressCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

					System.out.println(email + " <---- Email baby");

					switch (addressType) {

					case StructuredPostal.TYPE_HOME:
						// do something with the Work number here...

						addressDto.setAddressType(context.getResources()
								.getStringArray(R.array.addressspinner)[0]);
						break;
					case Email.TYPE_WORK:
						// do something with the Mobile number here...
						addressDto.setAddressType(context.getResources()
								.getStringArray(R.array.addressspinner)[1]);
						break;

					case Email.TYPE_OTHER:
						// do something with the Work number here...
						addressDto.setAddressType(context.getResources()
								.getStringArray(R.array.addressspinner)[2]);
						break;

					}
					contactDto.addAddresses(addressDto);
				}

			} while (addressCur.moveToNext());

		}
		addressCur.close();

		System.out.println(String.valueOf(contactID)
				+ " <--- Possible contact id");

		// GETTING BIRTHDAY
		String birthdayWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?AND "
				+ ContactsContract.CommonDataKinds.Event.TYPE + "= ?";
		String[] birthdayWhereParams = new String[] {
				String.valueOf(contactID),
				ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
				ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + "" };
		Cursor birthdayCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, null, birthdayWhere,
				birthdayWhereParams, null);
		while (birthdayCur.moveToNext()) {

			// contactDto
			// .setCompany(orgCur.getString(orgCur
			// .getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA))
			// == null ? ""
			// : orgCur.getString(orgCur
			// .getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA)));
		contactDto.setBirthday(	birthdayCur
					.getString(birthdayCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)));
			// String title = orgCur.getString(orgCur
			// .getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
		}
		birthdayCur.close();

		// GENERATING THUMBNAILID
		contactDto.setProfilepic(addThumbnail(String.valueOf(contactID)));

		return contactDto;
	}

	public String getContactInformation(String number) {

		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		Cursor cur = context.getContentResolver().query(
				uri,
				new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME,
						ContactsContract.PhoneLookup.NUMBER,
						ContactsContract.PhoneLookup._ID }, null, null, null);
		String id2 = "";
		if (cur.moveToNext()) {
			int id = cur.getColumnIndex(ContactsContract.Contacts._ID);

			id2 = cur.getString(id);

		}
		cur.close();
		return id2;

	}

	public String getContactName(String number) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		Cursor cur = context.getContentResolver().query(
				uri,
				new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME,
						ContactsContract.PhoneLookup.NUMBER,
						ContactsContract.PhoneLookup._ID }, null, null, null);
		String contactName = "";
		if (cur.moveToNext()) {
			int name = cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

			contactName = cur.getString(name);

		}
		cur.close();
		return contactName;

	}

	public boolean updateContact(ContactDto contactDto, ContactDto oldContactDto) {
		boolean success = true;

		// DELETION OF EXISTING NUMBERS
		ArrayList<android.content.ContentProviderOperation> number_oplist = new ArrayList<android.content.ContentProviderOperation>();
		for (PhoneNumberDto phoneNumberDto : oldContactDto.getPhoneNumbers()) {
			if (checkIfPhoneNumberIsPresent(phoneNumberDto,
					contactDto.getPhoneNumbers()) == false) {
				int phoneTypeIndex = 0;

				if (phoneNumberDto.getNumberType().equalsIgnoreCase("Mobile")) {
					phoneTypeIndex = Phone.TYPE_MOBILE;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Work")) {
					phoneTypeIndex = Phone.TYPE_WORK;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Home")) {
					phoneTypeIndex = Phone.TYPE_HOME;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Main")) {
					phoneTypeIndex = Phone.TYPE_MAIN;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Work Fax")) {
					phoneTypeIndex = Phone.TYPE_FAX_WORK;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Home Fax")) {
					phoneTypeIndex = Phone.TYPE_FAX_HOME;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Pager")) {
					phoneTypeIndex = Phone.TYPE_PAGER;
				} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
						"Other")) {
					phoneTypeIndex = Phone.TYPE_OTHER;
				}

				number_oplist.add(ContentProviderOperation
						.newDelete(Data.CONTENT_URI)
						.withSelection(
								ContactsContract.Data.CONTACT_ID + "=?"
										+ " AND "
										+ ContactsContract.Data.MIMETYPE + "=?"
										+ " AND " + Phone.TYPE + "=?" + " AND "
										+ Phone.NUMBER + "=?",
								new String[] { contactDto.getContactID(),
										Phone.CONTENT_ITEM_TYPE,
										String.valueOf(phoneTypeIndex),
										phoneNumberDto.getNumber() })

						.build());
			}

		}

		try {
			if (!number_oplist.isEmpty()) {
				context.getContentResolver().applyBatch(
						ContactsContract.AUTHORITY, number_oplist);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// INSERTING NEW PHONE NUMBERS
		ContentValues phoneContentValues = new ContentValues();
		for (PhoneNumberDto phoneNumberDto : contactDto.getPhoneNumbers()) {
			if (checkIfPhoneNumberIsPresent(phoneNumberDto,
					oldContactDto.getPhoneNumbers()) == false) {
				if (phoneNumberDto != null) {
					System.out.println(phoneNumberDto.getNumber());
					phoneContentValues = new ContentValues();
					phoneContentValues.put(Data.RAW_CONTACT_ID,
							Integer.parseInt(contactDto.getContactID()));
					phoneContentValues.put(Data.MIMETYPE,
							Phone.CONTENT_ITEM_TYPE);
					phoneContentValues.put(Phone.NUMBER,
							phoneNumberDto.getNumber());

					int phoneTypeIndex = 0;

					if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Mobile")) {
						phoneTypeIndex = Phone.TYPE_MOBILE;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Work")) {
						phoneTypeIndex = Phone.TYPE_WORK;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Home")) {
						phoneTypeIndex = Phone.TYPE_HOME;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Main")) {
						phoneTypeIndex = Phone.TYPE_MAIN;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Work Fax")) {
						phoneTypeIndex = Phone.TYPE_FAX_WORK;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Home Fax")) {
						phoneTypeIndex = Phone.TYPE_FAX_HOME;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Pager")) {
						phoneTypeIndex = Phone.TYPE_PAGER;
					} else if (phoneNumberDto.getNumberType().equalsIgnoreCase(
							"Other")) {
						phoneTypeIndex = Phone.TYPE_OTHER;
					}
					phoneContentValues.put(Phone.TYPE, phoneTypeIndex);
					context.getContentResolver().insert(Data.CONTENT_URI,
							phoneContentValues);
				}
			}

		}

		// DELETE OF OLD EMAILS
		ArrayList<android.content.ContentProviderOperation> emails_oplist = new ArrayList<android.content.ContentProviderOperation>();
		for (EmailDto emailDto : oldContactDto.getEmails()) {
			if (checkIfEmailIsPresent(emailDto, contactDto.getEmails()) == false) {
				int emailTypeIndex = 0;

				if (emailDto.getEmailType().equalsIgnoreCase("Work")) {
					emailTypeIndex = Email.TYPE_WORK;
				} else if (emailDto.getEmailType().equalsIgnoreCase("Home")) {
					emailTypeIndex = Email.TYPE_HOME;
				}
				if (emailDto.getEmailType().equalsIgnoreCase("Other")) {
					emailTypeIndex = Email.TYPE_OTHER;
				}

				emails_oplist.add(ContentProviderOperation
						.newDelete(Data.CONTENT_URI)
						.withSelection(
								ContactsContract.Data.CONTACT_ID + "=?"
										+ " AND "
										+ ContactsContract.Data.MIMETYPE + "=?"
										+ " AND " + Email.TYPE + "=?" + " AND "
										+ Email.DATA + "=?",
								new String[] { contactDto.getContactID(),
										Email.CONTENT_ITEM_TYPE,
										String.valueOf(emailTypeIndex),
										emailDto.getEmail() }).build());
			}

		}

		try {
			if (!emails_oplist.isEmpty()) {
				context.getContentResolver().applyBatch(
						ContactsContract.AUTHORITY, emails_oplist);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// INSERTING NEW EMAILS
		ContentValues emailContentValues = new ContentValues();
		for (EmailDto emailDto : contactDto.getEmails()) {
			if (checkIfEmailIsPresent(emailDto, oldContactDto.getEmails()) == false) {
				if (emailDto != null) {
					emailContentValues = new ContentValues();
					emailContentValues.put(Data.RAW_CONTACT_ID,
							Integer.parseInt(contactDto.getContactID()));
					emailContentValues.put(Data.MIMETYPE,
							Email.CONTENT_ITEM_TYPE);
					emailContentValues.put(Email.DATA, emailDto.getEmail());

					int emailTypeIndex = 0;

					if (emailDto.getEmailType().equalsIgnoreCase("Work")) {
						emailTypeIndex = Email.TYPE_WORK;
					} else if (emailDto.getEmailType().equalsIgnoreCase("Home")) {
						emailTypeIndex = Email.TYPE_HOME;
					} else if (emailDto.getEmailType()
							.equalsIgnoreCase("Other")) {
						emailTypeIndex = Email.TYPE_OTHER;
					}
					emailContentValues.put(Email.TYPE, emailTypeIndex);
					context.getContentResolver().insert(Data.CONTENT_URI,
							emailContentValues);
				}
			}

		}

		// DELETION OF OLD ADDRESSES
		ArrayList<android.content.ContentProviderOperation> address_oplist = new ArrayList<android.content.ContentProviderOperation>();
		for (AddressDto addressDto : oldContactDto.getAddresses()) {
			if (checkIfAddressPresent(addressDto, contactDto.getAddresses()) == false) {
				int addressTypeIndex = 0;

				if (addressDto.getAddressType().equalsIgnoreCase("Work")) {
					addressTypeIndex = StructuredPostal.TYPE_WORK;
				} else if (addressDto.getAddressType().equalsIgnoreCase("Home")) {
					addressTypeIndex = StructuredPostal.TYPE_HOME;
				}
				if (addressDto.getAddressType().equalsIgnoreCase("Other")) {
					addressTypeIndex = StructuredPostal.TYPE_OTHER;
				}

				address_oplist.add(ContentProviderOperation
						.newDelete(Data.CONTENT_URI)
						.withSelection(
								ContactsContract.Data.CONTACT_ID + "=?"
										+ " AND "
										+ ContactsContract.Data.MIMETYPE + "=?"
										+ " AND " + StructuredPostal.TYPE
										+ "=?" + " AND "
										+ StructuredPostal.STREET + "=?"
										+ " AND "
										+ StructuredPostal.NEIGHBORHOOD + "=?"
										+ " AND " + StructuredPostal.CITY
										+ "=?" + " AND "
										+ StructuredPostal.COUNTRY + "=?"
										+ " AND " + StructuredPostal.POSTCODE
										+ "=?",
								new String[] { contactDto.getContactID(),
										StructuredPostal.CONTENT_ITEM_TYPE,
										String.valueOf(addressTypeIndex),
										addressDto.getStreetStr(),
										addressDto.getNeigborhoodStr(),
										addressDto.getCityStr(),
										addressDto.getCountryStr(),
										addressDto.getZipCodeStr() }).build());
			}

		}

		try {
			if (!address_oplist.isEmpty()) {
				context.getContentResolver().applyBatch(
						ContactsContract.AUTHORITY, address_oplist);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// INSERTING NEW ADDRESSES
		ContentValues values = new ContentValues();
		for (AddressDto addressDto : contactDto.getAddresses()) {
			if (checkIfAddressPresent(addressDto, oldContactDto.getAddresses()) == false) {
				if (addressDto != null) {
					values = new ContentValues();
					values.put(Data.RAW_CONTACT_ID,
							Integer.parseInt(contactDto.getContactID()));
					values.put(Data.MIMETYPE,
							StructuredPostal.CONTENT_ITEM_TYPE);

					int addressTypeIndex = 0;

					if (addressDto.getAddressType().equalsIgnoreCase("Work")) {
						addressTypeIndex = StructuredPostal.TYPE_WORK;
					} else if (addressDto.getAddressType().equalsIgnoreCase(
							"Home")) {
						addressTypeIndex = StructuredPostal.TYPE_HOME;
					} else if (addressDto.getAddressType().equalsIgnoreCase(
							"Other")) {
						addressTypeIndex = StructuredPostal.TYPE_OTHER;
					}
					values.put(StructuredPostal.TYPE, addressTypeIndex);
					values.put(StructuredPostal.STREET,
							addressDto.getStreetStr());
					values.put(StructuredPostal.NEIGHBORHOOD,
							addressDto.getNeigborhoodStr());
					values.put(StructuredPostal.CITY, addressDto.getCityStr());
					values.put(StructuredPostal.COUNTRY,
							addressDto.getCountryStr());
					values.put(StructuredPostal.POSTCODE,
							addressDto.getZipCodeStr());
					context.getContentResolver().insert(Data.CONTENT_URI,
							values);
				}
			}

		}

		// UPDATING OTHER DETAILS
		ArrayList<android.content.ContentProviderOperation> op_list = new ArrayList<android.content.ContentProviderOperation>();

		if (!contactDto.getDisplayName().isEmpty()) {
			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									StructuredName.CONTENT_ITEM_TYPE })
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
							contactDto.getDisplayName()).build());
		}

		if (!contactDto.getFirstName().isEmpty()) {
			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									StructuredName.CONTENT_ITEM_TYPE })
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
							contactDto.getFirstName()).build());

		}

		// ------------------------------------------------------ Names
		if (!contactDto.getLastName().isEmpty()) {
			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									StructuredName.CONTENT_ITEM_TYPE })
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
							contactDto.getLastName()).build());

			// .withSelection(
			// ContactsContract.Data.CONTACT_ID + "=?" + " AND "
			// + ContactsContract.Data.MIMETYPE + "=?"
			// + " AND " + StructuredName.FAMILY_NAME
			// + "=?",
			// new String[] { contactDto.getContactID(),
			// StructuredName.CONTENT_ITEM_TYPE,
			// oldContactDto.getLastName() })

		}

		if (contactDto.getProfilepic() != null) {
			// Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable())
			// .getBitmap();
			System.out.println("Pasok sa Photos Creation");
			ByteArrayOutputStream newStream = new ByteArrayOutputStream();
			contactDto.getProfilepic().compress(Bitmap.CompressFormat.PNG, 75,
					newStream);
			byte[] newImage = newStream.toByteArray();

			// ByteArrayOutputStream oldStream = new ByteArrayOutputStream();
			// oldContactDto.getProfilepic().compress(Bitmap.CompressFormat.PNG,
			// 75, oldStream);
			// byte[] oldImage = oldStream.toByteArray();

			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									Photo.CONTENT_ITEM_TYPE })
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
							newImage).build());

		}

		if (!contactDto.getBirthday().isEmpty()) {
			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									Event.CONTENT_ITEM_TYPE })
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

		if (!contactDto.getCompany().isEmpty()) {
			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									Organization.CONTENT_ITEM_TYPE })
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
			op_list.add(ContentProviderOperation
					.newUpdate(Data.CONTENT_URI)
					.withSelection(
							ContactsContract.Data.CONTACT_ID + "=?" + " AND "
									+ ContactsContract.Data.MIMETYPE + "=?",
							new String[] { contactDto.getContactID(),
									Note.CONTENT_ITEM_TYPE })
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Note.NOTE,
							contactDto.getNote())

					.build());
		}

		try {

			ContentProviderResult[] results = contentResolver.applyBatch(
					ContactsContract.AUTHORITY, op_list);

		} catch (Exception e) {
			e.printStackTrace();

			success = false;
		}

		return success;
	}

	public boolean isContactFavorite(String contactName) {
		boolean result = false;

		// ContactsContract.Data.CONTACT_ID

		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.STARRED };

		Cursor cursor = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				projection,
				"starred=? AND " + ContactsContract.Contacts.DISPLAY_NAME
						+ "=?", new String[] { "1", contactName }, null);

		int x = 0;

		if (cursor.moveToFirst()) {
			do {
				x++;
			} while (cursor.moveToNext());
		}

		if (x > 0) {
			result = true;
		}

		cursor.close();

		return result;
	}

	public ContactDto removeNullValues(ContactDto contactToFilter) {
		ContactDto contactDto = new ContactDto();
		contactDto = contactToFilter;

		// REMOVING NULL VALUES FROM PHONENUMBERS
		ArrayList<PhoneNumberDto> dummyPhoneNumberList = new ArrayList<PhoneNumberDto>();
		for (int x = 0; x < contactDto.getPhoneNumbers().size(); x++) {
			if (contactDto.getPhoneNumbers().get(x) != null) {
				if (!contactDto.getPhoneNumbers().get(x).getNumber().isEmpty()
						&& !contactDto.getPhoneNumbers().get(x).getNumberType()
								.isEmpty()) {
					dummyPhoneNumberList.add(contactDto.getPhoneNumbers()
							.get(x));
				}
			}
		}
		contactDto.phoneNumbers.clear();
		contactDto.phoneNumbers.addAll(dummyPhoneNumberList);

		// REMOVING NULL VALUES FROM EMAILS
		ArrayList<EmailDto> dummyEmailList = new ArrayList<EmailDto>();
		for (int x = 0; x < contactDto.getEmails().size(); x++) {
			if (contactDto.getEmails().get(x) != null) {

				if (!contactDto.getEmails().get(x).getEmail().isEmpty()
						&& !contactDto.getEmails().get(x).getEmailType()
								.isEmpty()) {
					dummyEmailList.add(contactDto.getEmails().get(x));
				}
			}
		}
		contactDto.emails.clear();
		contactDto.emails.addAll(dummyEmailList);

		// REMOVING NULL VALUES FROM ADDRESSES
		ArrayList<AddressDto> dummyAddressList = new ArrayList<AddressDto>();
		for (int x = 0; x < contactDto.getAddresses().size(); x++) {
			System.out.println("here on address clear null");
			if (contactDto.getAddresses().get(x) != null) {

				// if ((!contactDto.getAddresses().get(x).getCityStr().isEmpty()
				// && !contactDto.getAddresses().get(x).getCountryStr()
				// .isEmpty()
				// && !contactDto.getAddresses().get(x)
				// .getNeigborhoodStr().isEmpty()
				// && !contactDto.getAddresses().get(x).getStreetStr()
				// .isEmpty() && contactDto.getAddresses().get(x)
				// .getZipCodeStr().isEmpty())
				// && !contactDto.getAddresses().get(x).getAddressType()
				// .isEmpty()) {
				dummyAddressList.add(contactDto.getAddresses().get(x));
				// }
			}
		}
		contactDto.addresses.clear();
		contactDto.addresses.addAll(dummyAddressList);

		return contactDto;
	}

	public boolean checkIfPhoneNumberIsPresent(PhoneNumberDto phoneNumberDto,
			ArrayList<PhoneNumberDto> phoneNumberList) {
		boolean result = false;
		for (PhoneNumberDto phoneNumberDto2 : phoneNumberList) {
			if (phoneNumberDto2.getNumber().equalsIgnoreCase(
					phoneNumberDto.getNumber())
					&& phoneNumberDto2.getNumberType().equalsIgnoreCase(
							phoneNumberDto.getNumberType())) {
				result = true;
				break;
			}
		}

		return result;
	}

	public boolean checkIfEmailIsPresent(EmailDto emailDto,
			ArrayList<EmailDto> emailList) {
		boolean result = false;
		for (EmailDto emailDto2 : emailList) {
			if (emailDto2.getEmail().equalsIgnoreCase(emailDto.getEmail())
					&& emailDto2.getEmailType().equalsIgnoreCase(
							emailDto.getEmailType())) {
				result = true;
				break;
			}
		}

		return result;
	}

	public boolean checkIfAddressPresent(AddressDto addressDto,
			ArrayList<AddressDto> addressList) {
		boolean result = false;
		for (AddressDto addressDto2 : addressList) {
			if (addressDto2.getAddressType().equalsIgnoreCase(
					addressDto.getAddressType())
					&& addressDto2.getCityStr().equalsIgnoreCase(
							addressDto.getCityStr())
					&& addressDto2.getCountryStr().equalsIgnoreCase(
							addressDto.getCountryStr())
					&& addressDto2.getNeigborhoodStr().equalsIgnoreCase(
							addressDto.getNeigborhoodStr())
					&& addressDto2.getStreetStr().equalsIgnoreCase(
							addressDto.getStreetStr())
					&& addressDto2.getZipCodeStr().equalsIgnoreCase(
							addressDto.getZipCodeStr())) {
				result = true;
				break;
			}
		}

		return result;
	}

	public static Intent callfromDefaultDialer(Context ctxt, String no) {

		Intent i = new Intent();
		i.setAction(Intent.ACTION_CALL);
		i.setData(Uri.parse("tel:" + no));
		PackageManager pm = ctxt.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
		for (ResolveInfo info : list) {
			String pkgnam = info.activityInfo.packageName;
			if (pkgnam.toLowerCase().equals("com.android.phone")) {
				i.setClassName(pkgnam, info.activityInfo.name);
				return i;
			}
		}

		return i;
	}
}