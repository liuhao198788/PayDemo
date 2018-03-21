package com.paydemo;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by wuweiwei
 * Date 2016/12/5 12:14
 */
public class Maths{

    static {
        System.loadLibrary("Maths");
    }

    public static native byte[] intTobcd(int src, int len);

    public static native int bcdToint(byte []src, int start, int count);

    public static native byte[] intTohex(int src, int len);

    public static native int hexToint(byte []src, int start, int count);

    public static native byte[] stringTobcd(String src);

    public static native String bcdTostring(byte []src);

    public static native byte[] regulateAmountToBcd(String amount);

    /**
     * byte数组对比
     */
    public static int byteArrayCmp(byte []array1, int array1Start, byte []array2, int array2Start, int count) {
        for(int i=0; i<count; i++) {
            if(array1[array1Start+i] < array2[array2Start+i])
                return -1;
            else if(array1[array1Start+i] > array2[array2Start+i])
                return 1;
        }

        return 0;
    }

    /**
     * byte数组拷贝
     */
    public static void byteArrayCpy(byte []dest, int destStart, byte []src, int srcStart, int count) {
        for(int i=0; i<count; i++) {
            dest[destStart+i] = src[srcStart+i];
        }
    }

    /**
     * 截取byte数组中的数据
     */
    public static byte[] byteArraryExtract(byte[] src, int start, int count) {
        byte []data = new byte[count];
        byteArrayCpy(data, 0, src, start, count);
        return data;
    }

    /**
     * Convert byte[] to hex string. 把字节数组转化为字符串
     * 这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src
     *            byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * Convert hex string to byte[] 把为字符串转化为字节数组
     *
     * @param hexString
     *            the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }


    /**
     * Convert char to byte
     *
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * 16进制字符串转成普通字符串
     */
    public static String hexStringToString(String hexString) {
        String result = "";
        int length = hexString.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = hexString.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * 普通字符串转16进制字符串
     */
    public static String StringToHexString(String st) {
        String str = "";
        try {
            byte[] b = st.getBytes();

            for (int i = 0; i < b.length; i++) {
                Integer I = new Integer(b[i]);
                String strTmp = I.toHexString(b[i]);
                if (strTmp.length() > 2)
                    strTmp = strTmp.substring(strTmp.length() - 2);
                str = str + strTmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;

    }

    /**
     * 判读byte数组是不是全是0x00
     */
    public static boolean bytesIsZero(byte [] data) {

        int k=0;
        for(int i=0; i< data.length; i++) {
            if(data[i] == 0x00)
                k++;
        }

        if(k == data.length)
            return true;

        return false;
    }

    private final static String DES_TYPE = "DES/ECB/NoPadding";// DES加密类型

    /**
     * DES加密
     */
    public static byte[] encrypByDes(byte[] src, byte[] password) {
        try {
            if (src.length % 8 > 0) {// 补足8字节整数倍
                byte[] temp = new byte[(src.length / 8 + 1) * 8];
                System.arraycopy(src, 0, temp, 0, src.length);
                src = temp;
            }
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password);
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance(DES_TYPE);
            // 用密匙初始化Cipher对象 DES/ECB/NoPadding
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            // 现在，获取数据并加密
            // 正式执行加密操作
            return cipher.doFinal(src);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DES解密过程
     */
    public static byte[] decryptByDes(byte[] src, byte[] password) {
        try {
            if (src.length % 8 > 0) {// 补足8字节整数倍
                byte[] temp = new byte[(src.length / 8 + 1) * 8];
                System.arraycopy(src, 0, temp, 0, src.length);
                src = temp;
            }
            // DES算法要求有一个可信任的随机数源
            SecureRandom random = new SecureRandom();
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(password);
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance(DES_TYPE);
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            // 真正开始解密操作
            return cipher.doFinal(src);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 3DES加密
     * 双倍长密钥加密 3DES加密过程为：C=Ek3(Dk2(Ek1(P)))
     * K1=K3，但不能K1=K2=K3（如果相等的话就成了DES算法了）
     */
    public static byte[] encrypBy3Des(byte[] text, byte[] password) {
        if (password.length != 16)
            return null;

        byte[] key1 = new byte[8];
        byte[] key2 = new byte[8];
        System.arraycopy(password, 0, key1, 0, 8);
        System.arraycopy(password, 8, key2, 0, 8);
        byte[] result = encrypByDes(text, key1);
        result = decryptByDes(result, key2);
        result = encrypByDes(result, key1);
        return result;
    }

    /**
     * 3DES解密
     * 双倍长密钥解密 3DES解密过程为：P=Dk1((EK2(Dk3(C)))
     * K1=K3，但不能K1=K2=K3（如果相等的话就成了DES算法了）
     */
    public static byte[] decryptBy3Des(byte[] text, byte[] password) {
        if (password.length != 16)
            return null;

        byte[] key1 = new byte[8];
        byte[] key2 = new byte[8];
        System.arraycopy(password, 0, key1, 0, 8);
        System.arraycopy(password, 8, key2, 0, 8);
        byte[] result = decryptByDes(text, key1);
        result = encrypByDes(result, key2);
        result = decryptByDes(result, key1);
        return result;
    }

    public static byte[] hex2byte(byte[] var0, int var1, int var2) {
        byte[] var3 = new byte[var2];

        for(int var4 = 0; var4 < var2 * 2; ++var4) {
            int var5 = var4 % 2 == 1?0:4;
            var3[var4 >> 1] = (byte)(var3[var4 >> 1] | Character.digit((char)var0[var1 + var4], 16) << var5);
        }

        return var3;
    }

    public static byte[] hex2byte(String var0) {
        return var0.length() % 2 == 0?hex2byte(var0.getBytes(), 0, var0.length() >> 1):hex2byte("0" + var0);
    }

    public static String tohexSring(String s)
    {
        byte[] b = s.getBytes();
        String str = "";
        for (int i = 0; i < b.length; i++) {
            Integer I = new Integer(b[i]);
            String strTmp = I.toHexString(b[i]);
            if (strTmp.length() > 2)
                strTmp = strTmp.substring(strTmp.length() - 2);
            str = str + strTmp;
        }

        //System.out.println(str.toUpperCase());

        return str.toUpperCase();
    }
}
