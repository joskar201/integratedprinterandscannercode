package com.qs5501.demo;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.qs.wiget.App;
import com.qs5501demo.aidl.R;

public class MainPrinterActivity extends Activity {

	private ScanBroadcastReceiver scanBroadcastReceiver;

	private EditText tv;

	String str_massage;

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initView();

		scanBroadcastReceiver = new ScanBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.qs.scancode");
		this.registerReceiver(scanBroadcastReceiver, intentFilter);

	}

	/**
	 * Control initialization and button monitoring
	 */
	private void initView() {
		// TODO Auto-generated method stub
		tv = (EditText) findViewById(R.id.tv);

		// scanning
		findViewById(R.id.btn_scan1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				App.openScan();

			}
		});

		// Print text
		findViewById(R.id.btn_printText).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						str_massage = tv.getText().toString();
						printeText(str_massage);
					}
				});

		// Print one-dimensional code
		findViewById(R.id.btn_printBarcode).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						printBarCode();
					}
				});

		// Print QR code
		findViewById(R.id.btn_printQRcode).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						printQrCode();
					}
				});

		// Save content
		findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveData();
			}
		});

		// Get permission, this operation is required on the machine of Android 6.0 or above,
		// otherwise there will be cases where the SD card cannot be read or written.
//		verifyStoragePermissions(this);
	}

	// Print text
	private void printeText(String str) {
		App.printText(1, 0, str + "\n");
	}

	// Print barcode
	private void printBarCode() {
		// Get the string in the edit box
		str_massage = tv.getText().toString().trim();
		if (str_massage == null || str_massage.length() <= 0)
			return;

		// Determine whether the current character can generate a barcode
		if (str_massage.getBytes().length > str_massage.length()) {
			Toast.makeText(MainPrinterActivity.this, "Current data cannot generate one-dimensional code",
					Toast.LENGTH_SHORT).show();
			return;
		}

		App.printBarCode(0, 380, 100, str_massage);
	}

	// Print QR code
	private void printQrCode() {
		// Get the string in the edit box
		str_massage = tv.getText().toString().trim();
		if (str_massage == null || str_massage.length() <= 0)
			return;
		App.printQRCode(0, 300, str_massage);
	}

	// Save content to SD card
    //TODO we can use this method to make a call to a persistence layer service (either local or server)
	private void saveData() {
		// Get the string in the edit box
		str_massage = tv.getText().toString().trim();
		// Read saved data
		String str = readSDFile();

		// Data saving
		saveDada2SD(str + "\n" + str_massage);
	}

	/**
	 *
	 * Read the text file in the SD card
	 *
	 * @param
	 *
	 * @return
	 */
	@SuppressWarnings("resource")
	public String readSDFile() {
		try {
			File file = new File("/mnt/sdcard/pro.txt");
			FileInputStream is = new FileInputStream(file);
			byte[] b = new byte[is.available()];
			is.read(b);
			String result = new String(b);
			System.out.println("读取成功：" + result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	// Save to SD card
	public void saveDada2SD(String sb) {
		String filePath = null;
		boolean hasSDCard = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (hasSDCard) { //SD card root directory hello.text
			filePath = "/mnt/sdcard/pro.txt";
		}
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				File dir = new File(file.getParent());
				dir.mkdirs();
				file.createNewFile();
			}
			FileOutputStream fileOut = null;
			BufferedOutputStream writer = null;
			OutputStreamWriter outputStreamWriter = null;
			BufferedWriter bufferedWriter = null;
			try {
				fileOut = new FileOutputStream(file);
				writer = new BufferedOutputStream(fileOut);
				outputStreamWriter = new OutputStreamWriter(writer, "UTF-8");
				bufferedWriter = new BufferedWriter(outputStreamWriter);
				bufferedWriter.write(new String(sb.toString()));
				bufferedWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Toast.makeText(this, "Saved successfully, please check the SD card, the file name is pro.txt", Toast.LENGTH_SHORT)
				.show();
	}

//	//
//Check read and write permissions
//	public void verifyStoragePermissions(Activity activity) {
//		try {
//			// Check if there is write permission
//			int permission = ActivityCompat.checkSelfPermission(activity,
//					"android.permission.WRITE_EXTERNAL_STORAGE");
//			if (permission != PackageManager.PERMISSION_GRANTED) {
//				// No permission to write, to apply for permission to write, a dialog box will pop up
//				ActivityCompat.requestPermissions(activity,
//						PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	class ScanBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String text1 = intent.getExtras().getString("code");
			tv.setText(text1);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(scanBroadcastReceiver);
		App.closeCommonApi();
		System.exit(0);
		super.onDestroy();
	}

}
