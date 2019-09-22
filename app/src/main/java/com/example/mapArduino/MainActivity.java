package com.example.mapArduino;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback { //Main activity. is the whole app in this case.
    //Constants for convenience.
    public static final String DARK = "dark";
    public static final String LIGHT = "light";
    GoogleMap map;

    boolean AUTO_STATE = false;
    final String AUTO_ON = "a";
    final String AUTO_OFF = "0";
    final String UP = "f";
    final String DOWN = "b";
    final String RIGHT = "r";
    final String LEFT = "l";
    final String LOC = "p";


    BluetoothSPP bluetooth;

    File logFile;

    Button spooky;
    Button connect;
    Button auto_mode;
    Button up, down, right, left;
    Button mine, log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //https://github.com/akexorcist/Android-BluetoothSPPLibrary
        //Library used.
        //Buttons: Auto - sends "a"
        //up arrow - 'f'
        //down arrow - 'b'
        //right arrow - 'r'
        //left arrow - 'l'
        //mine - 'p' - to get coords.
        super.onCreate(savedInstanceState);
        setTheme(themer.getTheme()); //theme as defined in themer.java
        setContentView(R.layout.activity_main); //set the layout according to the XML

        //Define the file where the log will be stored.
        logFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/logger.txt");

        setupMap();
        setupButtons();

        bluetooth = new BluetoothSPP(this);
        //alert if bluetooth isn't available.
        if (bluetooth.isBluetoothAvailable()) setupBluetooth();
        else Toast.makeText(getApplicationContext(), R.string.bt_not_available, Toast.LENGTH_LONG).show();


    }

    public void onStart() { //called at activity start.
        super.onStart();
        startBluetooth();
    }

    public void onDestroy() { //called at activity end.
        super.onDestroy();
        bluetooth.stopService();
    }

    private void setupBluetooth() {
        bluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                connect.setText(getString(R.string.connect_success, name));
                buttonVis(View.VISIBLE);
            }

            public void onDeviceDisconnected() {
                connect.setText(R.string.connect_lost);
                buttonVis(View.INVISIBLE);
            }

            public void onDeviceConnectionFailed() {
                connect.setText(R.string.connect_fail);
            }
        });
        //whenever a valid marker is received add it to the map and log it.
        bluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) throws NumberFormatException {
                addNewMarker("marker", Float.parseFloat(message.split(" ")[0]), Float.parseFloat(message.split(" ")[1]));
                appendLog("marker@(" + message + ") added");
            }
        });
    }

    private void setupButtons() {
        initButtons();
        connect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType") // IDE being stupid
            @Override
            public void onClick(View v) {
                if(!bluetooth.isBluetoothAvailable()) Toast.makeText(getApplicationContext(), R.string.bt_not_available, Toast.LENGTH_LONG).show();
                else if (bluetooth.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bluetooth.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    connect.setText(R.id.connect);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareLogFile();
            }
        });
        //reset log on long press.
        log.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (logFile.exists()) {
                    logFile.delete();
                }
                Toast.makeText(MainActivity.this, "Reset log", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        setupArrowKeys();
        setupSpooky();
        buttonVis(View.INVISIBLE); // turns buttons invisible until BT connects
    }

    private void initButtons() {
        spooky = findViewById(R.id.darkModeToggle);
        connect = findViewById(R.id.connect);
        auto_mode = findViewById(R.id.auto);
        up = findViewById(R.id.uparrow);
        down = findViewById(R.id.downarrow);
        right = findViewById(R.id.rightarrow);
        left = findViewById(R.id.leftarrow);
        mine = findViewById(R.id.locMarker);
        log = findViewById(R.id.logger);
    }

    private void shareLogFile() {
        //creating an android sharing intent and passing it the log file through a fileProvider, which is now required since APIv24. android deals with the rest of the file sharing
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", logFile));
        startActivity(Intent.createChooser(sharingIntent, "share file with"));

    }

    private void shareLogText() { //same as shareLogFile but it shares the log but as a text message instead of as a file.
        StringBuilder banana = new StringBuilder("Log: \n");
        try {
            BufferedReader buf = new BufferedReader(new FileReader(logFile));
            String a = buf.readLine();
            while(a != null){
                banana.append(a).append("\n");
                a = buf.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, banana.toString());
        sharingIntent.setType("text/plain");

        Intent Schnitzel = Intent.createChooser(sharingIntent, null);
        startActivity(Schnitzel);

    }

    private void setupSpooky() {
        //setup the theme change button.
        spooky.setVisibility(View.VISIBLE);
        spooky.setBackgroundColor(Color.TRANSPARENT);
        spooky.setOnClickListener(new View.OnClickListener(){
            int a = themer.spooky;
            @Override
            public void onClick(View v){
                themer.spooky = a + 1;
                nextTheme();
                a++;
            }
        });
    }

    private void setupArrowKeys() {
        //creating a single listener for all buttons that do pretty much the same thing is convenient.
        View.OnClickListener arrowListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSender(v);
            }
        };
        up.setOnClickListener(arrowListener);
        down.setOnClickListener(arrowListener);
        right.setOnClickListener(arrowListener);
        left.setOnClickListener(arrowListener);
        mine.setOnClickListener(arrowListener);
        auto_mode.setOnClickListener(arrowListener);
    }

    private void buttonVis(int visibility) {
        //to change the visibility of all the buttons that won't work if called before BT connects.
        up.setVisibility(visibility);
        down.setVisibility(visibility);
        right.setVisibility(visibility);
        left.setVisibility(visibility);
        mine.setVisibility(visibility);
        auto_mode.setVisibility(visibility);
    }

    private void buttonSender(View v) {
        String content = "";
        switch (v.getId()){
            case R.id.locMarker:
                content = LOC;
                break;
            case R.id.auto:
                content = AUTO_ON;
                break;
            case R.id.uparrow:
                content = UP;
                break;
            case R.id.downarrow:
                content = DOWN;
                break;
            case R.id.rightarrow:
                content = RIGHT;
                break;
            case R.id.leftarrow:
                content = LEFT;
                break;

        }
        if(bluetooth.isBluetoothAvailable() && bluetooth.getConnectedDeviceName() != null) { //to ensure it won't crash if somehow used not connected.
            appendLog("Sent: " + content);
            bluetooth.send(content, true);
        }
    }

    private void startBluetooth() {
        //Bt setup
        if (bluetooth.isBluetoothAvailable()) {
            if (!bluetooth.isBluetoothEnabled()) {
                bluetooth.enable();
            } else {
                if (!bluetooth.isServiceAvailable()) {
                    bluetooth.setupService();
                    bluetooth.startService(BluetoothState.DEVICE_OTHER);
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Further BT setup
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bluetooth.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetooth.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //finish the map setup once google finishes their thing.
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json_clean)));
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMapStyle(new MapStyleOptions(getResources().getString(themer.getMapTheme())));
    }

    public void addNewMarker(String name, double latX, double latY) {
        //add a marker on the map and center the view on it
        LatLng pos = new LatLng(latX, latY);
        map.addMarker(new MarkerOptions().position(pos).title(name));
        map.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    private void nextTheme() {
        themer.nextTheme(this);
    }

    public void appendLog(String text) {
        //add to the log file the text. if it doesn't exist create it. uses file defined in onCreate.
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

