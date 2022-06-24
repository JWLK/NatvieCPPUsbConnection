package com.jwlryk.natviecppusbconnection.usbDeviceElement;

import android.hardware.usb.UsbDevice;

/**
 * Android USB Device Connection Check Interface
 */

public interface USBConnectionListener {

    /**
     * DEVICE Connected Event Listener
     */
    void onUsbConnected(UsbDevice device);

    /**
     * DEVICE DisConnected Event Listener
     */
    void onUsbDetached(UsbDevice device);
}
