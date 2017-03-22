package com.jiesean.readancs.dataprocess;

import java.util.HashMap;

/**
 * data source packet
 *
 * 暂时未用到
 */
public class DataPacket {
    private byte[] uid = new byte[4];

    int index = 0;
    HashMap<Integer, Byte> mPacket = new HashMap<>();

    public DataPacket(byte[] data){
        uid[0] = data[1];
        uid[1] = data[2];
        uid[2] = data[3];
        uid[3] = data[4];
        for (int i = 0; i < data.length; i++) {
            mPacket.put(i, data[i]);
        }
        index = data.length;
    }

    public void addData(byte[] data){
        for (int i = index; i < data.length+index; i++) {
            mPacket.put(i,data[i-index+5]);
        }
    }

    /**
     * attribute类
     */
    private class Attribute{
        private byte aid;
        private String name;
        private int length;
        private String content;

        public Attribute(byte[] data){

        }

        public byte getAid(){
            return aid;
        }
        public String getName(){
            switch (aid){
            }
            return name;
        }
        public int getLength(){
            return length;
        }
        public String getContent(){
            return content;
        }

    }
}
