package com.jiesean.readancs;

/**
 * 存储常用的常数
 * Created by tstz4 on 16-10-27.
 */
public class Constants {
    public static final String AUTHOR = "jiesean";
    //uuidx0f-4b1e122d00d0";
    public static final String service_ancs = "7905f431-b5ce-4e99-a40f-4b1e122d00d0";
    public static final String characteristics_notification_source = "9fbf120d-6301-42d9-8c58-25e699a21dbd";
    public static final String characteristics_data_source = "22eac6e9-24d6-4bb5-be44-b36ace7c7bfb";
    public static final String characteristics_control_point = "69d1d8f3-45e1-49a8-9821-9bbdfdaad9d9";
    public static final String descriptor_config = "00002902-0000-1000-8000-00805f9b34fb";
    //broadcast action
    public static final String ACTION_NOTIFICATION_SOURCE = "notification";
    public static final String ACTION_DATA_SOURCE = "data";
    public static final String ACTION_STATE_INFO = "state";
    //device name
    //scan iphone bluetooth service through this vertiual peripherial, you can use others
    public static final String DEVICE_NAME = "Heart Rate";

    //state
    public static final String BOND_FAIL = "BOND_FAIL";
    public static final String DEVICE_FIND = "DEVICE_FIND";
    public static final String CONNECT_SUCCESS = "CONNECT_SUCCESS";
    public static final String DISCONNECTED = "DISCONNECTED";
    public static final String NO_BLUETOOTH = "NO_BLUETOOTH";

    //category icon
    public static final int[] category_icons = new int[]{
            R.drawable.other, R.drawable.incoming_call, R.drawable.missed_call,
            R.drawable.voice_call, R.drawable.social, R.drawable.schedule,
            R.drawable.email, R.drawable.fitness, R.drawable.news,
            R.drawable.bussiness,R.drawable.location,R.drawable.entertainment
    };

}
