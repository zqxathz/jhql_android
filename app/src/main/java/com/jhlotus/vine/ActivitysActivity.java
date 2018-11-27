package com.jhlotus.vine;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.activity.CaptureActivity;
import com.jhlotus.vine.util.Common;
import com.jhlotus.vine.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
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
        findViewById(R.id.btn_activity_count).setOnClickListener(this);
        adapter = new ArrayAdapter<String>(
                this, R.layout.layout_log, data);


        ListView lv_log = findViewById(R.id.lv_log);
        lv_log.setAdapter(adapter);

        lv_log.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String user_id;
                int index = data.get(position).indexOf("\r\n");
                user_id = data.get(position).substring(3,index);
                showPopwindow(Integer.parseInt(user_id));
                return false;
            }
        });


         new GetLogTasker().execute(bundle.getInt("id"));


    }

    private void initview(){

    }

    private void initEvent(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_activity_scancode:
                startQrCode();
                //Toast.makeText(this,"测试一下"+bundle.get("name"),Toast.LENGTH_SHORT).show();
                //new GetLogTasker().execute(bundle.getInt("id"));
                //new ValidateUserTasker().execute("au0bb598535726aa47ed655b6f1de69af02");
                break;
            case R.id.btn_activity_count:
                new GetCount().execute(bundle.getInt("id"));
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

    private class GetCount extends  AsyncTask<Integer,Integer,ArrayList<String>>{
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
        protected ArrayList<String> doInBackground(Integer... params) {
            int id = params[0];
            list = Common.getActivityUserCount(id,session);
            if (list==null){
                return null;
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            //data = (String[])s.toArray(new String[s.size()]);
            if (s!=null){
                if (s.get(0)=="error"){
                    Common.showErrorDialog(ActivitysActivity.this,"网络错误,无法获取扫码记录");
                }else if (s!=null){
                    String[] array = (String[])s.toArray(new String[s.size()]);
                    new AlertDialog.Builder(ActivitysActivity.this).setItems(array,null).create().show();
                }
            }
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
            if (s!=null){
                if (s.get(0)=="error"){
                    Common.showErrorDialog(ActivitysActivity.this,"网络错误,无法获取扫码记录");
                }else if (s!=null){
                    data.addAll(s);
                    adapter.notifyDataSetChanged();
                }
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
        String code,session,message,mobile,img_url;
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitysActivity.this);
                View view = LayoutInflater.from(ActivitysActivity.this).inflate(R.layout.dialog_activitys_prompt, null);
                ImageView imgv = view.findViewById(R.id.dlg_imageview_goodimg);
                TextView tv_goodname = view.findViewById(R.id.dlg_tv_good_name);
                TextView tv_message = view.findViewById(R.id.dlg_tv_message);
                tv_goodname.setText(message);
                tv_message.setText("二维码核销成功,请发放实物");
                Glide.with(ActivitysActivity.this).load(img_url).placeholder(R.drawable.place).into(imgv);

                builder.setView(view).setTitle("提示")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                final Dialog dg = builder.create();
                dg.show();

               /* new AlertDialog.Builder(ActivitysActivity.this)
                        .setTitle("提示")
                        .setMessage("二维码核销成功,请发放实物券")
                        .setCancelable(false)
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                new GetLogTasker().execute(bundle.getInt("id"));
                            }
                        }).create().show();*/
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
                    img_url = jsonObject.optString("message","");
                    message = jsonObject.optString("message1","");
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

    private void showPopwindow(final int userid) {

        // 利用layoutInflater获得View
        //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View view = inflater.inflate(R.layout.layout_activity_logitem, null);

        View view=LayoutInflater.from(this).inflate(R.layout.layout_activity_logitem, null, false);

        // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

        final PopupWindow window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        window.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        window.setBackgroundDrawable(dw);

        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.mypopwindow_anim_style);
        // 在底部显示
        window.showAtLocation(ActivitysActivity.this.findViewById(R.id.lv_log), Gravity.BOTTOM, 0, 0);

        // 这里检验popWindow里的button是否可以点击
        Button first = (Button) view.findViewById(R.id.btn_setuser_block);
        first.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               setUserBlock(userid);
               window.dismiss();
            }
        });

        // popWindow消失监听方法
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                System.out.println("popWindow消失");
            }
        });

    }

    private void setUserBlock(int userid){
        Log.d("debug","测试一下"+userid);
        ApplicationData appdata = (ApplicationData)getApplication();
        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("mobilephone",appdata.getMobile())
                .add("userid",Integer.toString(userid))
                .add("activityid",Integer.toString(bundle.getInt("id")))
                .add("appclient","1")
                .build();
        Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",appdata.getSession())
                .url("https://www.jhlotus.com/activity/user/setblock")
                .post(body)
                .build();

        Call call2 = client.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("info_call2fail",e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){

                    try {
                        //Log.i("info_call2success",response.body().string());
                        String str = response.body().string();
                        JSONObject jsonObject = new JSONObject(str);
                        String res = jsonObject.getString("response");
                        final String msg = jsonObject.getString("message");

                        if (res.equals("success")){
                            ActivitysActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    new GetLogTasker().execute(bundle.getInt("id"));

                                }
                            }));
                        }else{
                            ActivitysActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            }));
                        }
                    } catch (JSONException e) {
                        Log.d("出错",e.getMessage());
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    /*private class BlockuserTasker extends AsyncTask<Integer,Integer,Integer>{
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            return null;
        }
    }*/
}
