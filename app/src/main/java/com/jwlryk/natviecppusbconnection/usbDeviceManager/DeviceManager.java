package com.jwlryk.natviecppusbconnection.usbDeviceManager;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import com.jwlryk.natviecppusbconnection.usbDeviceElement.USBConnectionListener;
import com.jwlryk.natviecppusbconnection.usbDeviceElement.USBMonitoringService;
import com.jwlryk.natviecppusbconnection.util.Dlog;

public class DeviceManager {
    private static DeviceManager mDeviceManagerInstance = null;
    private static final Object mSyncBlock = new Object();

    private static final int mVendorID = 0x04B4;
    private static final int mProductID = 0x00F1;

    //private static final int mVendorID = 0x05AC;
    //private static final int mProductID = 0x166A;

    private static final String ACTION_USB_PERMISSION = "com.jwlryk.natviecppusbconnection.USB_PERMISSION";

    public static final int MSG_USB_CONNECTION = 0;
    public static final int MSG_USB_DISCONNECTION = 1;
    private Handler mDeviceHandler;
    private final Object mDeviceHandlerBlockObject;

    private DeviceManager() {
        mDeviceHandler = null;
        mDeviceHandlerBlockObject = new Object();
    }

    public static DeviceManager getInstance() {
        if(mDeviceManagerInstance != null) {
            return mDeviceManagerInstance;
        }

        synchronized (mSyncBlock) {
            if(mDeviceManagerInstance == null) {
                mDeviceManagerInstance = new DeviceManager();
            }
        }
        return mDeviceManagerInstance;
    }

    public DeviceCommunicator CreateDeviceCommunicator(Context context, UsbDevice device) throws IOException {
        return new DeviceCommunicator(context, device);
    }

    public void loadUSBConnectEventHandler(Handler handler) {
        synchronized (mDeviceHandlerBlockObject) {
            mDeviceHandler = handler;
        }

    }

    public void unloadUSBConnectionEventHandler(Handler handler) {
        synchronized (mDeviceHandlerBlockObject) {
            mDeviceHandler = null;
        }
    }

    public void DeviceManagerMonitoringClear() {
        USBMonitoringService.getInstance().USBMonitorClear();
    }

    public void DeviceManagerMonitoringStart(Context context, Handler handler) {
        synchronized (mDeviceHandlerBlockObject) {
            mDeviceHandler = handler;
        }

        USBMonitoringService.getInstance().USBMonitorStart(context, mVendorID, mProductID, ACTION_USB_PERMISSION, new USBConnectionListener() {
            @Override
            public void onUsbConnected(UsbDevice device) {
                Dlog.i("USB Device Manager Connection Attached");
                synchronized (mDeviceHandlerBlockObject)
                {
                    if(mDeviceHandler != null) {
                        Message msg = new Message();
                        msg.what = MSG_USB_CONNECTION;
                        msg.obj = device;
                        mDeviceHandler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void onUsbDetached(UsbDevice device) {
                Dlog.i("USB Device Manager Connection Detached");
                synchronized (mDeviceHandlerBlockObject)
                {
                    if(mDeviceHandler != null) {
                        Message msg = new Message();
                        msg.what = MSG_USB_DISCONNECTION;
                        msg.obj = device;
                        mDeviceHandler.sendMessage(msg);
                    }
                }
            }
        });
    }

    //DeviceComunicator
}
