package com.jwlryk.natviecppusbconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jwlryk.natviecppusbconnection.databinding.ActivityMainBinding;
import com.jwlryk.natviecppusbconnection.usbDeviceManager.DeviceHandler;
import com.jwlryk.natviecppusbconnection.util.Dlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    /**
     *
     * Used to load the 'natviecppusbconnection' library on application startup.
     *
     */
    static {
        System.loadLibrary("natviecppusbconnection");
    }

    private ActivityMainBinding binding;


    /**
     * USB SETTING VALUE
     */
    public static boolean DEBUG = false;

    private DeviceHandler mDeviceHandler = new DeviceHandler(this);

    /*System Element*/
    Process logcat;
    StringBuilder log = null;

    /*XML Element*/
    public TextView logBoxText;

    Button menuBoxClearButton;
    Button menuBoxLoadButton;
    Button menuBoxSetButton;
    Button menuBoxStartButton;

    EditText sendBoxEdit;
    Button sendBoxButton;


    Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Native C++ Connection Test TextView
        TextView tv = findViewById(R.id.top_text_01);
        tv.setText(stringFromJNI());

        //Connect USB Connection Test
        USBInitial();

        ImageView imageView = findViewById(R.id.sample_image_view);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                BitmapGradation(bmp);
                imageView.setImageBitmap(bmp);
                imageView.post(this);
            }
        });
    }

    public void USBInitial () {
        this.DEBUG = isDebuggable(this);
        Dlog.i("Start");

        /**
         * Handler Start
         */

        mDeviceHandler.initialize();

        /**
         * Log Box Setting
         */
        logBoxText = (TextView)findViewById(R.id.log_box_text);
        logBoxText.setMovementMethod(new ScrollingMovementMethod());

        /**
         * Menu Box Setting
         */
        //Clear
        menuBoxClearButton = (Button)findViewById(R.id.menu_box_clear_button);
        menuBoxClearButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dlog.i("menuBoxClearButton");
                logBoxText.setText(null);
                clearLogcatLog();
                mDeviceHandler.lengthSaver = 0;
            }
        });

        /**
         * Load Button
         */
        menuBoxLoadButton = (Button)findViewById(R.id.menu_box_load_button);
        menuBoxLoadButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dlog.i("menuBoxLoadButton");
                setLogcat();
            }
        });

        /**
         * Reset Button
         */
        menuBoxSetButton =(Button)findViewById(R.id.menu_box_reset);
        menuBoxSetButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeviceHandler.resetData();
            }
        });

        /**
         * Start Button
         */
        menuBoxStartButton =(Button)findViewById(R.id.menu_box_counterStart);
        menuBoxStartButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeviceHandler.startCounter();
            }
        });
        /**
         * Send Box Setting
         */
        sendBoxEdit = (EditText)findViewById(R.id.send_box_edit);
        sendBoxEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendEvent();
                    return true;
                }
                return false;
            }
        });
        sendBoxButton = (Button)findViewById(R.id.send_box_button);

        sendBoxButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEvent();
            }
        });
    }
    public void sendEvent(){
        //Dlog.i("sendBoxButton");
        String sendBoxData = sendBoxEdit.getText().toString();
        String[] hexStringArray = { sendBoxData };
        int sendBoxDataLength = sendBoxData.length();
        Dlog.i("Data Length : " + sendBoxDataLength + System.lineSeparator() );

        if (sendBoxDataLength == 0) {
            return;
        } else if(sendBoxDataLength == 8){
            //logBoxText.append(sendBoxData + System.lineSeparator());
            Dlog.i("Hex Number : " + sendBoxData + System.lineSeparator() );
            mDeviceHandler.sendData(hexStringArray);
            sendBoxEdit.getText().clear();
        } else {
            Toast warningMessage = Toast.makeText(getApplicationContext(),"Please Enter 8 Hexadecimal numbers ", Toast.LENGTH_SHORT);
            warningMessage.show();
        }
        //logBoxText.setText(getLogcat());
    }

    public void setLogcat(){
        logBoxText.setText(getLogcat());
    }

    public String getLogcat() {
        try {
            log = new StringBuilder();
            logcat = Runtime.getRuntime().exec("logcat -d -v time");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logcat.getInputStream()),4*1024);
            String lineString;
            String separator = System.lineSeparator();
            while ((lineString = bufferedReader.readLine()) != null) {
                if(lineString.contains("JWLK")){
                    log.append(lineString);
                    log.append(separator);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return log.toString();
    }

    private void clearLogcatLog() {
        try {
            @SuppressWarnings("unused")
            Process process = Runtime.getRuntime().exec("logcat -b all -c");
            logBoxText.append("Please Click Load");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onStart() {
        super.onStart();
        Dlog.d("onStart");
        mDeviceHandler.handlingStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Dlog.d("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Dlog.d("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Dlog.d("onStop");
        mDeviceHandler.handlingStop();
        Dlog.i("Device Handler Stop And Reset Complete");
        mDeviceHandler.handlingClear();
        Dlog.i("Device Handler Clear Complete");

        Dlog.i("onStop Completed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dlog.d("onDestroy");
    }

    @Override
    public void onBackPressed() {
        Dlog.d("Application Finish");
        finish();
    }

    /**
     * get Debug Mode
     *
     * @param context
     * @return
     */
    private boolean isDebuggable(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
        }

        return debuggable;
    }

    /**
     * Cmake Project List
     * A native method that is implemented by the 'natviecppusbconnection' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native boolean BitmapGradation(Bitmap bmp);
}