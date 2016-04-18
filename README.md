# signalR_POC
Proof of concept application for implementing the Signal R Framework for Android, Dated: 18th of April' 2016


In the code, before using the library, initialize the library with Platform.loadPlatformComponent();
The Service method sendData(String data) will send the Data to the Server and the following methods will be called accordingly:

done(Action action)

onError(ErrorCallBack errorCallBack)
