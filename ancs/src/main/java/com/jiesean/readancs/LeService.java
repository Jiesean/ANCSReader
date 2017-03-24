package com.jiesean.readancs;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.jiesean.readancs.dataprocess.DataHandler;

import java.util.UUID;

/**
 * 负责扫描和连接gatt,并处理ANCS　response
 */
public class LeService extends Service {
    //TAG
    private static final String TAG = Constants.AUTHOR + "LeService";

    //自定义binder，用于service绑定activity之后为activity提供操作service的接口
    private LocalBinder mBinder = new LocalBinder();

    //bluetooth
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private BluetoothDevice mIphoneDevice;
    private BluetoothGatt mConnectedGatt;
    private BluetoothGattService mANCSService;
    private BluetoothGattCharacteristic mNotificationSourceChar;
    private BluetoothGattCharacteristic mPointControlChar;
    private BluetoothGattCharacteristic mDataSourceChar;

    //log constants
    private static final String BLUETOOTH_NOT_ENABLED = "bluetooth not support";
    private static final String BLUETOOTH_ENABLED = "bluetooth adpter enabled";

    //callback
    private LocalScanCallBack mScanCallback = new LocalScanCallBack();
    private LocalBluetoothGattCallback mGattCallback = new LocalBluetoothGattCallback();

    //data source respose packet handler
    private DataHandler mDataHandler;

    //data
    private byte[] uid = new byte[4];

    //broadcast
    private Intent mNotificationIntent;
    private Intent mDataIntent;
    private Intent mStateIntent;

    com.jiesean.readancs.dataprocess.Notification notification;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "service onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "service onCreate");
        //init bluetoothadapter.api 18 above
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //bluetooth is enabled or not
        if (mBluetoothAdapter != null) {
            Log.d(TAG, BLUETOOTH_ENABLED);
        } else {
            Log.d(TAG, BLUETOOTH_NOT_ENABLED);
        }

