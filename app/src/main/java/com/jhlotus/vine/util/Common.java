package com.jhlotus.vine.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.jhlotus.vine.ApplicationData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

public class Common {
    static public String getSession(String old_session){
        //ApplicationData appdata = (ApplicationData)getApplication();
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",old_session)
                .get()
                .url("https://www.jhlotus.com/crm/index/refreashsession")
                .build();
        //通过client发起请求

        try{
            Response res = client.newCall(request).execute();
            Log.d(TAG, "getSession: "+res.body().string());
            JSONObject json  = new JSONObject(res.body().string());
            String response = json.getString("response");
            String mobile = json.getString("mobile");
            Log.d(TAG, "getMobile: "+mobile);
            return mobile;
        }catch (Exception e){
            return null;
        }
    }

    static public void showErrorDialog(Context context,String msg){
        new AlertDialog.Builder(context)
                .setTitle("错误")
                .setMessage(msg)
                .setCancelable(true)
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    static public ArrayList<String> getActivityUser(int id,String session){
        ArrayList<String> result = new ArrayList<String>();
        ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();

        FormBody body = new FormBody.Builder()
                .add("mobile",appdata.getMobile())
                .add("id",""+id)
                .add("appclient","1")
                .build();

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",session)
                .post(body)
                .url("https://www.jhlotus.com/activity/locale/getlogs")
                .build();
        //通过client发起请求

        try{
            Response res = client.newCall(request).execute();
            String http_result = res.body().string();

            if (http_result.isEmpty()){
                return null;
            }

            Log.d(TAG, "getSession: "+http_result);
            JSONArray json_array = new JSONArray(http_result);



            if (json_array.length()==0){
                return null;
            }

            for (int i = 0; i < json_array.length(); i++) {
                JSONObject jsonObject = json_array.getJSONObject(i);
                String log_str = "ID:"+jsonObject.getString("id")+"\r\n领取编码:"+jsonObject.getString("code");
                String log_mobile = jsonObject.getString("mobile");
                if (!log_mobile.isEmpty()){
                    log_str=log_str+"手机号:"+log_mobile;
                }
                log_str=log_str+"\r\n领取时间:"+Common.timetostr(jsonObject.getLong("update_time"));
                //Log.d("str",log_str);
                result.add(log_str);
            }
            //throw new Exception("error");
            return result;
        }catch (Exception e){
            Log.d("error",e.getMessage());
            result.clear();
            result.add("error");
            return result;
        }

    }

    public static boolean isApkInDebug(Context context) {
          try {
                       ApplicationInfo info = context.getApplicationInfo();
                      return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                  } catch (Exception e) {
                       return false;
                   }
    }

    public static String timetostr(long time){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        Date date = new Date((time)*1000);

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");



        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    /**
     * 超长数据LOG显示
     * @param log
     * @param showCount
     */
    public static void showLogCompletion(String log,int showCount){
        if(log.length() >showCount){
            String show = log.substring(0, showCount);
//			System.out.println(show);
            Log.i("TAG", show+"");
            if((log.length() - showCount)>showCount){//剩下的文本还是大于规定长度
                String partLog = log.substring(showCount,log.length());
                showLogCompletion(partLog, showCount);
            }else{
                String surplusLog = log.substring(showCount, log.length());
//				System.out.println(surplusLog);
                Log.i("TAG", surplusLog+"");
            }

        }else{
//			System.out.println(log);
            Log.i("TAG", log+"");
        }
    }

}
