package com.katadigital.phone.callsmsblocker.messageListener;


import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.katadigital.phone.callsmsblocker.callBlockerService.CallBlockerService;
import com.katadigital.phone.callsmsblocker.objects.BlockedContact;

public class SmsReceiver extends BroadcastReceiver
{

	private Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		this.context=context;
		blockSms(intent);
	}
	
	public void blockSms(Intent intent)
	{

		Bundle bundle = intent.getExtras();        
		final SmsMessage[] msgs;
		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];    

			for (int i=0; i<msgs.length; i++)
			{
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                                 
			}
			
			final BlockedContact cn=CallBlockerService.blackList.get(msgs[0].getOriginatingAddress());
			if(cn!=null && cn.isBlockedForMessages())
			{
				abortBroadcast();
				
				Thread t=new Thread(new Runnable() 
				{
					
					@Override
					public void run() 
					{
						DateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
						Date date=new Date();
						String currentDate=dateFormat.format(date);

						//LOG FORMAT --> TITLE;;MESSAGE;;NAME;;NUMBER;;HOUR;;BODYMESSAGE;;SEPARATOR
						String message="Message Blocked;;A message from "+cn.getName()+" ("+msgs[0].getOriginatingAddress()+") was blocked at "+currentDate+";;"+cn.getName()+";;"+msgs[0].getOriginatingAddress()+";;"+currentDate+";;"+msgs[0].getMessageBody()+";;\r\n";
						writeInLog(message);
					}
				});
				
				t.start();
			}
		}
	}
	
	public void writeInLog(String message)
	{
		try
		{
			OutputStreamWriter fos = new OutputStreamWriter(context.openFileOutput("CallLog.txt", Context.MODE_APPEND));
			fos.append(message);
			fos.close();
			System.out.println("Writed in log succesfully");
		}
		catch(Exception e)
		{
			Log.e("Error", e.getMessage());
		}
		
	}

}
