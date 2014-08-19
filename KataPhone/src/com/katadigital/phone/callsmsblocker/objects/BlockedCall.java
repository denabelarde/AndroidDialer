package com.katadigital.phone.callsmsblocker.objects;

public class BlockedCall implements BlockedActivity
{
	String title;
	String message;
	String name;
	String number;
	String hour;

	public BlockedCall(String title, String message, String name, String number, String hour) 
	{
		this.title=title;
		this.message=message;
		this.name=name;
		this.number=number;
		this.hour=hour;
	}

	@Override
	public String getTitle() 
	{
		return title;
	}

	@Override
	public String getMessage() 
	{
		return message;
	}

	@Override
	public String getName() 
	{
		return name;
	}

	@Override
	public String getNumber() 
	{
		return number;
	}

	@Override
	public String getHour() 
	{
		return hour;
	}

	@Override
	public String getBodyMessage() 
	{
		return "";
	}

}
