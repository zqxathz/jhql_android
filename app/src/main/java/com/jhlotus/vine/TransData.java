package com.jhlotus.vine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TransData extends AppCompatActivity {
    private Handler handler;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
    public TransData() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public class MyRunnable implements Runnable {
        private String code;
        public void setCode(String code)
        {
            this.code = code;
        }
        private int oid;

        public void setOid(int oid) {
            this.oid = oid;
        }

        @Override
        public void run() {
            ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();
            //String mo = "18606829855";
            //appdata.setMobile(mo);

            String mobile = appdata.getMobile();
            Log.d("mobile:" ,mobile);
            Log.d("code:",this.code);

            OkHttpClient client = new OkHttpClient();
            FormBody body = new FormBody.Builder()
                    .add("mobile",mobile)
                    .add("code",this.code)
                    .add("oid",this.oid+"")
                    //.add("__token__",appdata.getToken())
                    .add("appclient","1")
                    .build();
            Request request = new Request.Builder()
                    //.addHeader("cookie",appdata.getSession())
                    .url("https://www.jhlotus.com/crm/locale/visitorenter")
                    .post(body)
                    .build();

            Message msg = new Message();
            Bundle data = new Bundle();
            msg.what = 1;

            try{
                Call call2 = client.newCall(request);
                Response res = call2.execute();
                String res_body = res.body().string();
                //Log.d("body:",res_body);
                JSONObject json  = new JSONObject(res_body);
                String response = json.getString("response");
                if (response.equals("success")){
                    data.putInt("value",1);
                }else{
                    data.putInt("value",0);
                }

            }catch (Exception e){
                data.putInt("value",0);
                Log.d("error:",e.toString());
            }

            msg.setData(data);
            handler.sendMessage(msg);

        }
    }




    public int visitorEnter(String code,int oid){

        final String f_code = code;
        final int f_oid = oid;
        MyRunnable myRunnable = new MyRunnable();
        myRunnable.setCode(code);
        myRunnable.setOid(oid);
        new Thread(myRunnable).start();
        return 1;

    }
}
