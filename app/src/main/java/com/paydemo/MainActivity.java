package com.paydemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private boolean loginState=false;       //签到状态
    PosTrade posTrade = new PosTrade(this);
    private TextView txt_VoucherNo;
    public static boolean qpayFlag=false;   //云闪付标识

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        //设置
        findViewById(R.id.btn_set).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                View dialogView = getLayoutInflater().inflate(R.layout.setting_dialog, null);
                final EditText editText = (EditText) dialogView.findViewById(R.id.edit_voucher_no);
                String voucherNo = PayUtils.ReadVoucherNo(MainActivity.this);
                editText.setText(voucherNo);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("请设置凭证号")//设置对话框的标题
                        .setView(dialogView)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content = editText.getText().toString();
                                PayUtils.SaveVoucherNo(MainActivity.this, content);
                                txt_VoucherNo.setText(String.format(getResources().getString(R.string.dsp_voucher_no), content));
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();

            }
        });

        //签到
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            Log.e("xx", "---签到---");
                            boolean ret = posTrade.Signin();
                            Log.e("xx", "ret="+ret);
                            if(ret) {
                                loginState = true;
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run(){
                                        showMessage("签到成功");
                                        txt_VoucherNo.setText(String.format(getResources().getString(R.string.dsp_voucher_no),
                                                PayUtils.ReadVoucherNo(MainActivity.this)));
                                    }
                                });
                            }
                        }catch(final PosTrade.PosTradeException e){
                            e.printStackTrace();
                            final String msg = e.getMessage();
                            Log.e("xx", "e="+msg);
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run(){
                                    showMessage(msg);
                                }
                            });
                        }
                    }
                }).start();

            }
        });

        //消费
        findViewById(R.id.btn_pay).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if(!loginState) {
                    showMessage("请先签到");
                    return;
                }

                qpayFlag = false;
                startActivity(new Intent(MainActivity.this, EmvActivity.class));
            }
        });

        //云闪付
        findViewById(R.id.btn_qpay).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if(!loginState) {
                    showMessage("请先签到");
                    return;
                }

                qpayFlag = true;
                startActivity(new Intent(MainActivity.this, EmvActivity.class));
            }
        });
    }

    private void initData(){
        String voucherNo = PayUtils.ReadVoucherNo(MainActivity.this);
        Log.e("xx", "voucherNo="+voucherNo);
        if(voucherNo == null) {
            voucherNo = "000001";
            PayUtils.SaveVoucherNo(MainActivity.this, voucherNo);
        }
        txt_VoucherNo = (TextView) findViewById(R.id.dsp_voucher_no);
        txt_VoucherNo.setText(String.format(getResources().getString(R.string.dsp_voucher_no), voucherNo));
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        txt_VoucherNo.setText(String.format(getResources().getString(R.string.dsp_voucher_no),
                PayUtils.ReadVoucherNo(MainActivity.this)));
    }
}
