package com.paydemo;

/**
 * 报文结构
 * Created by wuweiwei
 * Date 2016/12/6 10:59
 */
public class PosMessage {

    private byte[] MsgType = new byte[2];                            //#0 消息类型 n4
    private byte[] BitMap = new byte[8];                            //#1 位表 b64
    private byte[] MainAcc = new byte[1 + 10];                            //#2 主帐号 n..19
    private byte[] ProcessCode = new byte[3];                        //#3 处理码 n6
    private byte[] TradeAmount = new byte[6];                        //#4 交易金额 n12
    private byte[] VoucherNo = new byte[3];                            //#11 流水号 n6
    private byte[] TradeTime = new byte[3];                            //#12 交易时间 n6(hhmmss)
    private byte[] TradeDate = new byte[2];                            //#13 交易日期 n4(MMDD)
    private byte[] ExpiredDate = new byte[2];                        //#14 卡有效期 n4(YYMM)
    private byte[] SettleDate = new byte[2];                            //#15 清算日期 n4(MMDD)
    private byte[] EntryMode = new byte[2];                            //#22 POS输入方式 n3
    private byte[] IccSn = new byte[2];                                //#23 IC卡系列号 n3
    private byte ConditionCode;                            //#25 条件码 n2
    private byte CaptureCode;                            //#26 PIN获取码 n2
    private byte[] UnionpayId = new byte[1 + 6];                        //#32 受理方标识码 n..11
    private byte[] Track2 = new byte[1 + 19];                            //#35 2磁道数据 Z..37
    private byte[] Track3 = new byte[2 + 52];                            //#36 3磁道数据 Z..104
    private byte[] SysRefNo = new byte[12];                            //#37 系统参考号 an12
    private byte[] AuthCode = new byte[6];                            //#38 授权码 an6
    private byte[] ResponseCode = new byte[2];                        //#39 交易响应码 an2
    private byte[] TerminalNo = new byte[9];                            //#41 终端号 ans8
    private byte[] MerchantNo = new byte[16];                        //#42 商户号 ans15
    private byte[] Reserved43 = new byte[41];                    //#43 自定义域 ans40 //商户名称
    private byte[] AddResData = new byte[1 + 25];                        //#44 附加响应数据 an..25
    private byte[] PrivateData = new byte[2 + 170];                    //#48 附加数据-私有ans..322
    private byte[] CurrencyCode = new byte[3];                        //#49 交易货币代码 an3
    private byte[] PINData = new byte[9];                            //#52 个人密码 b64
    private byte[] SecurityInfo = new byte[8];                        //#53 安全控制信息 n16
    private byte[] BalanceAmount = new byte[2 + 40];                //#54 帐户余额 an...020
    private byte[] IccData = new byte[2 + 255];                        //#55 IC卡数据域 var up to 255
    private byte[] SendEPData = new byte[2 + 100];                  //#58 电子钱包数据 vart up to 100
    private byte[] Reserved59 = new byte[2 + 600];                    //#59 自定义域
    private byte[] Reserved60 = new byte[2 + 17];                            //#60 自定义60域长度n...13
    private byte[] Reserved61 = new byte[2 + 29];                            //#61 自定义61域长度 n...029
    private byte[] Reserved62 = new byte[2 + 512];                    //#62 自定义62域      ans...512
    private byte[] Reserved63 = new byte[2 + 63];                            //#63 自定义63域长度  ans...163
    private byte[] Mac = new byte[8];                                //#64 消息鉴别码 b64

    public byte[] getMsgType() {
        return MsgType;
    }

    public void setMsgType(byte[] msgType) {
        MsgType = msgType;
    }

    public byte[] getBitMap() {
        return BitMap;
    }

    public void setBitMap(byte[] bitMap) {
        BitMap = bitMap;
    }

    public byte[] getMainAcc() {
        return MainAcc;
    }

    public void setMainAcc(byte[] mainAcc) {
        MainAcc = mainAcc;
    }

    public byte[] getProcessCode() {
        return ProcessCode;
    }

    public void setProcessCode(byte[] processCode) {
        ProcessCode = processCode;
    }

    public byte[] getTradeAmount() {
        return TradeAmount;
    }

    public void setTradeAmount(byte[] tradeAmount) {
        TradeAmount = tradeAmount;
    }

    public byte[] getVoucherNo() {
        return VoucherNo;
    }

    public void setVoucherNo(byte[] voucherNo) {
        VoucherNo = voucherNo;
    }

    public byte[] getTradeTime() {
        return TradeTime;
    }

    public void setTradeTime(byte[] tradeTime) {
        TradeTime = tradeTime;
    }

    public byte[] getTradeDate() {
        return TradeDate;
    }

