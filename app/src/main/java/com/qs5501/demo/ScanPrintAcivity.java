package com.qs5501.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.qs.wiget.App;
import com.qs5501.service.QSService;
import com.qs5501demo.aidl.R;

/**
 * 群索PDA 扫描和打印 示范例子
 * 当安装APK时候出现Installation failed with message Invalid File:问题时，
 * 解决办法如下：
 * 1.点击工具栏上的Build中的Clean Project
 * 2.再点击工具栏上的Build中的Rebulid Project!
 * @author wsl
 *
 */
public class ScanPrintAcivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_layout1);

		Intent mIntent = new Intent(this,QSService.class);
		this.startService(mIntent);
		
		findViewById(R.id.scan).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				App.openScan();
				App.printText(1, 1, "这里是打印测试数据1234567890\n");
			}
		});
	}
	
}
