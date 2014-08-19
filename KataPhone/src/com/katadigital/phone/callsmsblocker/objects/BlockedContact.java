package com.katadigital.phone.callsmsblocker.objects;

import java.io.Serializable;

public class BlockedContact implements Serializable
{
	
	private static final long serialVersionUID = 7304477898143785497L;
	private String name;
	private String number;
	private int numberType;
	private boolean blockedForCalling;
	private boolean blockedForMessages;
	
	public BlockedContact(String name, String number, int numberType, boolean blockedForCalling, boolean blockedForMessages)
	{
		this.name=name;
		this.number=number;
		this.numberType=numberType;
		this.blockedForCalling=blockedForCalling;
		this.blockedForMessages=blockedForMessages;
	}

	public boolean isBlockedForCalling() {
		return blockedForCalling;
	}

	public void setBlockedForCalling(boolean blockedForCalling) {
		this.blockedForCalling = blockedForCalling;
	}

	public boolean isBlockedForMessages() {
		return blockedForMessages;
	}

	public void setBlockedForMessages(boolean blockedForMessages) {
		this.blockedForMessages = blockedForMessages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getNumberType() {
		return numberType;
	}

	public void setNumberType(int numberType) {
		this.numberType = numberType;
	}
	
}
