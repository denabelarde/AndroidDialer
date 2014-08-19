package com.katadigital.phone.callsmsblocker.callListener;

import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.katadigital.phone.callsmsblocker.callBlockerService.CallBlockerService;
import com.katadigital.phone.callsmsblocker.objects.BlockedContact;

public class DeviceStateListener extends PhoneStateListener {
	private ITelephony telephonyService;
	private Context context;

	public DeviceStateListener(Context context) {
		this.context = context;
		initializeTelephonyService();
	}

	private void initializeTelephonyService() {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			Class clase = Class.forName(telephonyManager.getClass().getName());
			Method method = clase.getDeclaredMethod("getITelephony");
			method.setAccessible(true);
			telephonyService = (ITelephony) method.invoke(telephonyManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCallStateChanged(int state, final String incomingNumber) {
		switch (state) {

		case TelephonyManager.CALL_STATE_RINGING:

			final BlockedContact cn = CallBlockerService.blackList
					.get(incomingNumber);
			if (cn != null && cn.isBlockedForCalling()) {
				try {
					telephonyService.endCall();
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							DateFormat dateFormat = new SimpleDateFormat(
									"yyyy/MM/dd - HH:mm:ss");
							Date date = new Date();
							String currentDate = dateFormat.format(date);

							// LOG FORMAT -->
							// TITLE;;MESSAGE;;NAME;;NUMBER;;HOUR;;BODYMESSAGE(NULL);;SEPARATOR
							String message = "Call Blocked;;A call from "
									+ cn.getName() + " (" + incomingNumber
									+ ") was blocked at " + currentDate + ";;"
									+ cn.getName() + ";;" + incomingNumber
									+ ";;" + currentDate + ";;NULL;;\r\n";
							writeInLog(message);
						}
					});
					t.start();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	public void writeInLog(String message) {
		try {
			OutputStreamWriter fos = new OutputStreamWriter(
					context.openFileOutput("CallLog.txt", Context.MODE_APPEND));
			fos.append(message);
			fos.close();
			System.out.println("Writed in log succesfully");
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
		}

	}

}