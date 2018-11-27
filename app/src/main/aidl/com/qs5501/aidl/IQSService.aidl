/**
* JIUI P1 ��ӡ����
* AIDL Version: 2.1
*/

package com.qs5501.aidl;

import android.graphics.Bitmap;

interface IQSService
{

    /**
    * ���豸ɨ��ͷ
    */
    void openScan();

	/**
	* ��ӡ���֣����ֿ����һ���Զ������Ű棬����һ���в���ӡ����ǿ�ƻ���
	* @param size: ��ӡ�����С��sizeΪ1ʱ��Ϊ�������壬sizeΪ2ʱ��Ϊ˫������
    * @param left����߾࣬��ӡͼƬ��ֽ����ߵľ���
	* @param text:	Ҫ��ӡ�������ַ���
	* @param align:	���ֶ��뷽ʽ
	*/
	void printText(int size,int align,String text);

	/**
	* ��ӡͼƬ
    * @param align:	���뷽ʽ  0 ����� ��1 ���ж��� ��2 �Ҷ���
	* @param bitmap: 	ͼƬbitmap����(�����384���أ������޷���ӡ���ұ��쳣)
	*/
	void printBitmap(int align,in Bitmap bitmap);

	/**
	* ��ӡһά����
    * @param @param align:	���뷽ʽ  0 ����� ��1 ���ж��� ��2 �Ҷ���
    * @param width��һά����Ŀ�� 58ֽ�������Ϊ384,80�����570
    * @param height��һά����ĸ߶�
    * @param data: 	һά�������(���������ģ���Ϊ������ᱨ��)
	*/
	void printBarCode(int align, int width, int height, String data);

	/**
	* ��ӡ��ά��
	* @param align:	���뷽ʽ  0 ����� ��1 ���ж��� ��2 �Ҷ���
    * @param width����ά��Ŀ�� 58ֽ�������Ϊ58
    * @param data: 	��ά�����
	*
	*/
	void printQRCode(int align, int height,String data);
	
   /**
	* ��������
    * @param data: 	ָ��byte����
	*
	*/
	void sendCMD(in byte[] list);


}