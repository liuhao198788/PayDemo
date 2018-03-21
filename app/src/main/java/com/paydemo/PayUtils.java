package com.paydemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by WuWeiwei
 * Date 2018/3/5
 */

public class PayUtils{


    /**
     * 保存系统凭证号
     * */
    public static void SaveVoucherNo(Context context, String value){
        SharedPreferences sp = context.getSharedPreferences("PaySys", context.MODE_PRIVATE);
        sp.edit().putString("voucherNo", value).commit();
    }


    /**
     * 读取系统凭证号
     * */
    public static String ReadVoucherNo(Context context){
        SharedPreferences sp = context.getSharedPreferences("PaySys", context.MODE_PRIVATE);
        String value = sp.getString("voucherNo", null);

        return value;
    }
}
