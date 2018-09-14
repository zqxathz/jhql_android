package com.jhlotus.vine;

import java.text.SimpleDateFormat;
import java.util.Date;

public class activitylist {
    private int id;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private String title;
    private String expo;
    private String place;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getExpo() {
        return expo;
    }

    public void setExpo(String expo) {
        this.expo = expo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(long startmilSecond,long endmilSecond) {
        String pattern = "yyyy年M月d日";
        Date date1 = new Date((startmilSecond+100000)*1000);

        Date date2 = new Date((endmilSecond+100000)*1000);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        this.date = format.format(date1)+"-"+format.format(date2);
    }
}
