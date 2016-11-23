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
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;


public class SignalRService extends Service
{

    public static final String serverUrl = "http://192.168.0.125:38860/";

    public static final String SERVER_HUB_CHAT = "moveShapeHub";
    public static final String SERVER_METHOD_SEND = "updateModel";
    public static final String CLIENT_METHOD_RECEIVE = "showDate";

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


        mHubProxy.invoke(String.class, SERVER_METHOD_SEND, data)
                .done(new Action()
                {
                    @Override
                    public void run(Object o) throws Exception
                    {
                        Log.e(TAG, "Success");
                        Log.e(TAG, o.toString());

                    }
                }).onError(new ErrorCallback()
        {
            @Override
            public void onError(Throwable throwable)
            {
                Log.e(TAG, "Error: " + throwable.getMessage());
            }
        });
    }

    private void startSignalR()
    {

        Platform.loadPlatformComponent(new AndroidPlatformComponent());

//        String serverUrl = "http://transit.alwaysaware.org/signalr";


//        mHubConnection = new HubConnection(serverUrl);
//
//        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);
//
//
//        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
//
//        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);
//
//        try
//        {
//            signalRFuture.get();
//        }
//        catch (InterruptedException | ExecutionException e)
//        {
//            e.printStackTrace();
//            return;
//        }

        HubConnection connection = new HubConnection(serverUrl);
        HubProxy proxy = connection.createHubProxy(SERVER_HUB_CHAT);

        SignalRFuture<Void> awaitConnection = connection.start();
        try
        {
            awaitConnection.get();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Invoke JoinGroup to start receiving broadcast messages
        proxy.invoke(CLIENT_METHOD_RECEIVE, "Group1");

        //Then call on() to handle the messages when they are received.
        proxy.on(CLIENT_METHOD_RECEIVE, new SubscriptionHandler1<String>()
        {
            @Override
            public void run(String msg)
            {
                Log.d("result := ", msg);
            }
        }, String.class);

    }

    @Override
    public void onDestroy()
    {

        mHubConnection.stop();
        super.onDestroy();

    }
}