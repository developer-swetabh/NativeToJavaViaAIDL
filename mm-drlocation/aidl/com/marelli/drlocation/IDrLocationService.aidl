package com.marelli.drlocation;

import com.marelli.drlocation.IDrLocationCallback;

// AIDL interface exposed by native service
interface IDrLocationService {
    void registerCallback(IDrLocationCallback callback);
    void unregisterCallback(IDrLocationCallback callback);

    void setUpdateInterval(int milliseconds);
    int getUpdateInterval();
}