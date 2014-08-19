package com.katadigital.phone.entities;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class ContactDto {

	String contactID = "";
	String firstName = "";
	String lastName = "";
	String DisplayName = "";
	String company = "";
	String birthday = "";
	public ArrayList<AddressDto> addresses = new ArrayList<AddressDto>();
	public ArrayList<EmailDto> emails = new ArrayList<EmailDto>();
	public ArrayList<PhoneNumberDto> phoneNumbers = new ArrayList<PhoneNumberDto>();
	Bitmap profilepic = null;
	String note = "";

	public String getContactID() {
		return contactID;
	}

	public void setContactID(String contactID) {
		this.contactID = contactID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public ArrayList<AddressDto> getAddresses() {
		return addresses;
	}

	public AddressDto getAddressWithID(int id) {
		AddressDto address = new AddressDto();

		for (AddressDto addressDto : addresses) {
			if (addressDto.getId() == id) {
				address = addressDto;
			}
		}

		return address;
	}

	// public AddressDto removeAddressWithID(int id) {
	// AddressDto address = new AddressDto();
	// int x = 0;
	// for (AddressDto addressDto : addresses) {
	// if (addressDto.getId() == id) {
	// addresses.remove(x);
	// }
	// x++;
	// }
	//
	// return address;
	// }

	public void addAddresses(AddressDto address) {
		addresses.add(address);
	}

	public ArrayList<EmailDto> getEmails() {
		return emails;
	}

	public EmailDto getEmailWithID(int id) {
		EmailDto email = new EmailDto();

		for (EmailDto emailDto : emails) {
			if (emailDto.getId() == id) {
				email = emailDto;
			}
		}

		return email;
	}

	// public EmailDto removeEmailWithID(int id) {
	// EmailDto email = new EmailDto();
	// int x = 0;
	// for (EmailDto emailDto : emails) {
	// if (emailDto.getId() == id) {
	// emails.remove(x);
	// }
	// x++;
	// }
	//
	// return email;
	// }

	public void addEmails(EmailDto email) {
		emails.add(email);
	}

	public ArrayList<PhoneNumberDto> getPhoneNumbers() {
		return phoneNumbers;
	}

	// public PhoneNumberDto getPhoneNumberWithID(int id) {
	// PhoneNumberDto phoneNumber = new PhoneNumberDto();
	//
	// for (PhoneNumberDto phoneNumberDto : phoneNumbers) {
	// if (phoneNumberDto.getId() == id) {
	// phoneNumber = phoneNumberDto;
	// }
	// }
	//
	// return phoneNumber;
	// }

	// public PhoneNumberDto removePhoneNumberWithID(int id) {
	// PhoneNumberDto phoneNumber = new PhoneNumberDto();
	// int x = 0;
	// for (PhoneNumberDto phoneNumberDto : phoneNumbers) {
	// if (phoneNumberDto.getId() == id) {
	// phoneNumbers.remove(x);
	// }
	// x++;
	// }
	//
	// return phoneNumber;
	// }

	public void addPhoneNumbers(PhoneNumberDto phonenumber) {
		phoneNumbers.add(phonenumber);
	}

	public Bitmap getProfilepic() {
		return profilepic;
	}

	public void setProfilepic(Bitmap profilepic) {
		this.profilepic = profilepic;
	}

	public void setAddresses(ArrayList<AddressDto> addresses) {
		this.addresses = addresses;
	}

	public void setEmails(ArrayList<EmailDto> emails) {
		this.emails = emails;
	}

	public void setPhoneNumbers(ArrayList<PhoneNumberDto> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
