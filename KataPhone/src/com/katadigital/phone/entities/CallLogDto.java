package com.katadigital.phone.entities;

import java.util.ArrayList;
import java.util.Date;

public class CallLogDto {

	String contactName;
	String number;
	Date callDate;
	String callLogID;
	String callType;
	String numberType;

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Date getCallDate() {
		return callDate;
	}

	public void setCallDate(Date callDate) {
		this.callDate = callDate;
	}

	public String getCallLogID() {
		return callLogID;
	}

	public void setCallLogID(String callLogID) {
		this.callLogID = callLogID;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getNumberType() {
		return numberType;
	}

	public void setNumberType(String numberType) {
		this.numberType = numberType;
	}

}
