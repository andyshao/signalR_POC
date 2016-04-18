package com.example.oashraf.signalr_poc;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ILocationCallBack
{
    private final Context mContext = this;
    private SignalRService mService;
    private boolean mBound = false;

    public static int PERMISSIONS_REQUEST_ACCESS_INTERNET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent();
        intent.setClass(mContext, SignalRService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        askLocationPermission(this);
    }


    public void askLocationPermission(ILocationCallBack locationCallBack)
    {

        if (Build.VERSION.SDK_INT < 23) // Android M support, Run time permissions Request
        {
            locationCallBack.permissionsGranted();
        }
        else // Below Android M Implementation
        {
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, PERMISSIONS_REQUEST_ACCESS_INTERNET);
            }
            else
            {
                locationCallBack.permissionsGranted();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_INTERNET)
        {
            if (grantResults != null && grantResults.length > 0)
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    permissionsGranted();
                }
                else
                {
                    askLocationPermission(this);
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
    }

    @Override
    protected void onStop()
    {
        // Unbind from the service
        if (mBound)
        {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    public void sendMessage()
    {
        if (mBound)
        {
            // Call a method from the SignalRService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
//            EditText editText = (EditText) findViewById(R.id.edit_message);
//            if (editText != null && editText.getText().length() > 0)
//            {
            String message = "Some Message from Karwa";
            mService.sendData(message);
//            }
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
//            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            mBound = false;
        }
    };

    @Override
    public void permissionsGranted()
    {
        sendMessage();
    }
}