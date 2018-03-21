package com.paydemo;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.paydemo.Maths.encrypBy3Des;
import static com.paydemo.Maths.encrypByDes;
import static com.paydemo.Maths.stringTobcd;

/**
 * Created by WuWeiwei
 * Date 2018/2/28
 */

public class PosTrade {

    private final Context context;
    private String merchantNo = "848584359686300";
    //商户号
    private String terminalNo = "58800024";
    //终端号
    private byte[] TMK = new byte[]{0x54, 0x58, (byte) 0xBC, 0x0D, (byte) 0xD0, (byte) 0xD6, (byte) 0xF2, (byte) 0xB5, (byte) 0xE3, 0x0D, 0x49, (byte) 0xC2, 0x3E, 0x68, (byte) 0x8A, (byte) 0xC7};
    private static byte[] PIN_KEY = new byte[16];
    private static byte[] MAC_KEY = new byte[8];

    private PosMessage sendPosMessage = new PosMessage();  //发送报文
    private PosMessage recvPosMessage = new PosMessage();  //接收报文
    private byte[] SendDomain = new byte[64];    //发送包域
    private byte[] RecDomain = new byte[64];        //接收包域
    public byte[] tradeAmount = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x01};     //交易金额,以分为单位,如“12.34元”为{0x00,0x00,0x00,0x00,0x12,0x34}
    public String cardMode = "";        //读卡模式
    public String cardNo = "";          //卡号
    public String expiredDate = "";     //卡有效期
    public String iccSn = "";           //卡序列号
    public String icData = "";          //IC卡数据
    public String cardTrack1 = "";      //一磁道
    public String cardTrack2 = "";      //二磁道
    public String cardTrack3 = "";      //三磁道
    public String cardPwd = "";         //持卡人密码
    private String sysRefNo = "";        //系统参数考号
    private String authCode = "";        //授权码
    private String batchNo = "000001";    //批次号
    private final String CARD_IC = "CARD_IC";     //IC卡
    private final String CARD_RF = "CARD_RF";     //非接卡
    private byte[] CalcMac = new byte[8];       //计算出来的MAC值

    /*****************解包错误代码定义**********************/
    public final int ERR_MSGTYPE = 0xA0;                    //消息类型不符
    public final int ERR_PROCESSCODE = 0xA1;                    //处理码不符
    public final int ERR_VOUCHERNO = 0xA2;                    //凭证号不符
    public final int ERR_TRADEAMOUNT = 0xA3;                    //交易金额不符
    public final int ERR_BATCHNO = 0xA4;                    //批次号不符
    public final int ERR_TERMINALNO = 0xA5;                    //终端号不符
    public final int ERR_MERCHANTNO = 0xA6;                    //商户号不符
    public final int ERR_CONDITIONCODE = 0xA7;                    //条件码不符
    public final int ERR_CURRENCYCODE = 0xA8;                    //货币代码不符
    public final int ERR_UNIONPAYID = 0xA9;                    //32域格式错
    public final int ERR_MISSMAC = 0xAA;                    //缺少MAC域
    public final int ERR_MISSBALANCE = 0xAB;                    //缺少查询余额域
    public final int ERR_MISSACC = 0xAC;                    //缺少主帐号
    public final int ERR_MISSAUTHCODE = 0xAD;                    //缺少授权码
    public final int ERR_CARDDES = 0xAE;                    //卡加密错
    public final int ERR_RECZERO = 0xAF;                    //接收长度为零
    public final int ERR_FLUSHWK = 0xB0;                    //更新密钥失败

    int[] ErrorTab = new int[]{
            7,                           //tpk校验错
            8,                            //Mac错
            9,                          //tdk校验错
            ERR_MSGTYPE,            //消息类型不符
            ERR_PROCESSCODE,         //处理码不符

            ERR_VOUCHERNO,           //凭证号不符
            ERR_TRADEAMOUNT,         //交易金额不符
            ERR_BATCHNO,             //批次号不符
            ERR_TERMINALNO,          //终端号不符
            ERR_MERCHANTNO,          //商户号不符

            ERR_CONDITIONCODE,       //条件码不符
            ERR_CURRENCYCODE,        //货币代码不符
            ERR_UNIONPAYID,          //32域格式错
            ERR_MISSMAC,             //缺少MAC域
            ERR_MISSBALANCE,         //缺少查询余额域

            ERR_MISSACC,             //缺少主帐号
            ERR_MISSAUTHCODE,        //缺少授权码
            ERR_CARDDES,             //卡加密错
            ERR_RECZERO,             //接收长度为零
            ERR_FLUSHWK                //更新密钥失败
    };

    String[] ErrorHintTab = new String[]{
            "PIN校验错",
            "MAC校验错",
            "TDK校验错",
            "消息类型不符",
            "处理码不符",

            "凭证号不符",
            "交易金额不符",
            "批次号不符",
            "终端号不符",
            "商户号不符",

            "条件码不符",
            "货币代码不符",
            "消息格式错",
            "缺少MAC域",
            "缺少金额域",

            "缺少主帐号",
            "缺少授权码",
            "卡加密错",
            "接收长度为零",
            "更新密钥失败"
    };

    private final int RESPERRORNUM = 49;
    String[] RespErrorTab = new String[]{
            "01", "03", "04", "05", "11",
            "12", "13", "14", "15", "21",
            "22", "25", "30", "34", "38",
            "40", "41", "43", "51", "54",
            "55", "57", "58", "59", "61",
            "62", "64", "65", "68", "75",
            "90", "91", "92", "94", "96",
            "97", "98", "99", "A0", "A1",
            "A2", "A3", "A4", "A5", "A6",
            "A7", "77", "06", "08"
    };

    String[] RespErrorHintTab = new String[]{
            "请持卡人与发卡行联系",
            "无效商户",
            "此卡被没收",
            "持卡人认证失败",
            "成功,VIP客户",

            "无效交易",
            "无效金额",
            "无效卡号",
            "此卡无对应发卡方",
            "该卡未初始化或睡眠卡",

            "操作有误,或超出交易允许天数",
            "没有原始交易,请联系发卡方",
            "请重试",
            "作弊卡,吞卡",
            "密码错误次数超限,请与发卡方联系",

            "交易失败,发卡方不支持的交易类型",
            "挂失卡,请没收(POS)",
            "被窃卡,请没收",
            "可用余额不足",
            "该卡已过期",

            "密码错",
            "不允许此卡交易",
            "发卡方不允许该 卡在本终端进行此交易",
            "卡片校验错",
            "交易金额超限",

            "受限制的卡",
            "交易金额与原交 易不匹配",
            "超出消费次数限制",
            "交易超时,请重试",
            "密码错误次数超限",

            "系统日切,请稍后重试",
            "发卡方状态不正 常,请稍后重试",
            "发卡方线路异常,请稍后重试",
            "拒绝,重复交易, 请稍后重试",
            "拒绝,交换中心异常,请稍后重试",

            "终端未登记",
            "发卡方超时",
            "PIN格式错,请重新签到",
            "MAC校验错,请重新签到",
            "转账货币不一致",

            "交易成功,请向发卡行确认",
            "账户不正确",
            "交易成功,请向发卡行确认",
            "交易成功,请向发卡行确认",
            "交易成功,请向发卡行确认",

            "拒绝,交换中心异常,请稍后重试",
            "批次号不一致,请重新签到",
            "请重新签到",
            "无此硬件序列号"
    };

    public PosTrade(Context mContext) {
        context = mContext;
    }

    public static class PosTradeException extends Exception {

        public PosTradeException(String msg) {
            super(msg);
        }
    }

    /**
     * 位图转成域
     */
    byte[] bitmapToDomain(byte[] bitmap) {
        int i, j, k;

        byte[] domain = new byte[bitmap.length * 8];
        for (i = 0; i < bitmap.length; i++) {
            for (j = 0; j < 8; j++) {
                k = i * 8 + j;
                domain[k] = 0;
                if (((bitmap[i] << j) & 0x80) == 0x80) {
                    domain[k] = (byte) (k + 1);
                }
            }
        }

        return domain;
    }

    /**
     * 域转位图
     */
    byte[] domainToBitmap(byte[] damin) {
        int i, j;
        byte[] bitmap = new byte[8];
        for (i = 0, j = 0; i < damin.length; i++, j++) {
            if (j % 8 == 0) {
                bitmap[j / 8] = 0x00;                            //位图清零
            }
            bitmap[j / 8] <<= 1;
            if (damin[i] != 0) {
                bitmap[j / 8] += 0x01;                            //位图置位
            }
        }

        return bitmap;
    }

    /**
     * 获取流水号，每次自动加1
     * 固定为3个字节
     */
    public byte[] getVoucherNo() {

        int voucherNo = Integer.parseInt(PayUtils.ReadVoucherNo(context));
        voucherNo++;
        byte[] byVoucherNo = Maths.intTobcd(voucherNo, 3);
        PayUtils.SaveVoucherNo(context, Maths.bytesToHexString(byVoucherNo));

        return byVoucherNo;
    }

    /**
     * 发送和接收报文
     */
    private String sendRecvMsg(final String sendMsg) {

        try {
            Socket socket = new Socket();
            socket.setSoTimeout(60 * 1000);   //设置接收超时时间
            socket.connect(new InetSocketAddress("116.228.6.69", 10002), 60 * 1000);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream os = socket.getOutputStream();
            os.write(Maths.hexStringToBytes(sendMsg));      //发送数据

            // 不断的读取Socket输入流的内容
            byte[] lenbuf = new byte[2]; //两字节长度
            while (socket != null && socket.isConnected()) {

                if (socket.getInputStream().read(lenbuf) == 2) {

                    //计算前两位报文所表示的报文长度
                    int size = Maths.hexToint(lenbuf, 0, 2);
                    Log.e("xx", "size=" + size);
                    //新建对应长度字节数组
                    byte[] buf = new byte[size];
                    //读取数据
                    socket.getInputStream().read(buf);

                    return Maths.bytesToHexString(buf);
                } else {
                    return null;
                }
            }

        } catch (SocketTimeoutException e) {    //网络连接超时
            Log.e("xx", "网络连接失败");

        } catch (IOException io) {
            io.printStackTrace();
            Log.e("xx", "io.getMessage()=" + io.getMessage());
        }

        return null;
    }

    /**
     * 组发送包报文公共域
     */
    public void formPublicSendPosMessage() {

        int i = 0;
        byte[] ibcd = new byte[2];
        int len = 0;
        sendPosMessage.setBitMap(domainToBitmap(SendDomain));
        for (int k = 0; k < 64; k++) {
            switch (SendDomain[k]) {
                case 2:                                                                                    //主帐号
                    i = (cardNo.length() + 1) / 2;
                    byte[] mainAcc = new byte[i + 1];
                    ibcd = Maths.intTobcd(cardNo.length(), 1);
                    Maths.byteArrayCpy(mainAcc, 0, ibcd, 0, 1);
                    byte[] bcdCardNo = stringTobcd(cardNo);
                    Maths.byteArrayCpy(mainAcc, 1, bcdCardNo, 0, i);
                    sendPosMessage.setMainAcc(mainAcc);
                    break;

                case 4:                                                                                    //交易金额
                    sendPosMessage.setTradeAmount(tradeAmount);
                    break;

                case 11:                                                                                //流水号
                    sendPosMessage.setVoucherNo(getVoucherNo());
                    break;

                case 14:                                                                                //卡有效期
                    sendPosMessage.setExpiredDate(Maths.hexStringToBytes(expiredDate));
                    break;

                case 22:        //输入方式  改为右对齐
                    byte[] entryMode = new byte[2];
                    if (cardMode.equals(CARD_RF)) // 非接触输入方式PBOC
                    {
                        entryMode[0] = (byte) 0x98;
                    } else {
                        entryMode[0] = 0x05;
                    }
                    entryMode[1] = 0x20;
                    if (cardPwd.length() > 0)   //有密码
                    {
                        entryMode[1] = 0x10;
                    }
                    sendPosMessage.setEntryMode(entryMode);
                    break;

                case 23:                                                                                //卡序列号
                    sendPosMessage.setIccSn(Maths.hexStringToBytes(iccSn));
                    break;

                case 25:                                                                                //服务点条件码
                    sendPosMessage.setConditionCode((byte) 0x00);
                    break;

                case 26:                                                                                //PIN获取码
                    byte captureCode = 0x12;
                    sendPosMessage.setCaptureCode(captureCode);
                    break;

                case 35:                                                                                //2磁道数据
                    len = cardTrack2.length();
                    if (len > 37) len = 37;
                    i = (len + 1) / 2;
                    byte[] track2 = new byte[i + 1];
                    ibcd = Maths.intTobcd(len, 1);
                    Maths.byteArrayCpy(track2, 0, ibcd, 0, 1);
                    byte[] bcdTrack2 = stringTobcd(cardTrack2);
                    Maths.byteArrayCpy(track2, 1, bcdTrack2, 0, i);
                    sendPosMessage.setTrack2(track2);
                    break;

                case 36:                                                                                //3磁道数据
                    len = cardTrack3.length();
                    if (len > 104) len = 104;
                    i = (len + 1) / 2;
                    byte[] track3 = new byte[i + 2];
                    ibcd = Maths.intTobcd(len, 2);
                    Maths.byteArrayCpy(track3, 0, ibcd, 0, 2);
                    byte[] bcdTrack3 = stringTobcd(cardTrack3);
                    Maths.byteArrayCpy(track3, 2, bcdTrack3, 0, i);
                    sendPosMessage.setTrack3(track3);
                    break;

                case 37:                                                                                //系统参考号
                    sendPosMessage.setSysRefNo(Maths.hexStringToBytes(sysRefNo));
                    break;

                case 38:                                                                                //授权码
                    sendPosMessage.setAuthCode(Maths.hexStringToBytes(authCode));
                    break;

                case 41:                                                                                //终端号
                    sendPosMessage.setTerminalNo(terminalNo.getBytes());
                    break;

                case 42:                                                                                //商户号
                    sendPosMessage.setMerchantNo(merchantNo.getBytes());
                    break;

                case 49:                                                                                //货币代码
                    sendPosMessage.setCurrencyCode(new byte[]{0x31, 0x35, 0x36});
                    break;

                case 52:                                                                                //PIN
                    sendPosMessage.setPINData(Maths.hexStringToBytes(cardPwd));
                    break;

                case 53:                                                                                //安全码
                    byte[] securityInfo = new byte[8];
                    if (cardPwd.length() > 0) { //有密码
                        securityInfo[0] = 0x26;     //带主账号的加密
                    } else { //无密码
                        securityInfo[0] = 0x06;     //带主账号的加密
                    }
                    sendPosMessage.setSecurityInfo(securityInfo);
                    break;

                case 55:                    //IC卡数据
                    len = icData.length();
                    if (len > 512) len = 512;
                    i = (len + 1) / 2;
                    byte[] iccdata = new byte[i + 2];
                    ibcd = Maths.intTobcd(i, 2);
                    Maths.byteArrayCpy(iccdata, 0, ibcd, 0, 2);
                    byte[] bcdData = stringTobcd(icData);
                    Maths.byteArrayCpy(iccdata, 2, bcdData, 0, i);
                    Log.e("xx", "55 iccdata=" + Maths.bytesToHexString(iccdata));
                    sendPosMessage.setIccData(iccdata);
                    break;

                case 60:                                                                                //保留域
                    byte[] reserved60 = new byte[2 + 13];
                    reserved60[0] = 0x00;
                    reserved60[1] = 0x13;
                    reserved60[2] = 0;
                    Maths.byteArrayCpy(reserved60, 3, Maths.hexStringToBytes(batchNo), 0, 3);
                    sendPosMessage.setReserved60(reserved60);
                    break;

                case 61:                                                                            //保留域
                    break;

                case 62:                                                                            //62域缺省处理
                    break;

                case 63:                                                                            //国际组织代码
                    break;

                default:
                    break;
            }
        }

    }

    /**
     * 解析接收到的数据包
     */
    public void formRecvPosMessage(byte[] In) {

        int i, k;
        int Len = 0;

        recvPosMessage.setMsgType(Maths.byteArraryExtract(In, 0, 2));
        recvPosMessage.setBitMap(Maths.byteArraryExtract(In, 2, 8));
        RecDomain = bitmapToDomain(recvPosMessage.getBitMap());
        byte[] pX = new byte[In.length - 2 - 8];
        Maths.byteArrayCpy(pX, 0, In, 10, pX.length);
        for (k = 0; k < 64; k++) {
            switch (RecDomain[k]) {
                case 2:                                             //主帐号
                    i = Maths.bcdToint(pX, 0, 1);
                    if (i > 19) i = 19;
                    i = (i + 1) / 2 + 1;
                    recvPosMessage.setMainAcc(Maths.byteArraryExtract(pX, 0, i)); //填充接收包主帐号
                    Len = i;
                    break;
                case 3:                                             //处理码
                    recvPosMessage.setProcessCode(Maths.byteArraryExtract(pX, Len, 3));
                    Len += 3;
                    break;
                case 4:                                             //交易金额
                    recvPosMessage.setTradeAmount(Maths.byteArraryExtract(pX, Len, 6));
                    Len += 6;
                    break;
                case 11:                                            //流水号
                    recvPosMessage.setVoucherNo(Maths.byteArraryExtract(pX, Len, 3));
                    Len += 3;
                    break;
                case 12:                                            //交易时间
                    recvPosMessage.setTradeTime(Maths.byteArraryExtract(pX, Len, 3));
                    Len += 3;
                    break;
                case 13:                                            //交易日期
                    recvPosMessage.setTradeDate(Maths.byteArraryExtract(pX, Len, 2));
                    Len += 2;
                    break;
                case 14:                                            //卡失效日期
                    recvPosMessage.setExpiredDate(Maths.byteArraryExtract(pX, Len, 2));
                    Len += 2;
                    break;
                case 15:                                            //中心清算日期
                    recvPosMessage.setSettleDate(Maths.byteArraryExtract(pX, Len, 2));
                    Len += 2;
                    break;
                case 22:                                            //POS输入方式
                    recvPosMessage.setEntryMode(Maths.byteArraryExtract(pX, Len, 2));
                    Len += 2;
                    break;
                case 23:
                    recvPosMessage.setIccSn(Maths.byteArraryExtract(pX, Len, 2));
                    Len += 2;
                    break;
                case 25:                                            //条件码
                    recvPosMessage.setConditionCode(pX[Len]);
                    Len += 1;
                    break;
                case 26:                                            //PIN获取码
                    recvPosMessage.setCaptureCode(pX[Len]);
                    Len += 1;
                    break;
                case 32:                                           //POS中心代码
                    i = Maths.bcdToint(pX, Len, 1);
                    i = (i + 1) / 2 + 1;
                    if (i > 7) i = 7;
                    recvPosMessage.setUnionpayId(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 35:                                            //2磁道数据
                    i = Maths.bcdToint(pX, Len, 1);
                    i = (i + 1) / 2 + 1;
                    if (i > 20) i = 20;
                    recvPosMessage.setTrack2(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 36:                                           //3磁道数据
                    i = Maths.bcdToint(pX, Len, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 54) i = 54;
                    recvPosMessage.setTrack3(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 37:                                            //系统参考号
                    recvPosMessage.setSysRefNo(Maths.byteArraryExtract(pX, Len, 12));
                    Len += 12;
                    break;
                case 38:                                            //授权码
                    recvPosMessage.setAuthCode(Maths.byteArraryExtract(pX, Len, 6));
                    Len += 6;
                    break;
                case 39:                                            //交易响应码
                    recvPosMessage.setResponseCode(Maths.byteArraryExtract(pX, Len, 2));
                    Len += 2;
                    break;
                case 41:                                            //终端号
                    recvPosMessage.setTerminalNo(Maths.byteArraryExtract(pX, Len, 8));
                    Len += 8;
                    break;
                case 42:                                            //商户号
                    recvPosMessage.setMerchantNo(Maths.byteArraryExtract(pX, Len, 15));
                    Len += 15;
                    break;
                case 43:
                    recvPosMessage.setReserved43(Maths.byteArraryExtract(pX, Len, 40));
                    Len += 40;
                    break;
                case 44:                                            //附件响应数据
                    i = Maths.bcdToint(pX, Len, 1);
                    i = (i) + 1;
                    if (i > 26) i = 26;
                    recvPosMessage.setAddResData(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 48:                                            //附加数据-私有
                    i = Maths.bcdToint(pX, Len, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 163) i = 163;
                    recvPosMessage.setPrivateData(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 49:                                            //货币代码
                    recvPosMessage.setCurrencyCode(Maths.byteArraryExtract(pX, Len, 3));
                    Len += 3;
                    break;
                case 52:                                            //PIN数据
                    recvPosMessage.setPINData(Maths.byteArraryExtract(pX, Len, 8));
                    Len += 8;
                    break;
                case 53:                                            //安全控制信息
                    recvPosMessage.setSecurityInfo(Maths.byteArraryExtract(pX, Len, 8));
                    Len += 8;
                    break;
                case 54:                                            //附加金额
                    i = Maths.bcdToint(pX, Len, 2);
                    i = i + 2;
                    if (i > 42) i = 42;
                    recvPosMessage.setBalanceAmount(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 55:                                                    //Icc卡数据域
                    i = Maths.bcdToint(pX, Len, 2);
                    i = i + 2;
                    if (i > 257) i = 257;
                    recvPosMessage.setIccData(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;

                case 58:                                           // add for EP
                    i = Maths.bcdToint(pX, Len, 2);
                    i = i + 2;
                    if (i > 257) i = 257;
                    recvPosMessage.setSendEPData(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 59:
                    i = Maths.bcdToint(pX, Len, 2);
                    i = i + 2;
                    if (i > 600) i = 600;
                    recvPosMessage.setReserved59(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 60:                                            //自定义域60
                    i = Maths.bcdToint(pX, Len, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 11) i = 11;
                    recvPosMessage.setReserved60(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;

                case 61:                                              //原始信息域
                    i = Maths.bcdToint(pX, Len, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 17) i = 17;
                    recvPosMessage.setReserved61(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 62:                                                //自定义域62
                    i = Maths.bcdToint(pX, Len, 2);
                    i = i + 2;
                    if (i > 514) i = 514;
                    recvPosMessage.setReserved62(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 63:                                                        //自定义域63
                    i = Maths.bcdToint(pX, Len, 2);
                    i = i + 2;
                    if (i > 165) i = 165;
                    recvPosMessage.setReserved63(Maths.byteArraryExtract(pX, Len, i));
                    Len += i;
                    break;
                case 64:                                                        //MAC域
                    recvPosMessage.setMac(Maths.byteArraryExtract(pX, Len, 8));
                    Len += 8;
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 组8583数据包
     */
    public String form8583(PosMessage posMessage) {
        int i, m, k, Len;
        byte[] Out = new byte[2048];

        byte[] domain = bitmapToDomain(posMessage.getBitMap());
        Maths.byteArrayCpy(Out, 0, posMessage.getMsgType(), 0, 2);
        Len = 2;
        Maths.byteArrayCpy(Out, Len, posMessage.getBitMap(), 0, 8);
        Len += 8;
        for (k = 0; k < 64; k++) {
            switch (domain[k]) {
                case 2:                                     //主帐号
                    i = Maths.bcdToint(posMessage.getMainAcc(), 0, 1);
                    i = (i + 1) / 2 + 1;
                    Maths.byteArrayCpy(Out, Len, posMessage.getMainAcc(), 0, i);
                    Len += i;
                    break;

                case 3:                                     //处理码
                    Maths.byteArrayCpy(Out, Len, posMessage.getProcessCode(), 0, 3);
                    Len += 3;
                    break;

                case 4:                                     //交易金额
                    Maths.byteArrayCpy(Out, Len, posMessage.getTradeAmount(), 0, 6);
                    Len += 6;
                    break;

                case 11:                                    //流水号
                    Maths.byteArrayCpy(Out, Len, posMessage.getVoucherNo(), 0, 3);
                    Len += 3;
                    break;

                case 12:                                    //交易时间
                    Maths.byteArrayCpy(Out, Len, posMessage.getTradeAmount(), 0, 3);
                    Len += 3;
                    break;

                case 13:                                   //交易日期
                    Maths.byteArrayCpy(Out, Len, posMessage.getTradeDate(), 0, 2);
                    Len += 2;
                    break;

                case 14:                                    //卡失效日期
                    Maths.byteArrayCpy(Out, Len, posMessage.getExpiredDate(), 0, 2);
                    Len += 2;
                    break;

                case 15:                                    //中心清算日期
                    Maths.byteArrayCpy(Out, Len, posMessage.getSettleDate(), 0, 2);
                    Len += 2;
                    break;

                case 22:                                    //POS 输入方式
                    Maths.byteArrayCpy(Out, Len, posMessage.getEntryMode(), 0, 2);
                    Len += 2;
                    break;

                case 23:                                         //Ic卡系列号
                    Maths.byteArrayCpy(Out, Len, posMessage.getIccSn(), 0, 2);
                    Len += 2;
                    break;

                case 25:                                    //服务点条件码
                    Out[Len] = posMessage.getConditionCode();
                    Len += 1;
                    break;

                case 26:                                    //服务点PIN获取码
                    Out[Len] = posMessage.getCaptureCode();
                    Len += 1;
                    break;

                case 32:                                    //Pos中心号(受理方标识码)
                    i = Maths.bcdToint(posMessage.getUnionpayId(), 0, 1);
                    i = (i + 1) / 2 + 1;
                    if (i > 7) i = 7;
                    Maths.byteArrayCpy(Out, Len, posMessage.getUnionpayId(), 0, i);
                    Len += i;
                    break;

                case 35:                                     //2磁道数据
                    i = Maths.bcdToint(posMessage.getTrack2(), 0, 1);
                    i = (i + 1) / 2 + 1;
                    if (i > 20) i = 20;
                    Maths.byteArrayCpy(Out, Len, posMessage.getTrack2(), 0, i);
                    Len += i;
                    break;

                case 36:                                     //3磁道数据
                    i = Maths.bcdToint(posMessage.getTrack3(), 0, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 54) i = 54;
                    Maths.byteArrayCpy(Out, Len, posMessage.getTrack3(), 0, i);
                    Len += i;
                    break;

                case 37:                                      //系统参考号
                    Maths.byteArrayCpy(Out, Len, posMessage.getSysRefNo(), 0, 12);
                    Len += 12;
                    break;

                case 38:                                        //授权码
                    Maths.byteArrayCpy(Out, Len, posMessage.getAuthCode(), 0, 6);
                    Len += 6;
                    break;

                case 39:                                       //响应码
                    Maths.byteArrayCpy(Out, Len, posMessage.getResponseCode(), 0, 2);
                    Len += 2;
                    break;

                case 41:                                        //终端号
                    Maths.byteArrayCpy(Out, Len, posMessage.getTerminalNo(), 0, 8);
                    Len += 8;
                    break;

                case 42:                                       //商户号
                    Maths.byteArrayCpy(Out, Len, posMessage.getMerchantNo(), 0, 15);
                    Len += 15;
                    break;

                case 44:                                        //发卡行,收单行代码
                    i = Maths.bcdToint(posMessage.getAddResData(), 0, 1);
                    i = i + 1;
                    Maths.byteArrayCpy(Out, Len, posMessage.getAddResData(), 0, i);
                    Len += i;
                    break;

                case 48:                                        //附加数据-私有
                    i = Maths.bcdToint(posMessage.getPrivateData(), 0, 2);
                    i = (i + 1) / 2 + 2;
                    Maths.byteArrayCpy(Out, Len, posMessage.getPrivateData(), 0, i);
                    Len += i;
                    break;

                case 49:                                       //交易货币代码
                    Maths.byteArrayCpy(Out, Len, posMessage.getCurrencyCode(), 0, 3);
                    Len += 3;
                    break;

                case 52:                                    //密码密文
                    Maths.byteArrayCpy(Out, Len, posMessage.getPINData(), 0, 8);
                    Len += 8;
                    break;

                case 53:                                       //安全控制信息
                    Maths.byteArrayCpy(Out, Len, posMessage.getSecurityInfo(), 0, 8);
                    Len += 8;
                    break;

                case 55:                                    //Icc数据
                    i = Maths.bcdToint(posMessage.getIccData(), 0, 2);
                    i = i + 2;
                    if (i > 257) i = 257;
                    Maths.byteArrayCpy(Out, Len, posMessage.getIccData(), 0, i);
                    Len += i;
                    break;

                case 58: // add for EP
                    i = Maths.bcdToint(posMessage.getSendEPData(), 0, 2);
                    i = i + 2;
                    if (i > 257) i = 257;
                    Maths.byteArrayCpy(Out, Len, posMessage.getSendEPData(), 0, i);
                    Len += i;
                    break;

                case 60:                                       //自定义域60
                    i = Maths.bcdToint(posMessage.getReserved60(), 0, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 11) i = 11;
                    Maths.byteArrayCpy(Out, Len, posMessage.getReserved60(), 0, i);
                    Len += i;
                    break;

                case 61:                                        //自定义域61
                    i = Maths.bcdToint(posMessage.getReserved61(), 0, 2);
                    i = (i + 1) / 2 + 2;
                    if (i > 17) i = 17;
                    Maths.byteArrayCpy(Out, Len, posMessage.getReserved61(), 0, i);
                    Len += i;
                    break;

                case 62:                                        //自定义域62
                    i = Maths.bcdToint(posMessage.getReserved62(), 0, 2);
                    i += 2;
                    if (i > 514) i = 514;
                    Maths.byteArrayCpy(Out, Len, posMessage.getReserved62(), 0, i);
                    Len += i;
                    break;

                case 63:                                       //自定义域63
                    i = Maths.bcdToint(posMessage.getReserved63(), 0, 2);
                    i = i + 2;
                    if (i > 65) i = 65;
                    Maths.byteArrayCpy(Out, Len, posMessage.getReserved63(), 0, i);
                    Len += i;
                    break;

                case 64:
                    byte[] macData = new byte[Len];
                    Maths.byteArrayCpy(macData, 0, Out, 0, Len);
                    byte[] mac = encrypMac(macData);
                    Maths.byteArrayCpy(Out, Len, mac, 0, 8);
                    Len += 8;
                    break;

                default:
                    break;
            }
        }

        byte[] data = new byte[Len];
        Maths.byteArrayCpy(data, 0, Out, 0, Len);
        return Maths.bytesToHexString(data);
    }

    /**
     * 发送8583数据到后台
     */
    private boolean sendPos8583(String msg) {

        byte[] header = new byte[]{0x60, 0x01, 0x01, 0x00, 0x00, 0x60, 0x31, 0x00, 0x31, 0x10, 0x02};
        int len = header.length + msg.length() / 2;
        byte lenbuf[] = new byte[2];
        lenbuf[0] = (byte) (len / 256);
        lenbuf[1] = (byte) (len % 256);
        String sendMsg = Maths.bytesToHexString(lenbuf) + Maths.bytesToHexString(header) + msg;
        Log.e("xx", "sendPos8583 sendMsg=" + sendMsg);
        String recvMsg = sendRecvMsg(sendMsg);
        Log.e("xx", "sendPos8583 recvMsg=" + recvMsg);
        if (recvMsg != null) {
            if (recvMsg.length() > 22) { //接收到数据包
                formRecvPosMessage(Maths.hexStringToBytes(recvMsg.substring(22)));
                if (RecDomain[63] == 64) {     //存在64域
                    CalcMac = encrypMac(Maths.stringTobcd(recvMsg.substring(22, recvMsg.length() - 16)));
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 校验接收到的数据
     */
    private int CheckRecvPosMessage() {

        byte[] ResponseCode = recvPosMessage.getResponseCode();

        //MAC域判断
        byte[] BitMap = recvPosMessage.getBitMap();
        if ((BitMap[7] & 0x01) == 0x01) {

            if ((ResponseCode[0] == 0x30) && (ResponseCode[1] == 0x30)) {
                if (RecDomain[63] != 64) {
                    return ERR_MISSMAC;
                }
            }
        }
        if (RecDomain[63] == 64) {

            byte[] Mac = recvPosMessage.getMac();
            if (Maths.byteArrayCmp(Mac, 0, CalcMac, 0, 8) != 0) {
                return 8;
            }
        }

        //比较消息类型
        byte[] sMsgType = sendPosMessage.getMsgType();
        byte[] rMsgType = recvPosMessage.getMsgType();
        if (sMsgType[0] != rMsgType[0] || (sMsgType[1] != (rMsgType[1] - 0x10))) {
            return ERR_MSGTYPE;
        }

        //比较处理码
        if (RecDomain[2] == 3 && SendDomain[2] == 3) {
            if (Maths.byteArrayCmp(sendPosMessage.getProcessCode(), 0,
                    recvPosMessage.getProcessCode(), 0, 1) != 0) {
                return ERR_PROCESSCODE;
            }
        }

        //比较POS流水号
        if (RecDomain[10] == 11 && SendDomain[10] == 11) {
            if (Maths.byteArrayCmp(sendPosMessage.getVoucherNo(), 0,
                    recvPosMessage.getVoucherNo(), 0, 3) != 0) {
                return ERR_VOUCHERNO;
            }
        }

        //比较交易金额
        if (RecDomain[3] == 4 && SendDomain[3] == 4) {
            if (Maths.byteArrayCmp(sendPosMessage.getTradeAmount(), 0,
                    recvPosMessage.getTradeAmount(), 0, 6) != 0) {
                return ERR_TRADEAMOUNT;
            }
        }

        //比较终端号
        if (RecDomain[40] == 41 && SendDomain[40] == 41) {
            if (Maths.byteArrayCmp(sendPosMessage.getTerminalNo(), 0,
                    recvPosMessage.getTerminalNo(), 0, 8) != 0) {
                return ERR_TERMINALNO;
            }
        }

        //比较商户号
        if (RecDomain[41] == 42 && SendDomain[41] == 42) {
            if (Maths.byteArrayCmp(sendPosMessage.getMerchantNo(), 0,
                    recvPosMessage.getMerchantNo(), 0, 15) != 0) {
                return ERR_MERCHANTNO;
            }
        }

        //响应码处理
        if (sMsgType[0] != 0x05) {    //结算报文没有39域

            if (RecDomain[38] != 39 || !CheckResponseCode(ResponseCode)) {
                return 7;
            }
        }


        return 0;
    }

    /**
     * 校验响应码
     */
    private boolean CheckResponseCode(byte[] resp) {
        if (Maths.byteArrayCmp(resp, 0, "00".getBytes(), 0, 2) == 0
                || Maths.byteArrayCmp(resp, 0, "11".getBytes(), 0, 2) == 0
                || Maths.byteArrayCmp(resp, 0, "A2".getBytes(), 0, 2) == 0
                || Maths.byteArrayCmp(resp, 0, "A4".getBytes(), 0, 2) == 0
                || Maths.byteArrayCmp(resp, 0, "A5".getBytes(), 0, 2) == 0
                || Maths.byteArrayCmp(resp, 0, "A6".getBytes(), 0, 2) == 0
                ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得交易类错误
     */
    private String getTradeErrMsg(int code) {

        String errMsg = "";
        for (int i = 0; i < ErrorTab.length; i++) {
            if (code == ErrorTab[i]) {
                errMsg = ErrorHintTab[i];
                break;
            }
        }

        return errMsg;
    }

    /**
     * 获得响应码类错误信息
     */
    private String getResponseErrMsg(String code) {

        int i = 0;
        String errMsg = "";
        String mCode = Maths.hexStringToString(code);

        for (i = 0; i < RESPERRORNUM; i++) {
            if (mCode.equals(RespErrorTab[i]))
                break;
        }
        if (i < RESPERRORNUM) {
            errMsg = RespErrorHintTab[i];
        } else {
            errMsg = "交易失败";
        }

        return errMsg + " : " + mCode;
    }

    /**
     * 组签到发送报文包
     */
    private String formLoginSendPosMessag() {

        SendDomain = bitmapToDomain(new byte[]{0x00, 0x20, 0x00, 0x00, 0x00, (byte) 0xC0, 0x00, 0x16});
        sendPosMessage.setMsgType(new byte[]{0x08, 0x00});
        formPublicSendPosMessage();

        for (int k = 0; k < 64; k++) {
            switch (SendDomain[k]) {
                case 60:
                    byte[] byBatchNo = Maths.hexStringToBytes(batchNo);
                    sendPosMessage.setReserved60(new byte[]{0x00, 0x11, 0x00, byBatchNo[0], byBatchNo[1], byBatchNo[2], 0x00, 0x30});
                    break;

                case 62:
                    String PosSn = "JBW000001";
                    String reserved62 = "Sequence No";
                    byte[] len = Maths.intTobcd(PosSn.length() + 4, 1);
                    reserved62 = reserved62 + Maths.bytesToHexString(len) + "0000" + PosSn;
                    byte[] le = Maths.intTobcd(reserved62.length(), 2);
                    byte[] data = reserved62.getBytes();
                    byte[] reserved = new byte[le.length + data.length];
                    Maths.byteArrayCpy(reserved, 0, le, 0, 2);
                    Maths.byteArrayCpy(reserved, 2, data, 0, data.length);
                    sendPosMessage.setReserved62(reserved);
                    break;

                case 63:                                                                                                                   //±£ÁôÓò
                    sendPosMessage.setReserved63(new byte[]{0x00, 0x03, 0x30, 0x31, 0x20});
                    break;

                default:
                    break;
            }
        }

        return form8583(sendPosMessage);
    }

    /**
     * 签到交易
     */
    public boolean Signin() throws PosTradeException {

        String data = formLoginSendPosMessag();
        boolean ret = sendPos8583(data);
        if (ret) {
            int i = CheckRecvPosMessage();
            if (i == 0) {
                ret = updateWorkKey(recvPosMessage.getReserved62());
                if (ret) return true;
                else {
                    throw new PosTradeException("更新工作密钥失败");
                }
            } else if (i == 7) {
                throw new PosTradeException(getResponseErrMsg(Maths.bytesToHexString(
                        recvPosMessage.getResponseCode())));
            } else {
                throw new PosTradeException(getTradeErrMsg(i));
            }
        } else {
            throw new PosTradeException("通信失败");
        }
    }

    /**
     * 更新工作密钥
     */
    boolean updateWorkKey(byte[] reserved62) {

        //TPK数据填充
        byte[] temp = new byte[16];
        Maths.byteArrayCpy(temp, 0, reserved62, 2, 16);                                        //TPK
        PIN_KEY = Maths.decryptBy3Des(temp, TMK);
        Log.e("xx", "PIN_KEY=" + Maths.bytesToHexString(PIN_KEY));
        byte[] checkValue = encrypBy3Des(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, PIN_KEY);
        Log.e("xx", "checkValue=" + Maths.bytesToHexString(checkValue));

        if (Maths.byteArrayCmp(checkValue, 0, reserved62, 18, 4) != 0) {    //checkvalue
            return false;
        }

        //TAK数据填充
        byte[] temp2 = new byte[8];
        Maths.byteArrayCpy(temp2, 0, reserved62, 22, 8);                        //TAK
        MAC_KEY = Maths.decryptBy3Des(temp2, TMK);
        Log.e("xx", "MAC_KEY=" + Maths.bytesToHexString(MAC_KEY));
        byte[] checkValue2 = encrypByDes(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, MAC_KEY);
        Log.e("xx", "checkValue2=" + Maths.bytesToHexString(checkValue2));
        if (Maths.byteArrayCmp(checkValue2, 0, reserved62, 38, 4) != 0) { //checkvalue
            return false;
        }

        return true;
    }

    /**
     * 组消费交易发送报文
     */
    private String formConsumeTradeSendPosMessag() {

        SendDomain = bitmapToDomain(new byte[]{0x70, 0x24, 0x06, (byte) 0xC0, 0x20, (byte) 0xC0, (byte) 0x9A, 0x11});
        sendPosMessage.setMsgType(new byte[]{0x02, 0x00});
        if (cardPwd == "") {
            SendDomain[25] = 0x00;
            SendDomain[51] = 0x00;
            SendDomain[52] = 0x00;
        }
        formPublicSendPosMessage();

        for (int k = 0; k < 64; k++) {
            switch (SendDomain[k]) {
                case 3:
                    sendPosMessage.setProcessCode(new byte[]{0x00, 0x00, 0x00});
                    break;

                case 60:
                    byte[] byBatchNo = Maths.hexStringToBytes(batchNo);
                    sendPosMessage.setReserved60(new byte[]{0x00, 0x14, 0x22, byBatchNo[0], byBatchNo[1], byBatchNo[2], 0x00, 0x05, 0x01});
                    break;

                default:
                    break;
            }
        }

        return form8583(sendPosMessage);
    }

    /**
     * 消费交易
     */
    public boolean ConsumeTrade() throws PosTradeException {

        String data = formConsumeTradeSendPosMessag();
        boolean ret = sendPos8583(data);
        if (ret) {
            int i = CheckRecvPosMessage();
            if (i == 0) {
                return true;
            } else if (i == 7) {
                String respErrMsg = getResponseErrMsg(Maths.bytesToHexString(
                        recvPosMessage.getResponseCode()));
                throw new PosTradeException(respErrMsg);
            } else {
                String tradeErrMsg = getTradeErrMsg(i);
                throw new PosTradeException(tradeErrMsg);
            }
        } else {
            throw new PosTradeException("通信失败");
        }
    }

    /**
     * 得到计算卡号
     */
    private byte[] getPan(String cardNo) {
        byte[] PAN = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

        if (cardNo.length() >= 13) {
            String s = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1);
            byte[] bytes = stringTobcd(s);
            Maths.byteArrayCpy(PAN, 2, bytes, 0, 6);
        }

        return PAN;
    }

    private byte[] E98(String pin, byte[] pan) {
        int i = 0;
        byte[] PINBLOCK = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

        i = pin.length();
        PINBLOCK[0] = (byte) i;
        byte[] b = Maths.stringTobcd(pin);
        Maths.byteArrayCpy(PINBLOCK, 1, b, 0, b.length);
        if ((i % 2) != 0) {                                    //奇数
            PINBLOCK[(i / 2) + 1] |= 0x0F;                    //确保为0x0F
        }
        byte[] xpin = XOR8(pan, PINBLOCK);

        byte[] epin = Maths.encrypBy3Des(xpin, PIN_KEY);
        return epin;
    }

    private byte[] XOR8(byte[] Scr1, byte[] Scr2) {
        byte[] Dest = new byte[8];
        for (int i = 0; i < 8; i++) {
            Dest[i] = (byte) (Scr1[i] ^ Scr2[i]);
        }

        return Dest;
    }

    /**
     * 持卡人密码加密
     */
    public String encrypCardPin(String pin) {
        byte[] pan = getPan(cardNo);
        byte[] bpin = E98(pin, pan);
        return Maths.bytesToHexString(bpin);
    }

    private byte[] Ecb(byte[] data) {
        int i;
        byte[] A1 = new byte[8];

        int len = data.length;
        for (i = 0; i < (len / 8); i++) {
            byte[] b = new byte[8];
            Maths.byteArrayCpy(b, 0, data, i * 8, 8);
            A1 = XOR8(A1, b);
        }
        if ((len % 8) != 0) {
            byte[] temp = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            Maths.byteArrayCpy(temp, 0, data, (len / 8) * 8, len - (len / 8) * 8);
            A1 = XOR8(A1, temp);
        }

        return A1;
    }

    /**
     * 计算MAC
     */
    public byte[] encrypMac(byte[] data) {

        byte[] e = Ecb(data);
        String m = Maths.bcdTostring(e);
        String m1 = m.substring(0, 8);
        String m2 = m.substring(8, 16);

        byte[] d11 = m1.getBytes();
        byte[] d1 = Maths.encrypByDes(d11, MAC_KEY);
        byte[] d22 = m2.getBytes();
        byte[] d2 = XOR8(d1, d22);
        byte[] d3 = Maths.encrypByDes(d2, MAC_KEY);
        String s = Maths.bcdTostring(d3);
        String d = s.substring(0, 8);
        byte[] r = d.getBytes();
        return r;
    }
}
