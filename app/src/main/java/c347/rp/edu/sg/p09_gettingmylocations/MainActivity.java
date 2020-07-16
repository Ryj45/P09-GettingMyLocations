package c347.rp.edu.sg.p09_gettingmylocations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity {

    Button btnStart, btnStop, btnCheck;
    TextView tvLat, tvLng;
    String folderLocation;
    FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnCheck = findViewById(R.id.btnCheck);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);

        if (checkRWPermission() == true){
            folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09-GettingMyLocations";

            File folder = new File(folderLocation);
            if(folder.exists() == false){
                boolean result = folder.mkdir();
                if (result == true) {
                    Log.d("File Read/Write", "Folder created");
                }
            }
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission() == true && checkRWPermission() == true) {
                    Intent i = new Intent(MainActivity.this, MyService.class);
                    startService(i);
                }

                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setSmallestDisplacement(100);

                final LocationCallback mLocationCallBack = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if(locationResult != null){
                            Location data = locationResult.getLastLocation();
                            double lat = data.getLatitude();
                            double lng = data.getLongitude();
                            String record = lat + ", " + lng;

                            try{
                                File targetFile = new File(folderLocation, "data.txt");
                                FileWriter fileWriter= new FileWriter(targetFile, true);
                                fileWriter.write(record + "\n");
                                fileWriter.flush();
                                fileWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                client.requestLocationUpdates(mLocationRequest, mLocationCallBack, null);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderLocation = getExternalFilesDir(null) + "/P09";
                File targetFile = new File(folderLocation, "data.txt");

                if (targetFile.exists() == true){
                    String data = "";
                    try {

                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);

                        String line = br.readLine();
                        while (line != null) {
                            data += line + "\n";
                            line = br.readLine();
                        }

                        br.close();
                        reader.close();

                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    Log.d("Content", data);
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                }
            }
        });

        client = LocationServices.getFusedLocationProviderClient(this);

        if (checkLocationPermission() == true){
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        tvLat.setText("Latitude: " + location.getLatitude());
                        tvLng.setText("Longitude: " + location.getLongitude());
                    }else{
                        String msg = "No Last Known Location found";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private boolean checkLocationPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkRWPermission(){
        int permissionCheck_write = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_read = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck_write == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_read == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
