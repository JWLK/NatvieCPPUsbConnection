package com.jwlryk.natviecppusbconnection.usbDeviceElement;

import android.content.Context;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class USBMonitoringService {

    private static USBMonitoringService mUSBMonitorServiceInstance = null;
    private static final Object mSyncBlock = new Object();
    private List<USBConnector> mConnectorList;

    private USBMonitoringService() {
        mConnectorList = new LinkedList<USBConnector>();
    }

    public static USBMonitoringService getInstance() {
        if(mUSBMonitorServiceInstance != null) {
            return mUSBMonitorServiceInstance;
        }

        synchronized (mSyncBlock) {
            if(mUSBMonitorServiceInstance == null) {
                mUSBMonitorServiceInstance = new USBMonitoringService();
            }
        }

        return mUSBMonitorServiceInstance;
    }

    public void USBMonitorClear() {
        Iterator<USBConnector> usbListInterator = mConnectorList.iterator();
        USBConnector usbConnector;
        while (usbListInterator.hasNext()){
            usbConnector = usbListInterator.next();
            usbConnector.USBUnloadDeviceList();
        }
    }

    public void USBMonitorStart(Context context, int vendorID, int productID, String className, USBConnectionListener usbListener) {
        USBConnector connector = new USBConnector(context, vendorID, productID, className, usbListener);
        connector.USBLoadDeviceList();
        mConnectorList.add(connector);
    }


}
