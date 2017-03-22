package com.jiesean.readancs.dataprocess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jiesean
 * <p/>
 * data source 数据包处理类
 * <p/>
 */
public class DataHandler {

    //存存储已经收集到的数据包，以byte为单位存储
    private ArrayList<Byte> remainData = new ArrayList<>();
    //标记是否开始收集
    private boolean isStarted = false;

    //标记是否在被使用
    private boolean isUsed = false;


    //存储notification nid
    private byte[] nid;
    //attribute 1   title
    private String attributeTitle;
    //attribute 2  content
    private String attributeMessage;

    //中间转换
    private ByteArrayOutputStream title_value = new ByteArrayOutputStream();
    private ByteArrayOutputStream message_value = new ByteArrayOutputStream();

    //标记每次收集完后，仍然剩余的byte的数量
    private int leftLength;
    //在index个可以获得attribute的长度
    private int getAttribute2LengthIndex = 1;

    //结束的byte位置
    private int endIndex;
    //当前byte计数累计
    private int index;
    //attribute所占的byte字节数
    private int attribute1Length;
    private int attribute2Length;

    /**
     * 初始化dataHandler
     *
     * @param data 每次data source返回的数据
     */
    public void init(byte[] data) {
        isStarted = true;
        index = 1;
        endIndex = -1;
        leftLength = -1;
        attribute1Length = -1;
        attribute2Length = -1;
        isUsed = true;

        setRemainData(data);
        //nid
        nid = new byte[4];
        nid[0] = data[1];
        nid[1] = data[2];
        nid[2] = data[3];
        nid[3] = data[4];

        attribute1Length = getAttributeLength(data[7], data[6]);

        getAttribute2LengthIndex = ((int) (11 + attribute1Length) / 20) + 1;

        if (getAttribute2LengthIndex == 1) {
            attribute2Length = getAttributeLength(data[10 + attribute1Length], data[9 + attribute1Length]);
            endIndex = ((int) (11 + attribute1Length + attribute2Length) / 20) + 1;

            if (endIndex == 1) {
                leftLength = 0;
            }
        }
    }


    /**
     * 继续收集data
     *
     * @param appendData
     */
    public void appendData(byte[] appendData) {
        setRemainData(appendData);
        index++;
        if (endIndex == index) {
            leftLength = 0;
        } else {
            if (index == getAttribute2LengthIndex) {
                int highIndex = (int) (10 + attribute1Length);
                attribute2Length = getAttributeLength(remainData.get(highIndex), remainData.get(highIndex - 1));
                System.out.println("%%%%%" + attribute2Length);
                endIndex = ((int) (11 + attribute1Length + attribute2Length) / 20) + 1;
                if (endIndex == index) {
                    leftLength = 0;
                }
            } else {
                if (endIndex == index) {
                    leftLength = 0;
                }
            }
        }

    }

    /**
     * 是否初始化dataHandler
     *
     * @return
     */
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * 返回数据包是否收集完成
     *
     * @return 是否完成收集
     */
    public boolean isFinished() {
        if ((leftLength == 0 ? true : false)) {
            isUsed = false;
        }
        return (leftLength == 0 ? true : false);
    }

    /**
     * 解析属性的长度
     *
     * @param high 　高位，即数据包的高位
     * @param low  　低位，数据包的低位
     * @return 属性值的整数形式
     */
    private int getAttributeLength(byte high, byte low) {
        int length = 0;
        length = Integer.parseInt(String.format("%02x", high) + String.format("%02x", low), 16);
        return length;
    }

    /**
     * 解析完整的数据包
     */
    public void parseData() {
        byte[] temp = new byte[attribute1Length];
        byte[] temp1 = new byte[attribute2Length];
        for (int i = 8; i < 8 + attribute1Length; i++) {
            temp[i - 8] = remainData.get(i);
        }
        for (int i = (11 + attribute1Length); i < (11 + attribute1Length + attribute2Length); i++) {
            temp1[i - (11 + attribute1Length)] = remainData.get(i);
        }

        try {
            title_value.write(temp);
            message_value.write(temp1);

            attributeTitle = new String(title_value.toByteArray(), "UTF-8");
            attributeMessage = new String(message_value.toByteArray(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 将得到的数据包按照字节进行依次存储
     *
     * @param Data
     */
    public void setRemainData(byte[] Data) {
        for (int i = 0; i < Data.length; i++) {
            remainData.add(Data[i]);
        }
    }


    //****************get函数接口*************
    public boolean isUsed() {
        return isUsed;
    }

    public String getAttributeTitle() {
        return attributeTitle;
    }

    public String getAttributeMessage() {
        return attributeMessage;
    }
    //****************get函数接口*************


}
