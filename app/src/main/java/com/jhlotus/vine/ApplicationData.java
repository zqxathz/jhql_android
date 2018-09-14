package com.jhlotus.vine;

import android.app.Application;
import android.content.Context;

public class ApplicationData extends Application {
    private String token;
    private String session;
    private String mobile;

    private static ApplicationData instance;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getToken(){
        return token;
    }
    public void setToken(String s){
        this.token = s;
    }

    public String getSession(){
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        this.token = "";
        this.session = "";
    }

    public static Context getMyApplication() {
        return instance;
    }
}
