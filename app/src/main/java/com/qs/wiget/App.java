package com.qs.wiget;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.util.Log;
import android.widget.Toast;
import android.zyapi.CommonApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qs5501demo.aidl.R;

/**
 * Application class, please note that the entire code can only use the mPosApi variable here, you can not repeat the instantiation of PosApi, otherwise there will be printing delay or no printing.
 *  * Scan will scan twice or no scanning light, please note
 *
 * @author wsl
 *
 */
public class App extends Application {

	private static String mCurDev1 = "";

	private static int mComFd = -1;
	static CommonApi mCommonApi;

	static App instance = null;

	public static boolean isCanprint = false;

	public static boolean isCanSend = true;

	public static boolean temHigh = false;

	private final int MAX_RECV_BUF_SIZE = 1024;
	private boolean isOpen = false;
	private MediaPlayer player;
	private final static int SHOW_RECV_DATA = 1;
	private byte[] recv;
	private String strRead;
	public static boolean isScanDomn = false;
	// GreenOnReceiver greenOnReceiver;
	private static String pin_1 = "55";// 一维
	private static String pin_2 = "56";// 二维

	public static StringBuffer sb1 = new StringBuffer();

	// SCAN button monitor
	private ScanBroadcastReceiver scanBroadcastReceiver;

	Handler h;

	public App() {
		super.onCreate();
		instance = this;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// mDb = Database.getInstance(this);
		// initial
		init();

		// Instantiate MediaPlayer
		player = MediaPlayer.create(getApplicationContext(), R.raw.beep);
	}

