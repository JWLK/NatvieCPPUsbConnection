package com.jwlryk.natviecppusbconnection.usbDeviceManager;

import static com.jwlryk.natviecppusbconnection.util.ConvertData.hexStringArrayToInt32bit8HexArray;
import static com.jwlryk.natviecppusbconnection.util.ConvertData.hexStringToShort16bit4HexArray;

import com.jwlryk.natviecppusbconnection.util.Dlog;

import java.util.ArrayList;

public class DeviceRegisterSetting {

    public static void writeCommandHexData(DeviceCommunicator device, String hexString)
    {
        short[] dataShort = hexStringToShort16bit4HexArray(hexString);
        for(short shorts : dataShort){
            Dlog.d(String.format("0x%04X", shorts));
        }

        try {
            device.DataTransferSingleWrite(dataShort[0], dataShort[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Dlog.e(e.toString());
        }

    }

    public static void writeBulkHexData(DeviceCommunicator device, String[] mHexStringArray)
    {
        int[] sendIntArray = hexStringArrayToInt32bit8HexArray(mHexStringArray);
        for(int ints : sendIntArray){
            Dlog.d(String.format("0x%04X", ints));
        }
        try {
            device.DataTransferBulkWrite(mHexStringArray);
        } catch (Exception e) {
            e.printStackTrace();
            Dlog.e(e.toString());
        }

    }

    public static void counterTest(DeviceCommunicator device)
    {
        String[] sendStringArray =  {"980100FF", "98000003"};
        int[] sendIntArray = hexStringArrayToInt32bit8HexArray(sendStringArray);
        for(int ints : sendIntArray){
            Dlog.d(String.format("0x%04X", ints));
        }
        try {
            device.DataTransferBulkWrite(sendStringArray);
        } catch (Exception e) {
            e.printStackTrace();
            Dlog.e(e.toString());
        }

    }

    static ArrayList<String> dataSaveArrayList = new ArrayList<>();

    /**
     * Send Register Array
     */
    public static void sendRegisterButton(DeviceCommunicator device, ArrayList<String> arrayList) {
        String[] sendStringArray =  arrayList.toArray(new String[arrayList.size()]);
        /*
        int[] sendIntArray = hexStringArrayToInt32bit8HexArray(sendStringArray);
        for(int ints : sendIntArray){
            Dlog.d(String.format("0x%04X", ints));
        }
         */

        try {
            device.DataTransferBulkWrite(sendStringArray);
            Dlog.d("Send Register Data ");
            dataSaveArrayList = new ArrayList<>();
            Dlog.d("dataSaveArrayList Clear ");
        } catch (Exception e) {
            e.printStackTrace();
            Dlog.e(e.toString());
        }

    }

}
