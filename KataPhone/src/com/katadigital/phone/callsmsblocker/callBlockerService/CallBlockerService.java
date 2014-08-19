package com.katadigital.phone.callsmsblocker.callBlockerService;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.katadigital.phone.MainActivity;
import com.katadigital.phone.callsmsblocker.callListener.ServiceReciever;
import com.katadigital.phone.callsmsblocker.messageListener.SmsReceiver;
import com.katadigital.phone.callsmsblocker.notificationCenter.CallBlockerToastNotification;
import com.katadigital.phone.callsmsblocker.objects.BlockedContact;

public class CallBlockerService extends Service {

	public static final int notification_id = 111;

	// ---------------------------------------
	// Listening Services
	// ---------------------------------------
	private static ServiceReciever service;
	private static SmsReceiver sms;

	// ---------------------------------------
	// Data Structures
	// ---------------------------------------
	public static HashMap<String, BlockedContact> blackList;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		loadData();
		service = new ServiceReciever(getApplicationContext());
		sms = new SmsReceiver();
		registerReceiver(service, new IntentFilter(
				"android.intent.action.PHONE_STATE"));
		registerReceiver(sms, new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED"));
//		showStatusBarNotification("Call blocker is running now");
		System.out.println("Call blocker is running now");
	}

	@Override
	public void onDestroy() {
	
		service.stopListening();
		unregisterReceiver(service);
		unregisterReceiver(sms);
		service = null;
		sms = null;
		cancelStatusBarNotification();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public void showStatusBarNotification(String message) {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Intent i = new Intent(CallBlockerService.this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

		Notification noti = new NotificationCompat.Builder(this)
				.setContentTitle("Call blocker notification")
				.setWhen(System.currentTimeMillis()).setContentText(message)
				.setContentIntent(pi).build();
		// Notification noti =new NotificationCompat.Builder(this)
		// .setContentTitle("Call blocker notification")
		// .setWhen(System.currentTimeMillis())
		// .setContentText(message).setSmallIcon(R.drawable.running_not_icon)
		// .setContentIntent(pi).build();

		noti.flags |= Notification.FLAG_NO_CLEAR;

		manager.notify(notification_id, noti);
	}

	public void cancelStatusBarNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(notification_id);
	}

	public void loadData() {
		try {
			FileInputStream fis = openFileInput("CallBlocker.data");
			ObjectInputStream objeto = new ObjectInputStream(fis);
			blackList = (HashMap<String, BlockedContact>) objeto.readObject();
			fis.close();
			objeto.close();
		} catch (Exception e) {
			blackList = new HashMap<String, BlockedContact>();
			Log.e("Error", e.getMessage());
		}
	}

}