        //data source 解析类
        mDataHandler = new DataHandler();
        //广播intent
        mNotificationIntent = new Intent();
        mNotificationIntent.setAction(Constants.ACTION_NOTIFICATION_SOURCE);
        mDataIntent = new Intent();
        mDataIntent.setAction(Constants.ACTION_DATA_SOURCE);
        mStateIntent = new Intent();
        mStateIntent.setAction(Constants.ACTION_STATE_INFO);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "service onDestroy");
        if (mConnectedGatt != null) {
            mConnectedGatt.close();
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter = null;
        }
    }

    /**
     * 继承Binder类，实现localbinder,为activity提供操作接口
     */
    public class LocalBinder extends Binder {

        public void startLeScan() {
            Log.d(TAG, "startLeScan");

            if (mBluetoothAdapter.getState() ==BluetoothAdapter.STATE_OFF) {
                mBluetoothAdapter.enable();
            } else {
                if (mConnectedGatt != null) {
                    Log.d(TAG, "连接存在");
                } else {
                    //获取leScanner
                    //api above 21
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    //开始扫描，并将扫描结果交给mScanCallback处理
                    mBluetoothLeScanner.startScan(mScanCallback);
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void connectToGattServer() {
            if (mIphoneDevice != null) {
                Log.d(TAG, "connect gatt");
                mIphoneDevice.connectGatt(getApplicationContext(), false, mGattCallback);
            }
        }

        public void negativeResponseToNotification(byte[] nid) {

            byte[] action = {
                    (byte) 0x02,
                    //UID
                    nid[0], nid[1], nid[2], nid[3],
                    //action id
                    (byte) 0x01,

            };

            //如果已经绑定，而且此时未断开
            if (mConnectedGatt != null) {
                BluetoothGattService service = mConnectedGatt.getService(UUID.fromString(Constants.service_ancs));
                if (service == null) {
                    Log.d(TAG, "cant find service");
                } else {
                    Log.d(TAG, "find service");
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Constants.characteristics_control_point));
                    if (characteristic == null) {
                        Log.d(TAG, "cant find chara");
                    } else {
                        Log.d(TAG, "find chara");
                        characteristic.setValue(action);
                        mConnectedGatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }

        public void positiveResponseToNotification(byte[] nid) {

            byte[] action = {
                    (byte) 0x02,
                    //UID
                    nid[0], nid[1], nid[2], nid[3],
                    //action id
                    (byte) 0x00,

            };

            //如果已经绑定，而且此时未断开
            if (mConnectedGatt != null) {
                BluetoothGattService service = mConnectedGatt.getService(UUID.fromString(Constants.service_ancs));
                if (service == null) {
                    Log.d(TAG, "cant find service");
                } else {
                    Log.d(TAG, "find service");
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Constants.characteristics_control_point));
                    if (characteristic == null) {
                        Log.d(TAG, "cant find chara");
                    } else {
                        Log.d(TAG, "find chara");
                        characteristic.setValue(action);
                        mConnectedGatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }

        public void retrieveMoreInfo(byte[] nid) {

            byte[] getNotificationAttribute = {
                    (byte) 0x00,
                    //UID
                    nid[0], nid[1], nid[2], nid[3],

                    //title
                    (byte) 0x01, (byte) 0xff, (byte) 0xff,
                    //message
                    (byte) 0x03, (byte) 0xff, (byte) 0xff
            };


            //如果已经绑定，而且此时未断开
            if (mConnectedGatt != null) {
                BluetoothGattService service = mConnectedGatt.getService(UUID.fromString(Constants.service_ancs));
                if (service == null) {
                    Log.d(TAG, "cant find service");
                } else {
                    Log.d(TAG, "find service");
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Constants.characteristics_control_point));
                    if (characteristic == null) {
                        Log.d(TAG, "cant find chara");
                    } else {
                        Log.d(TAG, "find chara");
                        characteristic.setValue(getNotificationAttribute);
                        mConnectedGatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }
    }


    private class LocalScanCallBack extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult Device Address :" + result.getDevice());

            BluetoothDevice device = result.getDevice();

            if (device != null && device.getName().equals(Constants.DEVICE_NAME)) {
                mStateIntent.putExtra("state", Constants.DEVICE_FIND);
                sendBroadcast(mStateIntent);
                mIphoneDevice = device;

                //已经绑定，该设备在绑定的设备名单里面
                if (mBluetoothAdapter.getBondedDevices().contains(device)) {

                    device.connectGatt(getApplicationContext(), false, mGattCallback);
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
                else {//未绑定的设备
                    device.createBond();
                }
            }
        }
    }

    /**
     * 连接gatt server结果处理回调类
     */
    private class LocalBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "connected");

                mStateIntent.putExtra("state", Constants.CONNECT_SUCCESS);
                sendBroadcast(mStateIntent);

                mConnectedGatt = gatt;
                gatt.discoverServices();
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "disconnected");

                mConnectedGatt = null;
                mStateIntent.putExtra("state", Constants.DISCONNECTED);
                sendBroadcast(mStateIntent);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService ancsService = gatt.getService(UUID.fromString(Constants.service_ancs));
                if (ancsService != null) {
                    Log.d(TAG, "ANCS_FIND");
                    mANCSService = ancsService;
                    mDataSourceChar = ancsService.getCharacteristic(UUID.fromString(Constants.characteristics_data_source));
                    mPointControlChar = ancsService.getCharacteristic(UUID.fromString(Constants.characteristics_control_point));
                    mNotificationSourceChar = ancsService.getCharacteristic(UUID.fromString(Constants.characteristics_notification_source));
                    gatt.setCharacteristicNotification(mDataSourceChar, true);
                    BluetoothGattDescriptor descriptor = mDataSourceChar.getDescriptor(
                            UUID.fromString(Constants.descriptor_config));
                    if (descriptor != null) {
                        Log.d(TAG, "write descriptor");
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    } else {
                        Log.d(TAG, "gatt disconnect");
                    }
                } else {
                    Log.d(TAG, "ANCS_NOT_FIND");
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, " onDescriptorWrite:: " + status);
            Log.d(TAG, " BluetoothGatt.GATT_SUCCESS:: " + BluetoothGatt.GATT_SUCCESS);

            // Notification source
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor.getCharacteristic().getUuid().equals(UUID.fromString(Constants.characteristics_data_source))) {
                    Log.d(TAG, "notification_source　订阅成功 ");
                    if (Constants.characteristics_notification_source.equals(mNotificationSourceChar.getUuid().toString())) {
                        gatt.setCharacteristicNotification(mNotificationSourceChar, true);
                        BluetoothGattDescriptor notify_descriptor = mNotificationSourceChar.getDescriptor(
                                UUID.fromString(Constants.descriptor_config));
                        if (descriptor == null) {
                            Log.d(TAG, " not find desc :: " + notify_descriptor.getUuid());
                        } else {
                            notify_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(notify_descriptor);
                        }
                    }
                }
                if (descriptor.getCharacteristic().getUuid().equals(UUID.fromString(Constants.characteristics_notification_source))) {
                    Log.d(TAG, "data_source　订阅成功 ");
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite");
            if (Constants.characteristics_notification_source.toString().equals(characteristic.getUuid().toString())) {
                Log.d(TAG, "notification_source  Write successful");
            }
            if (Constants.characteristics_control_point.toString().equals(characteristic.getUuid().toString())) {
                Log.d(TAG, "control_point  Write successful");

            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (Constants.characteristics_notification_source.toString().equals(characteristic.getUuid().toString())) {
                Log.d(TAG, "notification_source Changed");
                byte[] nsData = characteristic.getValue();
                notification = new com.jiesean.readancs.dataprocess.Notification(nsData);
                mNotificationIntent.putExtra("notice", notification);
                System.out.println("EventId:" + String.format("%d", nsData[0]) + "\n" +
                        "EventFlags:" + String.format("%02x", nsData[1]) + "\n" +
                        "Category id:" + String.format("%d", nsData[2]) + "\n" +
                        "Category Count:" + String.format("%d", nsData[3]) + "\n" +
                        "NotificationUId:" + String.format("%02X", nsData[4]) + String.format("%02X", nsData[5]) + String.format("%02X", nsData[6]) + String.format("%02X", nsData[7]) + "\n"
                );

                if ((nsData[0] & 0x02) > 0) {
                    sendBroadcast(mNotificationIntent);
                } else {
                        byte[] getNotificationAttribute = {
                                (byte) 0x00,
                                //UID
                                nsData[4], nsData[5], nsData[6], nsData[7],
//                            //app id
//                            (byte) 0x00,
                                //title
                                (byte) 0x01, (byte) 0xff, (byte) 0xff,
                                //message
                                (byte) 0x03, (byte) 0xff, (byte) 0xff
                        };

                        if (mConnectedGatt != null) {
                            mPointControlChar.setValue(getNotificationAttribute);
                            mConnectedGatt.writeCharacteristic(mPointControlChar);
                        }
                }

            }
            if (Constants.characteristics_data_source.toString().equals(characteristic.getUuid().toString())) {
                Log.d(TAG, "characteristics_data_source changed");

                byte[] get_data = characteristic.getValue();

                if (!mDataHandler.isStarted()) {
                    mDataHandler.init(get_data);
                } else {
                    mDataHandler.appendData(get_data);
                }
                if (mDataHandler.isFinished()) {
                    mDataHandler.parseData();
                    System.out.println("**title" + mDataHandler.getAttributeTitle());

                    notification.setTitle(mDataHandler.getAttributeTitle());
                    notification.setMessage(mDataHandler.getAttributeMessage());
                    mDataIntent.putExtra("content", notification);
                    sendBroadcast(mDataIntent);

                    mDataHandler = new DataHandler();
                }
            }

            if (Constants.characteristics_control_point.toString().equals(characteristic.getUuid().toString())) {
                Log.d(TAG, "characteristics_control_point changed");
                byte[] cpData = characteristic.getValue();
            }
        }
    }


}
