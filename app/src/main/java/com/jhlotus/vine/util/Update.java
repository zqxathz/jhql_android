package com.jhlotus.vine.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cretin.www.cretinautoupdatelibrary.interfaces.ForceExitCallBack;
import com.cretin.www.cretinautoupdatelibrary.utils.CretinAutoUpdateUtils;
import com.jhlotus.vine.R;

import java.util.ArrayList;
import java.util.List;

public class Update implements ForceExitCallBack {
    private static Update instance;



    Context context;
    //private boolean isupdate=false;
    private CretinAutoUpdateUtils cretinAutoUpdateUtils;

    private Update(){}

    /**
     * 单一实例
     */
    public static  Update getUpdate(Context c){

        if(instance==null){
            instance=new Update();
        }
        instance.context = c;
        return instance;
    }



    public synchronized void checkupdate(boolean force){

        CretinAutoUpdateUtils.Builder builder = new CretinAutoUpdateUtils.Builder()
                //设置更新api
                .setBaseUrl("https://www.jhlotus.com/other/index/version")
                //设置是否显示忽略此版本
                .setIgnoreThisVersion(!force)
                //设置下载显示形式 对话框或者通知栏显示 二选一
                .setShowType(CretinAutoUpdateUtils.Builder.TYPE_DIALOG)
                //设置下载时展示的图标
                .setIconRes(R.mipmap.ic_launcher)
                //设置是否打印log日志
                .showLog(true)
                //设置请求方式
                .setRequestMethod(CretinAutoUpdateUtils.Builder.METHOD_GET)
                //设置下载时展示的应用名称
                .setAppName("京湖青莲现场管理")
                //设置自定义的Model类
                //.setTransition(new UpdateModel())
                .build();
        CretinAutoUpdateUtils.init(builder);



        if (cretinAutoUpdateUtils==null){
            cretinAutoUpdateUtils = CretinAutoUpdateUtils.getInstance(context);
        }else{
            cretinAutoUpdateUtils.destroy();
            cretinAutoUpdateUtils = CretinAutoUpdateUtils.getInstance(context);
            cretinAutoUpdateUtils.setContext(context);

        }

        /*if (force){
            *//*SharedPreferences sp = context.getSharedPreferences("ingoreList", 0);
            SharedPreferences.Editor mEdit1 = sp.edit();
            mEdit1.clear();
            mEdit1.commit();*//*
            List<String> list = new ArrayList<>();
            cretinAutoUpdateUtils.saveArray(list);

        }*/
        CretinAutoUpdateUtils.setRunning();

        cretinAutoUpdateUtils.check(this,false);





    }

    @Override
    public void exit() {
        ((Activity) context).finish();
    }
}
