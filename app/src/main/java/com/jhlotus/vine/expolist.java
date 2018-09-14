package com.jhlotus.vine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class expolist {
    private int id;
    private String title;
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

    public String getDate() {
        return date;
    }

    public void setDate(long startmilSecond,long endmilSecond) {
        String pattern = "yyyy年M月d日";
        Date date1 = new Date((startmilSecond)*1000);

        Date date2 = new Date((endmilSecond)*1000);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        format.setTimeZone(timeZone);
        this.date = format.format(date1)+"-"+format.format(date2);
    }
}
