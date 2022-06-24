package com.jwlryk.natviecppusbconnection.usbDeviceElement;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import java.util.HashMap;

import com.jwlryk.natviecppusbconnection.util.Dlog;

public class USBConnector {

    private final Context mContext;
    private final int mVendorID;
    private final int mProductID;
    private final String mClassName;
    private final USBConnectionListener mUSBListener;
    private final UsbManager mUSBManager;

    /**
     * Make USB Connection Element
     * @param context
     * @param vendorID
     * @param productID
     * @param className
     * @param usbListener
     */
    public USBConnector(Context context, int vendorID, int productID, String className, USBConnectionListener usbListener) {

        mContext = context.getApplicationContext();
        mVendorID = vendorID;
        mProductID = productID;
        mClassName = className;
        mUSBListener = usbListener;
        mUSBManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

    }

    /**
     * Create BroadCast Receiver
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName = intent.getAction();
            Dlog.d("USB Broadcast Action Name :" + actionName.toString());

            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(actionName) || UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(actionName)){

                Toast.makeText(context, "USB Attached Action : "+actionName , Toast.LENGTH_SHORT).show();

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device.getVendorId() == mVendorID && device.getProductId() == mProductID) {
                    if(mUSBManager.hasPermission(device)){
                        Dlog.i("USB Connected");
                        mUSBListener.onUsbConnected(device);
                    } else {
                        // #1
                        Dlog.i("USB Need Permission.");
                        USBRequestPermission(device);
                    }
                }

            } else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(actionName) || UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(actionName)){

                Toast.makeText(context, "USB Detached Action : "+actionName , Toast.LENGTH_SHORT).show();

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device.getVendorId() == mVendorID && device.getProductId() == mProductID) {
                    Dlog.i("USB DisConnected");
                    mUSBListener.onUsbDetached(device);
                }
            }
        }
    };

    /**
     * Set BroadCast Receiver.
     * @param device
     */
    // #1
    private void USBRequestPermission(UsbDevice device) {
        Dlog.i("USB Permission Init");
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0, new Intent(mClassName), PendingIntent.FLAG_MUTABLE);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mContext.unregisterReceiver(this);
                if(intent.getAction().equals(mClassName)) {
                    //Dlog.i("USB Permission getAction Class");
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        //Dlog.i("USB Permission UsbManager.EXTRA_PERMISSION_GRANTED" );

                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if(device != null) {
                            //Dlog.i("USB Permission Device Not Null Check");

                            if(device.getVendorId() == mVendorID && device.getProductId() == mProductID) {

                                Dlog.i("USB Permission Complete");
                                mUSBListener.onUsbConnected(device);
                            }
                        }
                    }
                }
            }
        }, new IntentFilter(mClassName));
        mUSBManager.requestPermission(device, intent);
    }

    /**
     * UnSet BroadCst Receiver.
     */
    // #2
    public void USBLoadDeviceList() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        Dlog.i("USB Device Load Loading...");
        HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
        for (UsbDevice device : deviceList.values() ) {
            Dlog.d("Device List : " + String.format("Vendor_ID : %04X / Product_ID : %04X", device.getVendorId(), device.getProductId()));
            Toast.makeText(mContext.getApplicationContext(), String.format("Vendor_ID : %04X / Product_ID : %04X", device.getVendorId(), device.getProductId()), Toast.LENGTH_LONG).show();
            if(device.getVendorId() == mVendorID && device.getProductId() == mProductID) {
                Dlog.i("USB Device Already Connected!");
                Dlog.d("USB Device Info : " + device.getDeviceName());
                if(!mUSBManager.hasPermission(device)){
                    USBRequestPermission(device);
                } else {
                    Dlog.i("USB Permission Already Initialization");
                    mUSBListener.onUsbConnected(device);
                }
                break;
            } else {
                Dlog.i("USB Device Check Vendor ID AND Product ID " + (device.getVendorId() == mVendorID) + " / "+ (device.getProductId() == mProductID) );
            }
        }
        if(deviceList.size() == 0) {
            Dlog.i("USB Device not exist!!!");
        }
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    // #3
    public void USBUnloadDeviceList() {
        Dlog.i("USB Device Register BroadCast Receiver Destroy...");

        try{
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            Dlog.e("Register BraodCast Receiver Error : " + e.getMessage());
        }
    }
}
