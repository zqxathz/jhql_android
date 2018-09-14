package com.jhlotus.vine;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.jhlotus.vine.util.Common;
import com.jhlotus.vine.util.Constant;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

public class ActivitysActivity extends AppCompatActivity implements View.OnClickListener {

    private Bundle bundle;
    private ArrayList<String> data;
    ArrayAdapter<String> adapter;
    private Dialog mDialogLoading;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitys);

        bundle = getIntent().getExtras();
        data = new ArrayList<>();


        findViewById(R.id.btn_activity_scancode).setOnClickListener(this);
        adapter = new ArrayAdapter<String>(
                this, R.layout.layout_log, data);


        ((ListView) findViewById(R.id.lv_log)).setAdapter(adapter);

         new GetLogTasker().execute(bundle.getInt("id"));


    }

    private void initview(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_activity_scancode:
                startQrCode();
                //Toast.makeText(this,"测试一下"+bundle.get("name"),Toast.LENGTH_SHORT).show();
                //new GetLogTasker().execute(bundle.getInt("id"));
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
            Bundle bundle1 = data.getExtras();
            String scanResult = bundle1.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            //Toast.makeText(getApplicationContext(),scanResult,Toast.LENGTH_LONG).show();

            mDialogLoading  = WeiboDialogUtils.createLoadingDialog(ActivitysActivity.this,"处理中...");



            new ValidateUserTasker().execute(scanResult);
            //TransData  transdata =  new TransData();
            //transdata.setHandler(handler);
            //int r = transdata.visitorEnter(scanResult,bundle.getInt("oid"));
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

    private class GetLogTasker extends AsyncTask<Integer,Integer,ArrayList<String>>{
        String session;
        ArrayList<String> list;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();
            session = appdata.getSession();
            list = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            //data = (String[])s.toArray(new String[s.size()]);
            data.clear();
            if (s.get(0)=="error"){
                Common.showErrorDialog(ActivitysActivity.this,"网络错误,无法获取扫码记录");
            }else if (s!=null){
                data.addAll(s);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... params) {
            int id = params[0];
            list = Common.getActivityUser(id,session);
            if (list==null){
                return null;
            }
            return list;
        }
    }

    private class ValidateUserTasker extends AsyncTask<String,Integer,Integer>{
        String code,session,message,mobile;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();
            session = appdata.getSession();
            mobile = appdata.getMobile();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (mDialogLoading.isShowing()){
                mDialogLoading.dismiss();
            }

            if (result==1){
                new AlertDialog.Builder(ActivitysActivity.this)
                        .setTitle("提示")
                        .setMessage("二维码核销成功,请发放实物券")
                        .setCancelable(false)
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                new GetLogTasker().execute(bundle.getInt("id"));
                            }
                        }).create().show();
            }else{
                if (!code.isEmpty()){
                    message = code+":"+message;
                }
                new AlertDialog.Builder(ActivitysActivity.this)
                        .setTitle("提示")
                        .setMessage(message)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            code = strings[0];
            FormBody body = new FormBody.Builder()
                    .add("id",bundle.getInt("id")+"")
                    .add("oid",bundle.getInt("oid")+"")
                    .add("mobile",mobile)
                    .add("code",code)
                    .build();

            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .addHeader("X-Requested-With","XMLHttpRequest")
                    .addHeader("cookie",session)
                    .post(body)
                    .url("https://www.jhlotus.com/activity/user/validateuser")
                    .build();
            //通过client发起请求

            try{
                Response res = client.newCall(request).execute();
                String http_result = res.body().string();
                if (http_result.isEmpty()){
                    code = "";
                    message="服务器无数据返回,请联系管理员";
                    return 0;
                }
                Log.d(TAG, "post: "+http_result);
                JSONObject jsonObject = new JSONObject(http_result);
                int response = jsonObject.optInt("response",0);
                if (response==1){
                    return  response;
                }else{
                    message = jsonObject.optString("message","核销失败");
                    return 0;
                }
            }catch (IOException e){
                code="";
                message = "网络不可用,请确定网络正常";
                return 0;
            }
            catch (Exception e){
                Log.d("error",e.getMessage());
                code="";
                message = "程序异常";
                return 0;
            }
        }
    }
}