    public void setTradeDate(byte[] tradeDate) {
        TradeDate = tradeDate;
    }

    public byte[] getExpiredDate() {
        return ExpiredDate;
    }

    public void setExpiredDate(byte[] expiredDate) {
        ExpiredDate = expiredDate;
    }

    public byte[] getSettleDate() {
        return SettleDate;
    }

    public void setSettleDate(byte[] settleDate) {
        SettleDate = settleDate;
    }

    public byte[] getEntryMode() {
        return EntryMode;
    }

    public void setEntryMode(byte[] entryMode) {
        EntryMode = entryMode;
    }

    public byte[] getIccSn() {
        return IccSn;
    }

    public void setIccSn(byte[] iccSn) {
        IccSn = iccSn;
    }

    public byte getConditionCode() {
        return ConditionCode;
    }

    public void setConditionCode(byte conditionCode) {
        ConditionCode = conditionCode;
    }

    public byte getCaptureCode() {
        return CaptureCode;
    }

    public void setCaptureCode(byte captureCode) {
        CaptureCode = captureCode;
    }

    public byte[] getUnionpayId() {
        return UnionpayId;
    }

    public void setUnionpayId(byte[] unionpayId) {
        UnionpayId = unionpayId;
    }

    public byte[] getTrack2() {
        return Track2;
    }

    public void setTrack2(byte[] track2) {
        Track2 = track2;
    }

    public byte[] getTrack3() {
        return Track3;
    }

    public void setTrack3(byte[] track3) {
        Track3 = track3;
    }

    public byte[] getSysRefNo() {
        return SysRefNo;
    }

    public void setSysRefNo(byte[] sysRefNo) {
        SysRefNo = sysRefNo;
    }

    public byte[] getAuthCode() {
        return AuthCode;
    }

    public void setAuthCode(byte[] authCode) {
        AuthCode = authCode;
    }

    public byte[] getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(byte[] responseCode) {
        ResponseCode = responseCode;
    }

    public byte[] getTerminalNo() {
        return TerminalNo;
    }

    public void setTerminalNo(byte[] terminalNo) {
        TerminalNo = terminalNo;
    }

    public byte[] getMerchantNo() {
        return MerchantNo;
    }

    public void setMerchantNo(byte[] merchantNo) {
        MerchantNo = merchantNo;
    }

    public byte[] getReserved43() {
        return Reserved43;
    }

    public void setReserved43(byte[] reserved43) {
        Reserved43 = reserved43;
    }

    public byte[] getAddResData() {
        return AddResData;
    }

    public void setAddResData(byte[] addResData) {
        AddResData = addResData;
    }

    public byte[] getPrivateData() {
        return PrivateData;
    }

    public void setPrivateData(byte[] privateData) {
        PrivateData = privateData;
    }

    public byte[] getCurrencyCode() {
        return CurrencyCode;
    }

    public void setCurrencyCode(byte[] currencyCode) {
        CurrencyCode = currencyCode;
    }

    public byte[] getPINData() {
        return PINData;
    }

    public void setPINData(byte[] PINData) {
        this.PINData = PINData;
    }

    public byte[] getSecurityInfo() {
        return SecurityInfo;
    }

    public void setSecurityInfo(byte[] securityInfo) {
        SecurityInfo = securityInfo;
    }

    public byte[] getBalanceAmount() {
        return BalanceAmount;
    }

    public void setBalanceAmount(byte[] balanceAmount) {
        BalanceAmount = balanceAmount;
    }

    public byte[] getIccData() {
        return IccData;
    }

    public void setIccData(byte[] iccData) {
        IccData = iccData;
    }

    public byte[] getSendEPData() {
        return SendEPData;
    }

    public void setSendEPData(byte[] sendEPData) {
        SendEPData = sendEPData;
    }

    public byte[] getReserved59() {
        return Reserved59;
    }

    public void setReserved59(byte[] reserved59) {
        Reserved59 = reserved59;
    }

    public byte[] getReserved60() {
        return Reserved60;
    }

    public void setReserved60(byte[] reserved60) {
        Reserved60 = reserved60;
    }

    public byte[] getReserved61() {
        return Reserved61;
    }

    public void setReserved61(byte[] reserved61) {
        Reserved61 = reserved61;
    }

    public byte[] getReserved62() {
        return Reserved62;
    }

    public void setReserved62(byte[] reserved62) {
        Reserved62 = reserved62;
    }

    public byte[] getReserved63() {
        return Reserved63;
    }

    public void setReserved63(byte[] reserved63) {
        Reserved63 = reserved63;
    }

    public byte[] getMac() {
        return Mac;
    }

    public void setMac(byte[] mac) {
        Mac = mac;
    }
}
