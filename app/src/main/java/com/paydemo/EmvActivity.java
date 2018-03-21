package com.paydemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;
import vpos.keypad.EMVCOHelper;

public class EmvActivity extends Activity {

    private TextView txt_Tip;
    private TextView txt_Result;
    private final int TYPE_TEST_EMV = 1;
    private final int TYPE_PIN_BLOCK = 2;
    private final int TYPE_SHOW_PAD = 3;
    private String strEmvStatus = "";
    private String QpayPwd="";      //闪付联机密码

    private EmvThread emvThread=null;
    private boolean m_bThreadFinished = true;

    private EMVCOHelper emvcoHelper = EMVCOHelper.getInstance();

    public static final String TAG=EmvActivity.class.getSimpleName();

    byte[] TermParabuf = {
            (byte) 0xDF, 0x18, 0x07, (byte) 0xF4, (byte) 0xE0, (byte) 0xF8, (byte) 0xE4, (byte) 0xEF, (byte) 0xF2, (byte) 0xA0, (byte) 0x9F, 0x35, 0x01, 0x22, (byte) 0x9F, 0x33, 0x03, (byte) 0xE0, 0x40, 0x00, (byte) 0x9F, 0x40, 0x05, 0x60,
            0x00, (byte) 0xF0, (byte) 0xF0, 0x01, (byte) 0xDF, 0x19, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xDF, 0x26, 0x0F, (byte) 0x9F, 0x02, 0x06, 0x5F, 0x2A, 0x02,
            (byte) 0x9A, 0x03, (byte) 0x9C, 0x01, (byte) 0x95, 0x05, (byte) 0x9F, 0x37, 0x04, (byte) 0xDF, 0x40, 0x01, (byte) 0xFF, (byte) 0x9F, 0x39, 0x01, 0x05, (byte) 0x9F, 0x1A, 0x02, 0x01, 0x56, (byte) 0x9F, 0x1E,
            0x08, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0xDF, 0x42, 0x01, 0x00, (byte) 0xDF, 0x43, 0x01, 0x00, (byte) 0xDF, 0x44, 0x01, 0x00, (byte) 0xDF, 0x45, 0x01,
            0x00, (byte) 0xDF, 0x46, 0x01, 0x01, (byte) 0x9F, 0x66, 0x04, 0x74, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xDF, 0x47, 0x05, (byte) 0xAF, 0x61, (byte) 0xFF, 0x0C, 0x07
    };

