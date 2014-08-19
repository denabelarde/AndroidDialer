package com.katadigital.phone.callsmsblocker.callListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class ServiceReciever extends BroadcastReceiver
{

	private static TelephonyManager telephony;
	private static DeviceStateListener phoneListener;
	private static boolean firstTime=true;
	
	public ServiceReciever(Context context)
	{
		telephony=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		phoneListener=new DeviceStateListener(context);
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{	
		if(firstTime)
		{
			telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			firstTime=false;
		}
	}
	
	public void stopListening()
	{
		telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
		firstTime=true;
	}

}
