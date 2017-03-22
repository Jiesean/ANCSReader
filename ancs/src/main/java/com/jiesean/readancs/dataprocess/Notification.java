package com.jiesean.readancs.dataprocess;

import java.io.Serializable;

/**
 * Created by tstz4 on 16-11-3.
 */
public class Notification implements Serializable {

    private int eventId;
    private byte eventFlags;

    private String category;
    private int categoryId;
    private int num;
    //４位16进制的数字
    private byte[] Nid;
    //整数标示的nid
    private int nid;

    private String title;
    private String message;

    //存在的动作，0为无动作，１为positive,2为negative,3是都存在
    private int action;

    //catsgory
    public static String[] categorys = {
            "OTHER", "INCOMING_CALL", "MISSED_CALL",
            "VOICE_CALL", "SOCIAL", "SCHEDULE",
            "EMAIL", "HEALTH_AND_FITNESS", "NEWS",
            "BUSSINESS_AND_FINANCE", "LOCATION", "ENTERTAINMENT"
    };

    public Notification(byte[] data) {
        this.eventId = Integer.parseInt(String.format("%d", data[0]));
        this.eventFlags = data[1];
        categoryId = Integer.parseInt(String.format("%d", data[2]));
        this.category = categorys[categoryId];
        this.num = Integer.parseInt(String.format("%d", data[3]));
        this.Nid = new byte[]{data[4], data[5], data[6], data[7]};

    }

    public boolean isPreExisting() {
        return ((eventFlags & 0x06) > 0) ? true : false;
    }

    public int getAction() {

        action = 0;
        //positive标志位为１
        if ((eventFlags & 0x08) > 0) {
            action = action + 1;
        }
        //negative标志位为１
        if ((eventFlags & 0x10) > 0) {
            action = action + 2;
        }
        return action;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCategory(int categoryId) {
        this.category = categorys[categoryId];
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setNid(byte[] nid) {
        Nid = nid;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getCategory() {
        return category;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getNum() {
        return num;
    }

    public int getEventId() {
        return eventId;
    }

    public byte[] getNid() {
        return Nid;
    }

    public int getIntegerNid() {
        System.out.println(String.format("%d", Nid[0]));
        nid = Integer.parseInt(String.format("%d", Nid[0]) + String.format("%d", Nid[1]) + String.format("%d", Nid[2]) + String.format("%d", Nid[3]));
        return nid;
    }
}
