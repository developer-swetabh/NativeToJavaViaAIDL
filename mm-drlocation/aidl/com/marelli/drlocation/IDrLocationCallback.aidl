package com.marelli.drlocation;

// Called by native to push new coordinates to Android service
interface IDrLocationCallback {
    void onLocationUpdate(double latitude, double longitude);
}