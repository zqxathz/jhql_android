package com.jhlotus.vine;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.jhlotus.vine.util.Constant;

public class VisitorEnterActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "debug";
    private Button mBtnScancode;
    private TextView mTvResult;

    private Integer oid;
    private Dialog mDialogLoading;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int val = data.getInt("value");
            WeiboDialogUtils.closeDialog(mDialogLoading);
            if (val==1){
                mTvResult.setText("此证件可用");
            }else{
                mTvResult.setTextColor(Resources.getSystem().getColor(android.R.color.holo_red_dark));
                mTvResult.setText("此证件不可用");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_enter);

        mBtnScancode = (Button) findViewById(R.id.btn_scancode);
        mTvResult = (TextView) findViewById(R.id.tv_result);

        mBtnScancode.setOnClickListener(this);

        Intent i = getIntent();
        oid = i.getIntExtra("oid",0);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_scancode:
                Log.d(TAG, "onClick: "+ oid);
                startQrCode();
                break;
            default:break;
        }
    }

    // 开始扫码
    private void startQrCode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            //Toast.makeText(getApplicationContext(),scanResult,Toast.LENGTH_LONG).show();

            mDialogLoading  = WeiboDialogUtils.createLoadingDialog(VisitorEnterActivity.this,"处理中...");
            TransData  transdata =  new TransData();
            transdata.setHandler(handler);
            int r = transdata.visitorEnter(scanResult,oid);
            /*if (r==1){
                Toast.makeText(getApplicationContext(),"可以使用",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"无法使用",Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(this, "请至权限中心打开本应用的文件访问权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
