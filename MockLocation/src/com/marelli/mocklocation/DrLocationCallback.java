package com.marelli.mocklocation;

import android.os.RemoteException;
import android.util.Log;
import com.marelli.drlocation.IDrLocationCallback;

public class DrLocationCallback extends IDrLocationCallback.Stub {
    private final OnLocationUpdateListener listener;

    public interface OnLocationUpdateListener {
        void onLocationReceived(double lat, double lon);
    }

    public DrLocationCallback(OnLocationUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLocationUpdate(double lat, double lon) throws RemoteException {
        Log.d("DrLocationCallback", "Received location: " + lat + ", " + lon);
        listener.onLocationReceived(lat, lon);
    }
}