package com.katadigital.phone.helpers;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;

public class MemoryManager {

	public boolean checkSdCardMemmory(long desiredMemory) {
		boolean result = true;

		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		long bytesAvailable = (long) stat.getBlockSize()
				* (long) stat.getAvailableBlocks();
		long megAvailable = bytesAvailable / 1048576;

		if (megAvailable < desiredMemory) {
			result = false;
		}

		return result;
	}

	public boolean checkInternalMemmory(long desiredMemory) {
		boolean result = true;

		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		long bytesAvailable = (long) stat.getBlockSize()
				* (long) stat.getAvailableBlocks();
		long megAvailable = bytesAvailable / 1048576;

		if (megAvailable < desiredMemory) {
			result = false;
		}

		return result;
	}

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public void createDIRNoMediaFile(String mainfolder, String subfolder,
			boolean isSdCard) {

		if (isSdCard == true) {

			File maindirect = new File(
					Environment.getExternalStorageDirectory() + mainfolder);
			if (!maindirect.exists()) {
				if (maindirect.mkdir())
					; // directory is created;

				System.out.println("Main folder created");
				File nomedia = new File(
						Environment.getExternalStorageDirectory() + mainfolder,
						".nomedia");

				try {
					nomedia.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (nomedia.exists()) {
					System.out.println("Nomedia is existing in mainfolder!!");
				}
			} else {
				System.out.println("main folder exist");

			}

			File direct = new File(Environment.getExternalStorageDirectory()
					+ subfolder);
			if (!direct.exists()) {
				if (direct.mkdir())
					; // directory is created;
				System.out.println("sub folder created");
				File nomedia = new File(
						Environment.getExternalStorageDirectory() + subfolder,
						".nomedia");

				try {
					nomedia.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (nomedia.exists()) {
					System.out.println("Nomedia is existing in subfolder!!");
				}
			} else {
				System.out.println("subfolder exist");

			}
		} else {
			File direct = new File(Environment.getDataDirectory() + subfolder);
			if (!direct.exists()) {
				if (direct.mkdir())
					; // directory is created;
				System.out.println("folder created");
				File nomedia = new File(Environment.getDataDirectory()
						+ subfolder, ".nomedia");

				try {
					nomedia.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (nomedia.exists()) {
					System.out.println("Nomedia is existing in Internal!!");
				}
			} else {
				System.out.println("folder exist");

			}
		}

	}

	public void createDirectory() {

	}

}
