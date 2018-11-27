package com.qs5501.service;

import android.graphics.Bitmap;
import android.os.RemoteException;
import com.qs5501.aidl.IQSService;

import com.qs.wiget.App;


/**
 * Created by Administrator on 2018-04-20.
 * 接口实现类
 */

public class IQSPDAImpl extends IQSService.Stub{

    @Override
    public void openScan() throws RemoteException {


        App.openScan();

    }
    
    @Override
    public void sendCMD(byte[] list) throws RemoteException {
    	// TODO Auto-generated method stub
    	App.send(list);
    }

    @Override
    public void printText(int size,int align,String text) throws RemoteException {

        App.printText(size,align,text);

    }

	@Override
	public void printBitmap(int align, Bitmap bitmap) throws RemoteException {
		// TODO Auto-generated method stub
		App.printBitmap(align, bitmap);
	}

	@Override
	public void printBarCode(int align, int width, int height, String data)
			throws RemoteException {
		// TODO Auto-generated method stub
		App.printBarCode(align,width,height,data);
	}

	@Override
	public void printQRCode(int width, int height, String data)
			throws RemoteException {
		// TODO Auto-generated method stub
		 App.printQRCode(width,height,data);
	}

}

