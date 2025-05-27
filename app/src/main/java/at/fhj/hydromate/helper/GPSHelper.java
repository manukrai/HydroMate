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

/**
 * Die Klasse {@code GPSHelper} vereinfacht den Zugriff auf den aktuellen Standort eines Geräts.
 * <p>
 * Sie nutzt den {@link FusedLocationProviderClient} von Google Play Services, um eine genaue
 * oder energieeffiziente Standortbestimmung basierend auf vorhandenen Berechtigungen zu ermöglichen.
 * </p>
 * <p>
 * Die Standortdaten werden asynchron angefordert und über einen benutzerdefinierten
 * {@link LocationCallback} zurückgegeben.
 * </p>
 *
 * <h2>Voraussetzungen</h2>
 * <ul>
 *     <li>Berechtigungen: {@link Manifest.permission#ACCESS_FINE_LOCATION} oder {@link Manifest.permission#ACCESS_COARSE_LOCATION}</li>
 *     <li>Google Play Services müssen verfügbar sein</li>
 * </ul>
 *
 * Beispielverwendung:
 * <pre>{@code
 * new GPSHelper(this).requestLocation(location -> {
 *     Log.d("Standort", location);
 * });
 * }</pre>
 */
public class GPSHelper {

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;

    /**
     * Konstruktor für die Klasse {@code GPSHelper}.
     *
     * @param activity Die Aktivität, aus der heraus der Standort angefordert wird.
     */
    public GPSHelper(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    /**
     * Fordert den aktuellen Standort an. Falls keine Standortberechtigungen vorhanden sind,
     * werden diese beim Nutzer angefragt.
     *
     * @param callback Callback-Schnittstelle, die beim Eintreffen des Standorts aufgerufen wird.
     */
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
