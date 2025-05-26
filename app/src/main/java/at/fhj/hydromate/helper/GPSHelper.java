package at.fhj.hydromate.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationRequest;

public class GPSHelper {

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;

    public GPSHelper(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void requestLocation(LocationCallback callback) {
        boolean hasFine = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasFine && !hasCoarse) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001);
            return;
        }

        int priority = hasFine ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        LocationRequest locationRequest = new LocationRequest.Builder(priority, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(1000)
                .setMaxUpdates(1)
                .build();

        com.google.android.gms.location.LocationCallback locationCallback =
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            Location location = locationResult.getLastLocation();
                            String coordinates = location.getLatitude() + "," + location.getLongitude();
                            Log.d("GPSHelper", coordinates);
                            callback.onLocationReceived(coordinates);
                        } else {
                            Log.d("GPSHelper", "Location not available");
                            callback.onLocationReceived("Location not available");
                        }
                    }
                };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}
