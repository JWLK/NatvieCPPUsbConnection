package com.jwlryk.natviecppusbconnection.usbDeviceManager;

import android.hardware.usb.UsbDevice;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import com.jwlryk.natviecppusbconnection.MainActivity;
import com.jwlryk.natviecppusbconnection.util.Dlog;

public class DeviceHandler extends Handler {

    public int lengthSaver = 0;

    private MainActivity mainActivity;
    private DeviceManager mDeviceManager;
    private DeviceCommunicator mDeviceCommunicator;
    private DeviceDataTransfer mDeviceDataTransfer;
    //private PropertyManager mPropertyManager;

    private SimpleDateFormat formatPrint = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss");

    private Thread mUSBRealTimeController = new Thread(){
        @Override
        public void run() {
            while (!isInterrupted()) {

                Date currentTime = new Date();
                String printString = formatPrint.format(currentTime);
                //Dlog.i(printString); // Timer

                try
                {
                    Thread.sleep(1000);
//                    if(mainActivity.getLogcat().length() > lengthSaver){
//                        lengthSaver = mainActivity.getLogcat().length();
//                        mainActivity.setLogcat();
//                        InterfaceUtil.scrollBottom(mainActivity.logBoxText);
//                    }
                }
                catch (InterruptedException e) {
                    Dlog.e("mUSBRealTimeController Thread Error : " + e );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    public DeviceHandler(MainActivity activity) {
        mainActivity = activity;
    }

    public void initialize() {
        mDeviceDataTransfer = DeviceDataTransfer.getInstance();
        //mUSBRealTimeController.start();
    }

    public void handleMessage(Message msg) {
        UsbDevice device = (UsbDevice) msg.obj;

        if(msg.what == DeviceManager.MSG_USB_CONNECTION) {
            Dlog.i("Device Manager Monitoring Service Send Message : On USB Connected");
            try {
                mDeviceCommunicator = mDeviceManager.CreateDeviceCommunicator(mainActivity.getApplicationContext(), device);
            } catch (Exception e) {
                Dlog.e("handleMessage Error "+ e );
            }

            if(mDeviceCommunicator != null) {
                mDeviceDataTransfer.registerDeviceCommunicator(mDeviceCommunicator);
            }
        } else {
            Dlog.i("Device Manager Monitoring Service Send Message : On USB DisConnected");
            handlingClear();
        }
    }

    public void handlingStart() {
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.DeviceManagerMonitoringStart(mainActivity.getApplicationContext(), this);
    }

    public void handlingStop() {
        if(mDeviceCommunicator != null)
        {
            try {
                Dlog.i("Set freeze");
                //DeviceSetting.sendFreeze(mDeviceCommunicator);
                Dlog.i("Complete freeze");
            } catch(Exception e) {
                Dlog.e("Send device freeze error : " + e.getMessage());
            }
        }
        mDeviceManager.DeviceManagerMonitoringClear();
    }

    public void handlingClear() {
        mDeviceDataTransfer.deregisterDeivceCommunicator();
        mDeviceCommunicator = null;

    }

    public void sendData(String[] hexStringArray) {
        if(mDeviceCommunicator != null) {
            DeviceRegisterSetting.writeBulkHexData(mDeviceCommunicator, hexStringArray);
        } else {
            Toast warningMessage = Toast.makeText(mainActivity.getApplicationContext(),"Please Connect USB Device: "+mDeviceCommunicator, Toast.LENGTH_SHORT);
            warningMessage.show();
        }

    }

    public void debug_registerSetting() {
        ArrayList<String> excel_1 = registerLoadSelect("debug.xls",0, 1, 1, 1, 6);
        DeviceRegisterSetting.sendRegisterButton(mDeviceCommunicator,excel_1);
    }


    public ArrayList<String> registerLoadSelect(String SheetName,int selectNumber, int colStartNumber ,int colCount, int rowStartNumber ,int rowCount) {
        ArrayList<String> dataSaveArrayList = new ArrayList<>();

        try {
            InputStream is = mainActivity.getApplicationContext().getAssets().open(SheetName);
            Workbook wb = Workbook.getWorkbook(is);

            if(wb != null) {
                Dlog.d("RegisterMap Init : " + SheetName);
                Sheet sheet = wb.getSheet(selectNumber);   // 시트 불러오기
                if(sheet != null) {
                    int colIndexStart = colStartNumber;
                    int colTotal = colCount;
                    int rowIndexStart = rowStartNumber;                  // row 인덱스 시작
                    int rowTotal = rowCount;

                    ArrayList<String> arrayList = new ArrayList<>();
                    StringBuilder sb;

                    for(int col = colIndexStart; col <= colTotal; col++) {

                        sb = new StringBuilder();

                        for(int row = rowIndexStart; row <= rowTotal; row++) {

                            String contents = sheet.getCell(col, row).getContents();
                            arrayList.add(contents);
                            if(row == rowTotal-1) {
                                sb.append("row"+row+" : "+contents);
                            } else {
                                sb.append("row"+row+" : "+contents+" , ");
                            }
                            Dlog.i("RegisterContents : " + col+","+row+" = "+contents);

                            //ADD Reigster
                            dataSaveArrayList.add(contents);
                        }

                    }
                    Dlog.i("Total = "+ arrayList.size());
                }
            }
        } catch (IOException | BiffException e) {
            e.printStackTrace();
            Log.e("error", e.toString());
        }

        return dataSaveArrayList;

    }

    public void resetData() {
        Dlog.i("Reset Data");
        if(mDeviceCommunicator != null) {
            mDeviceCommunicator.DataTransferReset();
        } else {
            Toast warningMessage = Toast.makeText(mainActivity.getApplicationContext(),"Please Connect USB Device: "+mDeviceCommunicator, Toast.LENGTH_SHORT);
            warningMessage.show();
        }
    }

    public void startCounter() {
        Dlog.i("Start Counter");
        if(mDeviceCommunicator != null) {
            DeviceRegisterSetting.counterTest(mDeviceCommunicator);
        } else {
            Toast warningMessage = Toast.makeText(mainActivity.getApplicationContext(),"Please Connect USB Device: "+mDeviceCommunicator, Toast.LENGTH_SHORT);
            warningMessage.show();
        }
    }




}
