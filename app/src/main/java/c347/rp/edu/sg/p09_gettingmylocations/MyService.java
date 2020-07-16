package c347.rp.edu.sg.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class MyService extends Service {

    boolean started;
    String folderLocation;
    FusedLocationProviderClient client;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallBack;

    public MyService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "Service created");

        if (checkLocationPermission() == true && checkRWPermission() == true){
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
        }

        client = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null){
                    Location location = locationResult.getLastLocation();
                    String data = location.getLatitude() + ", " + location.getLongitude();
                    Log.d("Location change: ", data);

                    String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";
                    File targetFile = new File(folderLocation, "data.txt");

                    try{
                        FileWriter writer = new FileWriter(targetFile, true);
                        writer.write(data + "\n");
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (started == false){
            started = true;
            Log.d("Service: ", "Service started");
            if(checkLocationPermission() == true && checkRWPermission() == true){
                client.requestLocationUpdates(mLocationRequest, mLocationCallBack, null);
            }
        }else{
            Log.d("Service", "Service is still running");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service: ", "Service exited");
        client.removeLocationUpdates(mLocationCallBack);
    }

    private boolean checkLocationPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkRWPermission(){
        int permissionCheck_write = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_read = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck_write == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_read == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
