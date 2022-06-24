package com.jwlryk.natviecppusbconnection.usbDeviceManager;

import com.jwlryk.natviecppusbconnection.util.ConvertData;
import com.jwlryk.natviecppusbconnection.util.Dlog;

public class DeviceDataTransfer {

    private static final int SEQUENCE_SIZE = 4096;
    private static final int SCANLINE_COUNT = 128;
    private static final int READ_SCANLINE_NUMBER = 4;

    private static DeviceDataTransfer mDataTransferInstance = null;
    private static final Object mSyncBlock = new Object();

    private Thread mDataTransferThread;
    private DeviceCommunicator mDeviceCommunicator;
    private final Object mDataTransferBlock;


    private class DeviceDataTransferThread extends Thread {

        public DeviceDataTransferThread() {
            super();
        }

        public void run(){
            final int defaultReadSize = SEQUENCE_SIZE * READ_SCANLINE_NUMBER;
            final byte[] readBuffer = new byte[defaultReadSize];
            int readSize;

            while (!isInterrupted()) {

                try
                {
                    readSize = mDeviceCommunicator.ReadBulkTransfer(readBuffer, 0, defaultReadSize);
                    if(isInterrupted()) {
                        Dlog.i("Thread is interrupted");
                        break;
                    }

                } catch (Exception e) {
                    Dlog.e("Thread Read Exception : " + e);
                    readSize = -1;
                }

                if(readSize <= 0) {
                    continue;
                } else {
                    Dlog.i("DeviceDataTransferThread readSize : "+ readSize);
                }


                for(int i = 0; i < defaultReadSize; i+=4) {
                    byte Data03 = readBuffer[i + 3];
                    byte Data02 = readBuffer[i + 2];
                    byte Data01 = readBuffer[i + 1];
                    byte Data00 = readBuffer[i + 0];
                    byte[] DataArray = {Data03,Data02,Data01,Data00};
                    Dlog.i("RX["+ i +"] - "+ConvertData.byteArrayToHexString(DataArray));
                }


            }

        }

    }

    private DeviceDataTransfer() {
        mDataTransferThread = null;
        mDataTransferBlock = new Object();
        mDeviceCommunicator = null;

    }

    public static DeviceDataTransfer getInstance() {
        if(mDataTransferInstance != null) {
            return mDataTransferInstance;
        }
        synchronized (mSyncBlock) {
            if(mDataTransferInstance == null) {
                mDataTransferInstance = new DeviceDataTransfer();
            }
        }
        return mDataTransferInstance;
    }


    //Communicator Set && Thread Start
    public void registerDeviceCommunicator(DeviceCommunicator communicator) {
        Dlog.i("Device Communicator Setting...");
        synchronized (mDataTransferBlock) {
            _interruptThreadAndReleaseUSB();
            mDeviceCommunicator = communicator;
            mDataTransferThread = new DeviceDataTransferThread();
            mDataTransferThread.start();
        }
        Dlog.i("Device Communicator Setting Complete!");
    }

    private void _interruptThreadAndReleaseUSB() {
        Dlog.i("Interrupt Thread Connection trying...");

        if(mDataTransferThread != null){
            mDataTransferThread.interrupt();
            try {
                mDataTransferThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mDataTransferThread = null;
        }
        if(mDeviceCommunicator != null) {
            mDeviceCommunicator.Clear();
            mDeviceCommunicator = null;
        }
        Dlog.i("Interrupt Thread Connection Complete!");
    }

    public void deregisterDeivceCommunicator() {
        Dlog.i("Device Communicator Resetting`...");
        synchronized (mDataTransferBlock)
        {
            _interruptThreadAndReleaseUSB();
        }
        Dlog.i("Device Communicator Reset Complete!");
    }

}
