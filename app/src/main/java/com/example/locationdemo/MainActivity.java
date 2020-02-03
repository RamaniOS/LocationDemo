package com.example.locationdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    LocationManager locationManager;
    LocationListener locationListener;

    Button btn_start, btn_stop;
    TextView txt_location, txt_latitude, txt_longitude, txt_altitude, txt_accuracy, txt_address;

    // location from google play
    FusedLocationProviderClient locationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onStart() {
        super.onStart();
        //check permission
        btn_start.setEnabled(checkPermission());
        if (!checkPermission()) {
            requestPermission();
        } else {
            getLastLocation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLocation();
        initFused();
        initViews();
    }

    private void initFused() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initViews() {
        if (!checkPermission()) {
            requestPermission();
        } else {
            getLastLocation();
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        txt_latitude = findViewById(R.id.txt_lat);
        txt_longitude = findViewById(R.id.txt_lon);
        txt_accuracy = findViewById(R.id.txt_accuracy);
        txt_altitude = findViewById(R.id.txt_altitude);
        txt_address = findViewById(R.id.txt_address);
        btn_start = findViewById(R.id.update);
        btn_start.setEnabled(checkPermission());
        btn_stop = findViewById(R.id.stop);
        txt_location = findViewById(R.id.location);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    getLastLocation();
                }
                locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                btn_start.setEnabled(!btn_start.isEnabled());
                btn_stop.setEnabled(!btn_stop.isEnabled());
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationClient.removeLocationUpdates(locationCallback);
                btn_start.setEnabled(!btn_start.isEnabled());
                btn_stop.setEnabled(!btn_stop.isEnabled());
            }
        });
    }

    private void initLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location>>>", "onLocationChanged" + location);
                //String loc = location.getLatitude() + " / " + location.getLongitude();
                //txt_location.setText(loc);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        // CHECK PERMISSION & REQUEST IF NEEDED
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE);
        } else {
            locationUpdate();
            locationCallback();
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void locationUpdate() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void locationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location: locationResult.getLocations()) {
                    txt_latitude.setText(String.valueOf(location.getLatitude()));
                    txt_longitude.setText(String.valueOf(location.getLongitude()));
                    txt_accuracy.setText(String.valueOf(location.getAccuracy()));
                    txt_altitude.setText(String.valueOf(location.getAltitude()));
                    // get address
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses =  geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {
                            Address address = addresses.get(0);
                            String country = address.getCountryName();
                            txt_address.setText(address.getAddressLine(0) + "," + address.getPostalCode() + "," + country);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("location updates>>>", location.getLatitude() + String.valueOf(location.getLongitude()));
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       if (grantResults.length <=0) {

       } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           //permission granted
           checkPermission();
           requestPermission();
       } else {
           //permission denied
           showSnackBar(R.string.warning, R.string.setting, new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent();
                   intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                   Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                   intent.setData(uri);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
               }
           });
       }

        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private boolean checkPermission() {
        int isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return isGranted == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        boolean isFlag = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (!isFlag) {
            startLocationPermissionRequest();
        } else {
            showSnackBar(R.string.warning, R.string.setting, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLocationPermissionRequest();
                }
            });
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE);
    }

    private void getLastLocation() {
        locationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    setLocation(task.getResult());
                }
            }
        });
    }

    private void setLocation(Location location) {
        String loc = location.getLatitude() + " / " + location.getLongitude();
        txt_location.setText(loc);
    }

    private void showSnackBar(final int mainStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), getString(mainStringId), Snackbar.LENGTH_INDEFINITE).setAction(actionStringId, listener).show();
    }

}
