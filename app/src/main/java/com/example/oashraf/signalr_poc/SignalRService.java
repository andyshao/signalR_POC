package com.example.oashraf.signalr_poc;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;


public class SignalRService extends Service
{
    private static final String TAG = "SignalRService";
    private static final String SHARED_PREFS = "shared_pref";
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = (IBinder) new LocalBinder();

    private SharedPreferences sp;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d(TAG, "Service Created");

        sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mHandler = new Handler(Looper.myLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        int result = super.onStartCommand(intent, flags, startId);
        startSignalR();
        return result;
    }

    @Override
    public IBinder onBind(Intent intent)
    {

        startSignalR();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder
    {
        public SignalRService getService()
        {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

    public void sendData(String data)
    {

        String SERVER_METHOD_SEND = "iAmAvailable";
        final String string = new String();

        mHubProxy.invoke(new String(), SERVER_METHOD_SEND, sp.getString("user_id", null), sp.getString("pass", null), "TransMedic")
                .done(new Action()
                {
                    @Override
                    public void run(Object o) throws Exception
                    {

                        Log.e(TAG, o.toString());

                    }
                }).onError(new ErrorCallback()
                {
                    @Override
                    public void onError(Throwable throwable)
                    {

                    }
                });
    }

    private void startSignalR()
    {

        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        String serverUrl = "http://transit.alwaysaware.org/signalr";

        mHubConnection = new HubConnection(serverUrl);

        String SERVER_HUB_CHAT = "ChatHub";

        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);

        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());

        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);


        try
        {

            signalRFuture.get();

        }
        catch (InterruptedException | ExecutionException e)
        {

            e.printStackTrace();
            return;

        }

        sendData("");
    }

    @Override
    public void onDestroy()
    {

        mHubConnection.stop();
        super.onDestroy();

    }
}