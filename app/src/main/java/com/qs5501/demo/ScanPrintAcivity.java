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
 * Ⱥ��PDA ɨ��ʹ�ӡ ʾ������
 * ����װAPKʱ�����Installation failed with message Invalid File:����ʱ��
 * ����취���£�
 * 1.����������ϵ�Build�е�Clean Project
 * 2.�ٵ���������ϵ�Build�е�Rebulid Project!
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
				App.printText(1, 1, "�����Ǵ�ӡ��������1234567890\n");
			}
		});
	}
	
}