    byte[] aid1buf = {
            (byte) 0x9F, 0x06, 0x07, (byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10, (byte) 0x9F, 0x01, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x9F, 0x09, 0x02, 0x00,
            0x20, (byte) 0x9F, 0x15, 0x02, 0x00, 0x01, (byte) 0x9F, 0x16, 0x08, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x9F, 0x4E, 0x12, (byte) 0xD2, (byte) 0xF8, (byte) 0xC1,
            (byte) 0xAA, (byte) 0xC9, (byte) 0xCC, (byte) 0xCE, (byte) 0xF1, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xB7, (byte) 0xD6, (byte) 0xB9, (byte) 0xAB, (byte) 0xCB, (byte) 0xBE, (byte) 0xDF, 0x11, 0x05, (byte) 0xCC, 0x00, 0x00, 0x00,
            0x00, (byte) 0xDF, 0x13, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xDF, 0x12, 0x05, (byte) 0xCC, 0x00, 0x00, 0x00, 0x00, (byte) 0xDF, 0x14, 0x03, (byte) 0x9f, 0x37, 0x04,
            (byte) 0xDF, 0x15, 0x04, 0x00, 0x00, (byte) 0x9C, 0x40, (byte) 0xDF, 0x16, 0x01, 0x32, (byte) 0xDF, 0x17, 0x01, 0x14, (byte) 0xDF, 0x18, 0x01, 0x01, (byte) 0x9F, 0x1B, 0x04, 0x00,
            0x01, (byte) 0x86, (byte) 0xA0, (byte) 0x9F, 0x1C, 0x08, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, 0x5F, 0x2A, 0x02, 0x01, 0x56, 0x5F, 0x36, 0x01, 0x02,
            (byte) 0x9F, 0x3C, 0x02, 0x01, 0x56, (byte) 0x9F, 0x3D, 0x01, 0x02, (byte) 0x9F, 0x1D, 0x01, 0x01, (byte) 0xDF, 0x01, 0x01, 0x00, (byte) 0xDF, 0x19, 0x06, 0x00, 0x00, 0x00,
            0x05, 0x00, 0x00, (byte) 0xDF, 0x20, 0x06, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, (byte) 0xDF, 0x21, 0x06, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, (byte) 0x9F, 0x7B,
            0x06, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00
    };

    byte[] capk1buf = {
            (byte) 0x9f, 0x22, 0x01, (byte) 0x80, (byte) 0x9f, 0x06, 0x05, (byte) 0xa0,
            0x00, 0x00, 0x03, 0x33, (byte) 0xdf, 0x05, 0x08, 0x32,
            0x30, 0x31, 0x38, 0x30, 0x35, 0x30, 0x31, (byte) 0xdf,
            0x06, 0x01, 0x01, (byte) 0xdf, 0x07, 0x01, 0x01, (byte) 0xdf,
            0x04, 0x03, 0x01, 0x00, 0x01, (byte) 0xdf, 0x03, 0x14,
            (byte) 0xa5, (byte) 0xe4, 0x4b, (byte) 0xb0, (byte) 0xe1, (byte) 0xfa, 0x4f, (byte) 0x96,
            (byte) 0xa1, 0x17, 0x09, 0x18, 0x66, 0x70, (byte) 0xd0, (byte) 0x83,
            0x50, 0x57, (byte) 0xd3, 0x5e, (byte) 0xdf, 0x02, (byte) 0x81, (byte) 0x80,
            (byte) 0xcc, (byte) 0xdb, (byte) 0xa6, (byte) 0x86, (byte) 0xe2, (byte) 0xef, (byte) 0xb8, 0x4c,
            (byte) 0xe2, (byte) 0xea, 0x01, 0x20, (byte) 0x9e, (byte) 0xeb, 0x53, (byte) 0xbe,
            (byte) 0xf2, 0x1a, (byte) 0xb6, (byte) 0xd3, 0x53, 0x27, 0x4f, (byte) 0xf8,
            0x39, 0x1d, 0x70, 0x35, (byte) 0xd7, 0x6e, 0x21, 0x56,
            (byte) 0xca, (byte) 0xed, (byte) 0xd0, 0x75, 0x10, (byte) 0xe0, 0x7d, (byte) 0xaf,
            (byte) 0xca, (byte) 0xca, (byte) 0xbb, 0x7c, (byte) 0xcb, 0x09, 0x50, (byte) 0xba,
            0x2f, 0x0a, 0x3c, (byte) 0xec, 0x31, 0x3c, 0x52, (byte) 0xee,
            0x6c, (byte) 0xd0, (byte) 0x9e, (byte) 0xf0, 0x04, 0x01, (byte) 0xa3, (byte) 0xd6,
            (byte) 0xcc, 0x5f, 0x68, (byte) 0xca, 0x5f, (byte) 0xcd, 0x0a, (byte) 0xc6,
            0x13, 0x21, 0x41, (byte) 0xfa, (byte) 0xfd, 0x1c, (byte) 0xfa, 0x36,
            (byte) 0xa2, 0x69, 0x2d, 0x02, (byte) 0xdd, (byte) 0xc2, 0x7e, (byte) 0xda,
            0x4c, (byte) 0xd5, (byte) 0xbe, (byte) 0xa6, (byte) 0xff, 0x21, (byte) 0x91, 0x3b,
            0x51, 0x3c, (byte) 0xe7, (byte) 0x8b, (byte) 0xf3, 0x3e, 0x68, 0x77,
            (byte) 0xaa, 0x5b, 0x60, 0x5b, (byte) 0xc6, (byte) 0x9a, 0x53, 0x4f,
            0x37, 0x77, (byte) 0xcb, (byte) 0xed, 0x63, 0x76, (byte) 0xba, 0x64,
            (byte) 0x9c, 0x72, 0x51, 0x6a, 0x7e, 0x16, (byte) 0xaf, (byte) 0x85
    };

    class EmvThread extends Thread {

        int type = 0;

        EmvThread(int type) {
            this.type = type;
        }

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public void run() {
            synchronized (this) {

                m_bThreadFinished = false;
                int ret = 0;

                switch (type) {
                    case TYPE_TEST_EMV:

                        int mCardType=-1;

                        byte picc_mode = 'M';
                        byte cardtype[] = new byte[3];
                        byte serialNo[] = new byte[50];

                        long time = System.currentTimeMillis();
                        while (System.currentTimeMillis() < time + 30*1000) {
                            if(!MainActivity.qpayFlag) {
                                ret = PosApiHelper.getInstance().IccCheck((byte)0);
                                if (ret == 0) {
                                    mCardType=1;
                                    Log.e("xx", "检测到IC卡");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            txt_Tip.setText("正在读IC卡，请稍等...");
                                        }
                                    });
                                    break;
                                }
                            }

                            ret = PosApiHelper.getInstance().PiccOpen();
                            if (0 == ret) {
                                ret = PosApiHelper.getInstance().PiccCheck(picc_mode, cardtype, serialNo);
                                if(ret==0){
                                    mCardType=2;
                                    Log.e("xx", "检测到非接卡");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            txt_Tip.setText("正在读非接卡，请稍等...");
                                        }
                                    });
                                    break;
                                }
                            }
                        }

                        if(mCardType==-1){
                            Log.e("xx", "检卡超时");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    txt_Tip.setText("检卡超时，请重新交易");
                                }
                            });
                            return;
                        }

                        ret = emvcoHelper.EmvKeyPadInit(EmvActivity.this);
                        if (ret != 0) {
                            m_bThreadFinished = true;
                            return;
                        }

                        emvcoHelper.EmvEnvParaInit();
                        emvcoHelper.EmvSaveTermParas(TermParabuf, TermParabuf.length);
                        emvcoHelper.EmvAddOneAIDS(aid1buf, aid1buf.length);
                        emvcoHelper.EmvKernelInit(0, 3);
                        emvcoHelper.EmvAddOneCAPK(capk1buf, capk1buf.length);

                        emvcoHelper.EmvSetTransType(2);
                        emvcoHelper.EmvSetTransAmount(1);
                        emvcoHelper.EmvSetCardType(mCardType); //1--CONTACT 2--CONTACTLESS
                        Log.e("EMV", "EMV TEST");

                        if(MainActivity.qpayFlag) {     //云闪付
                            ret = emvcoHelper.EmvProcess(2, 1); ////1--CONTACT 2--CONTACTLESS

                            //提示输入密码，如果是免密商户，就不需要输入密码
                            final byte[] pwd = new byte[20];

                            //emvcoHelper.EmvKeyPadInit(EmvTestActivity.this);
                            ret = emvcoHelper.EmvShowKeyPad(EmvActivity.this, pwd);

                            if (ret != 0) {
                                Log.e("xx","ShowPad ret :" +ret);
                                m_bThreadFinished = true;
                                return;
                            }

                            Log.e("xx","ShowPad ***********");

                            QpayPwd = ByteUtil.bytesToString(pwd);
                            Log.e("xx", "QpayPwd="+QpayPwd);

                        }
                        else {
                            ret = emvcoHelper.EmvProcess(mCardType, 0); ////1--CONTACT 2--CONTACTLESS
                        }
                        Log.e("EMV Process", "ret = " + ret);

                        if (ret < 0) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    strEmvStatus = "EMV Termination";
                                    txt_Tip.setText("读卡失败，请重试");
                                }
                            });

                            m_bThreadFinished = true;
                            return;

                        } else if (ret == 3) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    strEmvStatus = "EMV  GOONLINE";
                                }
                            });
                        }

                        PosTrade posTrade = new PosTrade(EmvActivity.this);

                        if(MainActivity.qpayFlag){     //云闪付
                            posTrade.cardMode = "CARD_RF";
                        }
                        else{
                            posTrade.cardMode = "CARD_IC";
                        }

                        //卡号
                        byte tagData[] = new byte[256];
                        int len = emvcoHelper.EmvGetTagData(tagData, 20, (short) 0x5A);
                        final byte[] cardNo = new byte[len];
                        Maths.byteArrayCpy(cardNo,0,tagData,0,len);
                        Log.e("xx", "cardNo="+Maths.bytesToHexString(cardNo));
                        posTrade.cardNo = Maths.bytesToHexString(cardNo);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String dspcardno = Maths.bytesToHexString(cardNo);
                                if(dspcardno.contains("F")) {
                                    dspcardno = dspcardno.substring(0, dspcardno.indexOf("F"));
                                }
                                txt_Tip.setText(dspcardno);
                            }
                        });

                        //磁道
                        len = emvcoHelper.EmvGetTagData(tagData, 38, (short) 0x57);
                        final byte[] track2 = new byte[len];
                        Maths.byteArrayCpy(track2,0,tagData,0,len);
                        Log.e("xx", "track2="+Maths.bytesToHexString(track2));
                        posTrade.cardTrack2 = Maths.bytesToHexString(track2);

                        //卡有效期
                        len = emvcoHelper.EmvGetTagData(tagData, 20, (short) 0x5F24);
                        final byte[] expiredDate = new byte[len];
                        Maths.byteArrayCpy(expiredDate,0,tagData,0,len);
                        Log.e("xx", "expiredDate="+Maths.bytesToHexString(expiredDate));
                        posTrade.expiredDate = Maths.bytesToHexString(expiredDate);

                        //卡序列号
                        len = emvcoHelper.EmvGetTagData(tagData, 20, (short) 0x5F34);
                        final byte[] iccSn = new byte[len];
                        Maths.byteArrayCpy(iccSn,0,tagData,0,len);
                        Log.e("xx", "iccSn="+Maths.bytesToHexString(iccSn));
                        posTrade.iccSn = "00" + Maths.bytesToHexString(iccSn);

                        //IC卡数据
                        byte[] icData=new byte[256];
                        int icDataLen=0;

                        len = emvcoHelper.EmvGetTagData(tagData, 8, (short) 0x9F26);
                        final byte[] data_9F26 = new byte[len];
                        Maths.byteArrayCpy(data_9F26,0,tagData,0,len);
                        Log.e("xx", "data_9F26="+Maths.bytesToHexString(data_9F26));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = 0x26;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F26, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 1, (short) 0x9F27);
                        final byte[] data_9F27 = new byte[len];
                        Maths.byteArrayCpy(data_9F27,0,tagData,0,len);
                        Log.e("xx", "data_9F27="+Maths.bytesToHexString(data_9F27));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = 0x27;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F27, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 32, (short) 0x9F10);
                        final byte[] data_9F10 = new byte[len];
                        Maths.byteArrayCpy(data_9F10,0,tagData,0,len);
                        Log.e("xx", "data_9F10="+Maths.bytesToHexString(data_9F10));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = 0x10;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F10, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 4, (short) 0x9F37);
                        final byte[] data_9F37 = new byte[len];
                        Maths.byteArrayCpy(data_9F37,0,tagData,0,len);
                        Log.e("xx", "data_9F37="+Maths.bytesToHexString(data_9F37));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = 0x37;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F37, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 4, (short) 0x9F36);
                        final byte[] data_9F36 = new byte[len];
                        Maths.byteArrayCpy(data_9F36,0,tagData,0,len);
                        Log.e("xx", "data_9F36="+Maths.bytesToHexString(data_9F36));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = 0x36;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F36, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 5, (short) 0x95);
                        final byte[] data_95 = new byte[len];
                        Maths.byteArrayCpy(data_95,0,tagData,0,len);
                        Log.e("xx", "data_95="+Maths.bytesToHexString(data_95));
                        icData[icDataLen++] = (byte) 0x95;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_95, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 3, (short) 0x9A);
                        final byte[] data_9A = new byte[len];
                        Maths.byteArrayCpy(data_9A,0,tagData,0,len);
                        Log.e("xx", "data_9A="+Maths.bytesToHexString(data_9A));
                        icData[icDataLen++] = (byte) 0x9A;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9A, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 1, (short) 0x9C);
                        final byte[] data_9C = new byte[len];
                        Maths.byteArrayCpy(data_9C,0,tagData,0,len);
                        Log.e("xx", "data_9C="+Maths.bytesToHexString(data_9C));
                        icData[icDataLen++] = (byte) 0x9C;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9C, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 6, (short) 0x9F02);
                        final byte[] data_9F02 = new byte[len];
                        Maths.byteArrayCpy(data_9F02,0,tagData,0,len);
                        Log.e("xx", "data_9F02="+Maths.bytesToHexString(data_9F02));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x02;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F02, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 2, (short) 0x5F2A);
                        final byte[] data_5F2A = new byte[len];
                        Maths.byteArrayCpy(data_5F2A,0,tagData,0,len);
                        Log.e("xx", "data_5F2A="+Maths.bytesToHexString(data_5F2A));
                        icData[icDataLen++] = (byte) 0x5F;
                        icData[icDataLen++] = (byte) 0x2A;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_5F2A, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 2, (short) 0x82);
                        final byte[] data_82 = new byte[len];
                        Maths.byteArrayCpy(data_82,0,tagData,0,len);
                        Log.e("xx", "data_82="+Maths.bytesToHexString(data_82));
                        icData[icDataLen++] = (byte) 0x82;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_82, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 2, (short) 0x9F1A);
                        final byte[] data_9F1A = new byte[len];
                        Maths.byteArrayCpy(data_9F1A,0,tagData,0,len);
                        Log.e("xx", "data_9F1A="+Maths.bytesToHexString(data_9F1A));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x1A;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F1A, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 6, (short) 0x9F03);
                        final byte[] data_9F03 = new byte[len];
                        Maths.byteArrayCpy(data_9F03,0,tagData,0,len);
                        Log.e("xx", "data_9F03="+Maths.bytesToHexString(data_9F03));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x03;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F03, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 3, (short) 0x9F33);
                        final byte[] data_9F33 = new byte[len];
                        Maths.byteArrayCpy(data_9F33,0,tagData,0,len);
                        Log.e("xx", "data_9F33="+Maths.bytesToHexString(data_9F33));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x33;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F33, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 3, (short) 0x9F34);
                        final byte[] data_9F34 = new byte[len];
                        Maths.byteArrayCpy(data_9F34,0,tagData,0,len);
                        Log.e("xx", "data_9F34="+Maths.bytesToHexString(data_9F34));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x34;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F34, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 1, (short) 0x9F35);
                        final byte[] data_9F35 = new byte[len];
                        Maths.byteArrayCpy(data_9F35,0,tagData,0,len);
                        Log.e("xx", "data_9F35="+Maths.bytesToHexString(data_9F35));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x35;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F35, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 8, (short) 0x9F1E);
                        final byte[] data_9F1E = new byte[len];
                        Maths.byteArrayCpy(data_9F1E,0,tagData,0,len);
                        Log.e("xx", "data_9F1E="+Maths.bytesToHexString(data_9F1E));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x1E;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F1E, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 16, (short) 0x84);
                        final byte[] data_84 = new byte[len];
                        Maths.byteArrayCpy(data_84,0,tagData,0,len);
                        Log.e("xx", "data_84="+Maths.bytesToHexString(data_84));
                        icData[icDataLen++] = (byte) 0x84;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_84, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 2, (short) 0x9F09);
                        final byte[] data_9F09 = new byte[len];
                        Maths.byteArrayCpy(data_9F09,0,tagData,0,len);
                        Log.e("xx", "data_9F09="+Maths.bytesToHexString(data_9F09));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x09;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F09, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 4, (short) 0x9F41);
                        final byte[] data_9F41 = new byte[len];
                        Maths.byteArrayCpy(data_9F41,0,tagData,0,len);
                        Log.e("xx", "data_9F41="+Maths.bytesToHexString(data_9F41));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x41;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F41, 0, len);
                        icDataLen += len;

                        len = emvcoHelper.EmvGetTagData(tagData, 16, (short) 0x9F63);
                        final byte[] data_9F63 = new byte[len];
                        Maths.byteArrayCpy(data_9F63,0,tagData,0,len);
                        Log.e("xx", "data_9F63="+Maths.bytesToHexString(data_9F63));
                        icData[icDataLen++] = (byte) 0x9F;
                        icData[icDataLen++] = (byte) 0x63;
                        icData[icDataLen++] = (byte) len;
                        Maths.byteArrayCpy(icData, icDataLen, data_9F63, 0, len);
                        icDataLen += len;

                        byte[] ic55 = new byte[icDataLen];
                        Maths.byteArrayCpy(ic55, 0, icData, 0, icDataLen);
                        Log.e("xx", "icDataLen="+icDataLen);
                        Log.e("xx", "ic55="+Maths.bytesToHexString(ic55));
                        posTrade.icData = Maths.bytesToHexString(ic55);

                        //密码
                        if(MainActivity.qpayFlag){     //云闪付
                            len = QpayPwd.length();
                            tagData = QpayPwd.getBytes();
                        }
                        else {
                            len = emvcoHelper.EmvGetTagData(tagData, 12, (short) 0xBD);
                        }
                        Log.e("xx", "Pin len="+len);
                        if(len > 0) {
                            byte[] pin = new byte[len];
                            Maths.byteArrayCpy(pin,0,tagData,0,len);
                            Log.e("xx", "pin" + Maths.bytesToHexString(pin));
                            posTrade.cardPwd = posTrade.encrypCardPin(new String(pin));
                        }
                        else {
                            posTrade.cardPwd="";
                        }

                        try{
                            final boolean result = posTrade.ConsumeTrade();
                            Log.e("xx", "result="+result);
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run(){
                                    if(result) {
                                        txt_Result.setText("交易成功");
                                    }
                                    else {
                                        txt_Result.setText("交易失败");
                                    }
                                }
                            });
                        }catch(final PosTrade.PosTradeException e){
                            e.printStackTrace();
                            Log.e("xx", "e="+e.getMessage());
                            final String msg = e.getMessage();
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run(){
                                    txt_Result.setText(msg);
                                }
                            });
                        }

                        emvcoHelper.EmvFinal();

                        break;
                    /*case TYPE_PIN_BLOCK:

                        int pinkey_n = 1;
                        byte[] card_no = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
                        byte[] amount = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
                        byte[] pin_block = new byte[8];

                        //emvcoHelper.EmvKeyPadInit(EmvTestActivity.this);

                        ret = emvcoHelper.EmvGetPinBlock(EmvTestActivity.this, pinkey_n, card_no, amount, pin_block);
                        //String pin_block00 = new String(pin_block, 0, 8);

                        if (ret != 0) {

                            Log.e(TAG,"PinBlock ret :" +ret);

                            m_bThreadFinished = true;
                            return;
                        }

                        final String pin_block00 = ByteUtil.bytearrayToHexString(pin_block, pin_block.length);

                        Log.e("pin_block00", "heyp pin_block00=----" + pin_block00);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvEmvMsg.setText("PinBlock :" + pin_block00);
                            }
                        });

                        break;
                    case TYPE_SHOW_PAD:
                        Log.e(TAG,"TYPE_SHOW_PAD");

                        final byte[] pwd = new byte[20];

//                emvcoHelper.EmvKeyPadInit(EmvTestActivity.this);
                        ret = emvcoHelper.EmvShowKeyPad(EmvTestActivity.this, pwd);

                        if (ret != 0) {
                            Log.e(TAG,"ShowPad ret :" +ret);
                            m_bThreadFinished = true;
                            return;
                        }

                        Log.e(TAG,"ShowPad ***********");

                        final String strPwd = ByteUtil.bytesToString(pwd);

                        if (!TextUtils.isEmpty(strPwd)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvEmvMsg.setText("Password : " + strPwd);
                                }
                            });
                        }

                        Log.e("EmvTestAty", ByteUtil.bytesToString(pwd));

                        break;*/
                    default:
                        break;

                }

                m_bThreadFinished = true;

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv);

        txt_Tip = (TextView) findViewById(R.id.txt_tip);
        txt_Result = (TextView) findViewById(R.id.txt_result);

        if(MainActivity.qpayFlag){     //云闪付
            txt_Tip.setText("请挥卡");
        }

        //Determine if the current Android version is >=23
        // 判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            testEmv();
        }

    }



    public static String[] MY_PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};

    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    /**
     * a callback for request permission
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                testEmv();
            }
        }
    }

    /**
     * @Description: Request permission
     * 申请权限
     */
    private void requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(EmvActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(EmvActivity.this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            testEmv();
        }
    }

    /**
     * @Description :  Test Emv
     */
    private void testEmv() {

        strEmvStatus = "";
        txt_Tip.setText("");

        if (emvThread != null && !emvThread.isThreadFinished()) {
            Log.e("xx", "Thread is still running...");
            return;
        }
        emvThread = new EmvThread(TYPE_TEST_EMV);
        emvThread.start();
    }

}
