package edu.stevens.mattmccreesh.heatrescue;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, android.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = MainActivity.class.getCanonicalName();
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("tag", "inonCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = (Button) findViewById(R.id.send_button);
        b.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    public void onStart(){
        super.onStart();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.v("tag", "gps provider enabled");
            mGoogleApiClient.connect();
        }
        else
            Log.v("tag", "gps provider not enabled");
    }

    public void onStop()
    {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void onConnected(Bundle bundle)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v("CONNECTED: ", "connected");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 10, (float) 5, (android.location.LocationListener) this);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS permission not granted").setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    public void onConnectionSuspended(int i)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.removeUpdates((android.location.LocationListener)this);
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS permission not granted").setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, ConnectionResult.CANCELED);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    public void onLocationChanged(Location location)
    {
        Toast.makeText(this, "Location Changed: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_LONG).show();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        String str = "";
        switch (status) {
            case 0:
                str = "OUT_OF_SERVICE";
                break;
            case 1:
                str = "TEMPORARILY UNAVAILABLE";
                break;
            case 2:
                str = "AVAILABLE";
                break;
            default:
                str = "";
        }
    }

    public void onProviderEnabled(String provider) {
        Log.i(TAG, "Provider: " + provider + " enabled");
    }

    public void onProviderDisabled(String provider) {
        Log.i(TAG, "Provider: " + provider + " disabled");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        //Toast.makeText(this, "Test",Toast.LENGTH_LONG).show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
        }

//        Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
       /* mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            public void onSuccess(Location location) {
                System.out.println("success");
                if (location != null) {
                    System.out.println(location.toString());
                }
            }
        });*/
/*
        Location myloc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(myloc != null)
            Toast.makeText(this, myloc.toString(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "null location", Toast.LENGTH_LONG).show();*/


        //trying this
        Location location = null;
        boolean isGPSEnabled;
        boolean isNetworkEnabled;
        boolean canGetLocation;
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            10,
                            3, (android.location.LocationListener)this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                10,
                                3, (android.location.LocationListener)this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                Toast.makeText(this, "Longitude: " + longitude + " Lattitude: " + latitude, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