	public void init() {

		openGPIO();
		initGPIO();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mComFd > 0) {
					open();
					isOpen = true;
					readData();
					// Black mark is turned off by default
					App.send(new byte[] { 0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04,
							0x05, 0x06, 0x66 });
				} else {
					isOpen = false;
				}
			}
		}, 2000);

		// Update the UI with Handler
		h = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					if (msg.obj != null) {
						String str = "" + msg.obj;
						if (!str.trim().contains("##55")) {
							if (!str.trim().equals("start")) {

								player.start();

								Intent intentBroadcast = new Intent();
								intentBroadcast.setAction("com.qs.scancode");

								intentBroadcast.putExtra("code", str.trim());

								sendBroadcast(intentBroadcast);

							}
						}
					}
				}
			}
		};

		scanBroadcastReceiver = new ScanBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("ismart.intent.scandown");
		this.registerReceiver(scanBroadcastReceiver, intentFilter);
	}

	/**
	 * Read data thread
	 */
	private void readData() {
		new Thread() {
			public void run() {
				while (isOpen) {
					int ret = 0;
					byte[] buf = new byte[MAX_RECV_BUF_SIZE + 1];
					ret = mCommonApi.readComEx(mComFd, buf, MAX_RECV_BUF_SIZE,
							0, 0);
					if (ret <= 0) {
						Log.d("", "read failed!!!! ret:" + ret);
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					} else {
						// Log.e("", "1read success:");
					}
					recv = new byte[ret];
					System.arraycopy(buf, 0, recv, 0, ret);

					try {
						strRead = new String(recv, "GBK");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					StringBuffer sb = new StringBuffer();

					String str = byteToString(buf, ret);

					if (str.contains("1C 00 0C 0F")) {
						Intent mIntent = new Intent("NOPAPER");
						instance.sendBroadcast(mIntent);
						isCanprint = false;
						return;
					} else {
						isCanprint = true;
					}

					for (int i = 0; i < recv.length; i++) {
						if (recv[i] == 0x0D) {
							sb.append("\n");
						} else {
							sb.append((char) recv[i]);
						}
					}

					String s = sb.toString();
					if (strRead != null) {
						Message msg = handler.obtainMessage(SHOW_RECV_DATA);
						msg.obj = s;
						msg.sendToTarget();
					}
				}
			}
		}.start();
	}

	boolean iscanScan = false;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case SHOW_RECV_DATA:
					String barCodeStr1 = (String) msg.obj;
					Log.e("", "1read success:" + barCodeStr1);
					if (barCodeStr1.trim() != "") {
						if (isOpen) {
							if (!barCodeStr1.trim().contains("##55")) {
								if (!barCodeStr1.trim().equals("start")) {
									if (barCodeStr1.trim().length() != 0) {

										Message m = new Message();
										m.what = 0x123;
										m.obj = barCodeStr1;
										h.sendMessage(m);
									}

								}
							}
						}
					}
					break;
			}
		};
	};

	int num = 1;
	Handler mHanlder = new Handler();
	Runnable run_getData = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (num > 1) {
				num = 1;
				mHanlder.removeCallbacks(run_getData);
				Message m = new Message();
				m.what = 0x123;
				Log.e("iiiiiii", "Send a GET request");
				try {
					m.obj = sb1.toString();
					Log.e("returned messages：", "" + m.obj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				h.sendMessage(m);
			} else {
				num++;
				mHanlder.postDelayed(run_getData, 100);
			}
		}
	};

	// Go to App Lago 55 and 56 feet
	public static void open() {
		/**
		 * 1. Pull up 55, 56 feet (APP->Printer) 1B 23 23 XXXX where XXXX is ASCII code: 56UP ie 1B 23 23
		 * 35 36 55 50 The microcontroller receives a pull-up 55, 56-pin level
		 */
		// Come in and pull up 55 and 56 feet
		App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x36, 0x55, 0x50 });

		// Pull down 55 feet when scanning
		// App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x35, 0x44, 0x4E });

	}

	// Perform a scan, that is, pull the 74, 75 feet down and then pull up
	public static void openScan() {
		/**
		 * 3. Pull down the 55-pin level (APP->Printer) 1B 23 23 XXXX where XXXX is
		 * ASCII code: 55DN is 1B 23 23 35 * 35 44 4E
		 */
		// Come in and pull up 55 and 56 feet
		// App.send(new byte[]{0x1B,0x23,0x23,0x35,0x36,0x55,0x50});
		// Pull down 55 feet when scanning
		// App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x35, 0x44, 0x4E });

		// Send instruction
		App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x35, 0x44, 0x4E });

		//Pull down the GPIO port
		mCommonApi.setGpioDir(74, 1);
		mCommonApi.setGpioOut(74, 0);
		mCommonApi.setGpioDir(75, 1);
		mCommonApi.setGpioOut(75, 0);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// Pull down the GPIO port and light the scan head
				mCommonApi.setGpioDir(74, 1);
				mCommonApi.setGpioOut(74, 1);
				mCommonApi.setGpioDir(75, 1);
				mCommonApi.setGpioOut(75, 1);

			}
		}, 50);

		handler1.removeCallbacks(run1);
		handler1.postDelayed(run1, 3000);
	}

	static boolean isScan = false;
	static Handler handler1 = new Handler();
	static Runnable run1 = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (isScan) {
				// Forced to close the scan head
				mCommonApi.setGpioDir(74, 1);
				mCommonApi.setGpioOut(74, 0);
				mCommonApi.setGpioDir(75, 1);
				mCommonApi.setGpioOut(75, 0);

				isScan = true;
			}
		}
	};

	public static App getInstance() {
		if (instance == null) {
			instance = new App();
		}
		return instance;
	}

	public String getCurDevice() {
		return mCurDev1;
	}

	public static void setCurDevice(String mCurDev) {
		mCurDev1 = mCurDev;
	}

		// Reference mCommonApi variable elsewhere
	public static CommonApi getCommonApi() {
		return mCommonApi;
	}

	public static void initGPIO() {
		// TODO Auto-generated method stub
		// 5501 for MT1 and 408 for MT3
		mComFd = mCommonApi.openCom("/dev/ttyMT1", 115200, 8, 'N', 1);
		// mComFd = mCommonApi.openCom("/dev/ttyMT3", 115200, 8, 'N', 1);

//		if (mComFd > 0) {
//			Toast.makeText(instance, "init success", 0).show();
//		}
	}

	public static void openGPIO() {

		mCommonApi = new CommonApi();

		mCommonApi.setGpioDir(84, 0);
		mCommonApi.getGpioIn(84);

		mCommonApi.setGpioDir(84, 1);
		mCommonApi.setGpioOut(84, 1);

	}

	static Handler mHandler = new Handler();
	Runnable mRun = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isCanSend = true;
		}
	};

	public String byteToString(byte[] b, int size) {
		byte high, low;
		byte maskHigh = (byte) 0xf0;
		byte maskLow = 0x0f;

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < size; i++) {
			high = (byte) ((b[i] & maskHigh) >> 4);
			low = (byte) (b[i] & maskLow);
			buf.append(findHex(high));
			buf.append(findHex(low));
			buf.append(" ");
		}
		return buf.toString();
	}

	private char findHex(byte b) {
		int t = new Byte(b).intValue();
		t = t < 0 ? t + 16 : t;
		if ((0 <= t) && (t <= 9)) {
			return (char) (t + '0');
		}
		return (char) (t - 10 + 'A');
	}

	/**
	 * See if a string can be converted to a number
	 *
	 * @param str
	 *            String
	 * @return true can; false No
	 */
	public static boolean isStr2Num(String str) {
		Pattern pattern = Pattern.compile("^[0-9]*$");
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * send data
	 */
	public static void send(byte[] data) {
		if (data == null)
			return;
		if (mComFd > 0) {
			mCommonApi.writeCom(mComFd, data, data.length);
		}

	}

	private static boolean isMessyCode(String strName) {
		try {
			Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
			Matcher m = p.matcher(strName);
			String after = m.replaceAll("");
			String temp = after.replaceAll("\\p{P}", "");
			char[] ch = temp.trim().toCharArray();

			int length = (ch != null) ? ch.length : 0;
			for (int i = 0; i < length; i++) {
				char c = ch[i];
				if (!Character.isLetterOrDigit(c)) {
					String str = "" + ch[i];
					if (!str.matches("[\u4e00-\u9fa5]+")) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public String deleteErr(String str_VarMboxRead) {

		String b = str_VarMboxRead.replace("�", "");
		b = b.replace("", "");

		return b.trim();
	}

	public static void closeCommonApi() {

//		handler1.removeCallbacks(run1);
//
//		isScan = false;
//
////		mCommonApi.setGpioMode(84, 0);
////		mCommonApi.setGpioDir(84, 0);
//		mCommonApi.setGpioOut(84, 0);
//		mCommonApi.closeCom(mComFd);

		if (mComFd > 0) {
			mCommonApi.setGpioMode(84, 0);
			mCommonApi.setGpioDir(84, 0);
			mCommonApi.setGpioOut(84, 0);
			mCommonApi.closeCom(mComFd);
		}

	}

	/**
	 * 打印文字
	 *
	 */
	public static void printText(int size, int align, String text) {

		switch (align) {
			case 0:
				send(new byte[] { 0x1b, 0x61, 0x00 });
				break;
			case 1:
				send(new byte[] { 0x1b, 0x61, 0x01 });
				break;
			case 2:
				send(new byte[] { 0x1b, 0x61, 0x02 });
				break;

			default:
				break;
		}
		switch (size) {
			case 1:
				send(new byte[] { 0x1D, 0x21, 0x00 });
				break;
			case 2:
				send(new byte[] { 0x1D, 0x12, 0x11 });
				break;

			default:
				break;
		}
		// print
		try {
			send((text).getBytes("GBK"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Print picture
	 *
	 * @param align
	 * @param bitmap
	 */
	public static void printBitmap(int align, Bitmap bitmap) {
		switch (align) {
			case 0:
				send(new byte[] { 0x1b, 0x61, 0x00 });
				break;
			case 1:
				send(new byte[] { 0x1b, 0x61, 0x01 });
				break;
			case 2:
				send(new byte[] { 0x1b, 0x61, 0x02 });
				break;

			default:
				break;
		}

		byte[] b = draw2PxPoint(bitmap);
		send(b);

	}

	/**
	 * Print one-dimensional code
	 *
	 * @param align
	 * @param width
	 * @param height
	 * @param data
	 */
	public static void printBarCode(int align, int width, int height,
									String data) {
		switch (align) {
			case 0:
				send(new byte[] { 0x1b, 0x61, 0x00 });
				break;
			case 1:
				send(new byte[] { 0x1b, 0x61, 0x01 });
				break;
			case 2:
				send(new byte[] { 0x1b, 0x61, 0x02 });
				break;

			default:
				break;
		}

		Bitmap mBitmap = BarcodeCreater.creatBarcode(getInstance(), data,
				width, height, true, 1);
		byte[] printData = draw2PxPoint(mBitmap);
		send(printData);

	}

	/**
	 * Print QR code
	 *
	 * @param align
	 * @param
	 * @param height
	 * @param data
	 */
	public static void printQRCode(int align, int height, String data) {
		switch (align) {
			case 0:
				send(new byte[] { 0x1b, 0x61, 0x00 });
				break;
			case 1:
				send(new byte[] { 0x1b, 0x61, 0x01 });
				break;
			case 2:
				send(new byte[] { 0x1b, 0x61, 0x02 });
				break;

			default:
				break;
		}

		// Bitmap mBitmap = BarcodeCreater.encode2dAsBitmap(data, height,
		// height,
		// 2);

		Bitmap mBitmap = createQRImage(data, height, height);

		Bitmap textBitmap = word2bitmap(data,mBitmap.getWidth());

		mBitmap = twoBtmap2One(mBitmap, textBitmap);

		byte[] printData1 = draw2PxPoint(mBitmap);

		send(printData1);

		send(new byte[] { 0x1d, 0x0c });
	}

	// SCAN button monitoring
	class ScanBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			openScan();
		}
	}

	/**
	 * Text to picture
	 *
	 * @param str
	 * @return
	 */
	public static Bitmap word2bitmap(String str,int width) {

		Bitmap bMap = Bitmap.createBitmap(width, 80, Config.ARGB_8888);
		Canvas canvas = new Canvas(bMap);
		canvas.drawColor(Color.WHITE);
		TextPaint textPaint = new TextPaint();
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(21.0F);
		StaticLayout layout = new StaticLayout(str, textPaint, bMap.getWidth(),
				Alignment.ALIGN_NORMAL, (float) 1.0, (float) 0.0, true);
		layout.draw(canvas);

		return bMap;

	}

	/**
	 * Combine two pictures into one
	 *
	 * @param bitmap1
	 * @param bitmap2
	 * @return
	 */
	public static Bitmap twoBtmap2One(Bitmap bitmap1, Bitmap bitmap2) {
		Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth(),
				bitmap1.getHeight() + bitmap2.getHeight(), bitmap1.getConfig());
		Canvas canvas = new Canvas(bitmap3);
		canvas.drawBitmap(bitmap1, new Matrix(), null);
		canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
		return bitmap3;
	}

	/**
	 * Generate QR code The address or string to be converted, which can be Chinese
	 *
	 * @param url
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap createQRImage(String url, int width, int height) {
		try {
			// Judging URL legality
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "GBK");
			// Image data conversion using matrix conversion
			BitMatrix bitMatrix = new QRCodeWriter().encode(url,
					BarcodeFormat.QR_CODE, width, height, hints);
			// bitMatrix = deleteWhite(bitMatrix);// 删除白边
			bitMatrix = deleteWhite(bitMatrix);// 删除白边
			width = bitMatrix.getWidth();
			height = bitMatrix.getHeight();
			int[] pixels = new int[width * height];
			// Here, according to the algorithm of the two-dimensional code, the images of the two-dimensional code are generated one by one.
			// Two for loops are the result of a picture scan
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			// Generate a QR image format using ARGB_8888
			Bitmap bitmap = Bitmap
					.createBitmap(width, height, Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static BitMatrix deleteWhite(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1]))
					resMatrix.set(i, j);
			}
		}
		return resMatrix;
	}

	/*************************************************************************
	 * Suppose a 240*240 image with a resolution of 24 is printed in 10 lines. Each line is a 240*24 dot matrix. Each column has 24 points and is stored in 3 bytes.
	 * Each byte stores 8 pixel point information. Since there are only black and white colors, the bit corresponding to 1 is black, and the bit corresponding to 0 is white.
	 **************************************************************************/
	/**
	 * Convert a Bitmap image into a byte stream that the printer can print
	 *
	 * @param bmp
	 * @return
	 */
	public static byte[] draw2PxPoint(Bitmap bmp) {
		// Used to store the converted bitmap data. Why do you want to add another 1000, this is to cope with the height of the picture?
		// Divide the situation at 24 o'clock. For example, the bitmap resolution is 240 * 250, which takes up 7500 bytes.
		// But actually you want to store 11 rows of data, each row requires 24 * 240 / 8 = 720bytes of space. Plus some instruction storage overhead,
		// Therefore, it is safe to apply for 1000 bytes of space, otherwise the runtime will throw an exception that the array accesses the boundary.
		int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
		byte[] data = new byte[size];
		int k = 0;
		// Set the instruction with line spacing 0
		data[k++] = 0x1B;
		data[k++] = 0x33;
		data[k++] = 0x00;
		// Line by line
		for (int j = 0; j < bmp.getHeight() / 24f; j++) {
			// Print image instructions
			data[k++] = 0x1B;
			data[k++] = 0x2A;
			data[k++] = 33;
			data[k++] = (byte) (bmp.getWidth() % 256); // nL
			data[k++] = (byte) (bmp.getWidth() / 256); // nH
			// For each line, print column by column
			for (int i = 0; i < bmp.getWidth(); i++) {
				// 24 pixels per column, divided into 3 bytes of storage
				for (int m = 0; m < 3; m++) {
					// Each byte represents 8 pixels, 0 for white and 1 for black
					for (int n = 0; n < 8; n++) {
						byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
						if (k < size) {
							data[k] += data[k] + b;
						}
						// data[k] = (byte) (data[k]+ data[k] + b);
					}
					k++;
				}
			}
			if (k < size) {
				data[k++] = 10;// Wrap
			}
		}
		return data;
	}

	/**
	 * Grayscale image black and white, black is 1, white is 0
	 *
	 * @param x
	 *            Abscissa
	 * @param y
	 *            Y-axis
	 * @param bit
	 *            bitmap
	 * @return
	 */
	public static byte px2Byte(int x, int y, Bitmap bit) {
		if (x < bit.getWidth() && y < bit.getHeight()) {
			byte b;
			int pixel = bit.getPixel(x, y);
			int red = (pixel & 0x00ff0000) >> 16; // Take two high
			int green = (pixel & 0x0000ff00) >> 8; // Take two
			int blue = pixel & 0x000000ff; // Take two lower
			int gray = RGB2Gray(red, green, blue);
			if (gray < 128) {
				b = 1;
			} else {
				b = 0;
			}
			return b;
		}
		return 0;
	}

	/**
	 * Image grayscale conversion
	 */
	private static int RGB2Gray(int r, int g, int b) {
		int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // Gray scale conversion formula
		return gray;
	}

}
