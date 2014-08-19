package com.katadigital.phone.callsmsblocker.notificationCenter;

import com.katadigital.phone.MainActivity;

import android.view.Gravity;
import android.widget.Toast;

public class CallBlockerToastNotification 
{

	public static void showDefaultShortNotification(String message)
	{
		Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_SHORT).show();
	}
	
	public static void showDefaultLongNotification(String message)
	{
		Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_LONG).show();
	}
	
	public static void showTopShortNotification(String message)
	{
		Toast t= Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.TOP, 0, 0);
		t.show();
	}
	
	public static void showRightShortNotification(String message)
	{
		Toast t= Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.RIGHT, 0, 0);
		t.show();
	}
	
	public static void showLeftShortNotification(String message)
	{
		Toast t= Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.LEFT, 0, 0);
		t.show();
	}
	
	public static void showTopLongNotification(String message)
	{
		Toast t= Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_LONG);
		t.setGravity(Gravity.TOP, 0, 0);
		t.show();
	}
	
	public static void showRightLongNotification(String message)
	{
		Toast t= Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_LONG);
		t.setGravity(Gravity.RIGHT, 0, 0);
		t.show();
	}
	
	public static void showLeftLongNotification(String message)
	{
		Toast t= Toast.makeText(MainActivity.applicationContext, message, Toast.LENGTH_LONG);
		t.setGravity(Gravity.LEFT, 0, 0);
		t.show();
	}
}
