package com.marelli.mocklocation;

import android.util.Log;
import android.widget.Button;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.RemoteException;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import android.app.AppOpsManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;


import com.marelli.drlocation.IDrLocationService;

public class MainActivity extends AppCompatActivity implements DrLocationCallback.OnLocationUpdateListener {
    private static final String TAG = "MockLocation";
    Button mBtnMockLocation;
    private TextView txtOutput;
    private LocationManager locationManager;
    private IDrLocationService locationService;
    private DrLocationCallback callback;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Swetabh::onLocationChanged: " + location.toString());
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            txtOutput.setText("latitude = " + latitude + ", longitude = " + longitude);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnMockLocation = findViewById(R.id.btn_mocklocation);
        txtOutput = findViewById(R.id.txtOutput);
        locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000, 0.01f, locationListener);

        setupAppOpsForMockLocation();
        setupMockProvider();
        mBtnMockLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Sending mock location");
                sendLocationUpdate(28.7041, 77.1025); // Example coordinates for Delhi
            }
        });
    }

    private void sendLocationUpdate(double latitude, double longitude) {
        Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
        mockLocation.setLatitude(latitude); // Example latitude
        mockLocation.setLongitude(longitude); // Example longitude
        mockLocation.setAltitude(0);
        mockLocation.setAccuracy(1.0f);
        mockLocation.setElapsedRealtimeNanos(System.nanoTime());
        mockLocation.setTime(System.currentTimeMillis());

        try {
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
            Log.d(TAG, "Mock location set: " + mockLocation.toString());
            //txtOutput.setText("Mock location set: latitude = " + mockLocation.getLatitude() +
            //        ", longitude = " + mockLocation.getLongitude());
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            txtOutput.setText("Failed to set mock location: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException: " + e.getMessage());
            txtOutput.setText("Failed to set mock location: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            txtOutput.setText("Failed to set mock location: " + e.getMessage());
        }
    }

    private void setupMockProvider() {
        Log.i(TAG, "Setting up mock provider");
        try {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "GPS_PROVIDER is not enabled");
            }

            try {
                Log.i(TAG, "Adding GPS_PROVIDER as a test provider");
                locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                        false, false, false, false, true, true, true, android.location.Criteria.POWER_LOW, android.location.Criteria.ACCURACY_FINE);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error adding test provider: " + e.getMessage());
            }
            Log.i(TAG, "Enabling GPS_PROVIDER as a test provider");
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void setupAppOpsForMockLocation() {
        Log.i(TAG, "Setting up AppOps for mock location");
        try {
            String packageName = this.getPackageName();
            PackageManager mPackageManager = this.getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = mPackageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Application not found: " + e.getMessage());
                return;
            }
            AppOpsManager mAppsOpsManager = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
            Log.i(TAG, "Application Uid of this Application: " + appInfo.uid);
            mAppsOpsManager.setMode(AppOpsManager.OPSTR_MOCK_LOCATION, appInfo.uid,
            packageName, AppOpsManager.MODE_ALLOWED);
            Log.d(TAG, "AppOps setup completed ");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up AppOps: " + e.getMessage());
        }
    }

    protected void onStart() {
        super.onStart();
        // Lookup binder directly from ServiceManager
        IBinder binder = ServiceManager.getService("com.marelli.drlocation.IDrLocationService/default");
        if (binder == null) {
            Log.e(TAG, "Failed to get service binder");
            return;
        }

        locationService = IDrLocationService.Stub.asInterface(binder);

        try {
            callback = new DrLocationCallback(this);
            locationService.registerCallback(callback);
            locationService.setUpdateInterval(2000);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register callback", e);
        }
    }

    @Override
    public void onLocationReceived(double lat, double lon) {
        sendLocationUpdate(lat, lon);
        //runOnUiThread(() -> txtOutput.setText("From DR :: Lat: " + lat + ", Lon: " + lon));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationService != null && callback != null) {
            try {
                locationService.unregisterCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "Error unregistering callback", e);
            }
        }
    }
}