package com.example.mapArduino;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String DARK = "dark";
    public static final String LIGHT = "light";
    public static final int themeChangeClicks = 1;
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
        //library with some docs.

        //Buttons: Auto - sends "a"
        //up arrow - 'f'
        //down arrow - 'b'
        //right arrow - 'r'
        //left arrow - 'l'

        super.onCreate(savedInstanceState);
        setTheme(themer.getTheme());
        setContentView(R.layout.activity_main);
//        logFile = new File(getFilesDir(), "/logger.txt");
        logFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/logger.txt");
        setupMap();
        initButtons();
        setupButtons();

        appendLog("game;start"); //DEBUG

        bluetooth = new BluetoothSPP(this);

        if (bluetooth.isBluetoothAvailable()) setupBluetooth();
        else {
            Toast.makeText(getApplicationContext(), R.string.bt_not_available, Toast.LENGTH_LONG).show();
//            finish();
        }


    }

    public void onStart() {
        super.onStart();
//        setTheme(R.style.DarkAppTheme);
        startBluetooth();
    }

    public void onDestroy() {
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
            }

            public void onDeviceConnectionFailed() {
                connect.setText(R.string.connect_fail);
            }
        });

        bluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { // move to not on the button. add extra buttons
            @Override
            public void onDataReceived(byte[] data, String message) {
                //TODO log to file, and add marker
                //TODO test for whether or not the string is the coords
                try {
                    addNewMarker("marker", Float.parseFloat(message.split(" ")[0]), Float.parseFloat(message.split(" ")[1]));
                    appendLog("marker@(" + message + ") added");
                } catch (NumberFormatException e) {
//                    e.printStackTrace();
                }
//                System.out.println(Arrays.toString(data));
                System.out.println(message); // "latSpacelong" is format
            }
        });
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

    private void setupButtons() {
        connect.setOnClickListener(new View.OnClickListener() {
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
//        auto_mode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (!AUTO_STATE) bluetooth.send(AUTO_ON, true);
////                else bluetooth.send(AUTO_OFF, true);
////                AUTO_STATE ^= true;
//                bluetooth.send(AUTO_ON, true);
//            }
//        });
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                shareLogText();
                shareLogFile();
            }
        });
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
        buttonVis(View.INVISIBLE);
    }

    private void shareLogFile() {
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, logFile);
//        shareIntent.setType("image/jpeg");
//        startActivity(Intent.createChooser(shareIntent, "boo"));


//        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//        sharingIntent.setType("text/*");
//        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + logFile.getAbsolutePath()));
//        startActivity(Intent.createChooser(sharingIntent, "share file with"));
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", logFile));
        startActivity(Intent.createChooser(sharingIntent, "share file with"));

    }

    private void shareLogText() {
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
        spooky.setVisibility(View.VISIBLE);
        spooky.setBackgroundColor(Color.TRANSPARENT);
//        spooky.setTextColor(Color.WHITE);
        spooky.setOnClickListener(new View.OnClickListener(){
            int a = themer.spooky;
            @Override
            public void onClick(View v){
//                if(a % 12 == 5) themeChange(DARK);
//                else if(a % 12 == 11) themeChange(LIGHT);
//                a++;
//                themer.spooky = a;
                System.out.println("theme?");
                if(a % themeChangeClicks == (themeChangeClicks - 1)){
                    System.out.println("Changed theme!");
                    themer.spooky = a + 1;
                    nextTheme();
                }
                a++;
            }
        });
    }

    private void setupArrowKeys() {
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
        up.setVisibility(visibility);
        down.setVisibility(visibility);
        right.setVisibility(visibility);
        left.setVisibility(visibility);
        mine.setVisibility(visibility);
//        auto_mode.setVisibility(visibility);
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
        System.out.println("Sent: " + content);
        appendLog("Sent: " + content);
        bluetooth.send(content, true);
    }

    private void startBluetooth() {
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
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json_clean)));
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMapStyle(new MapStyleOptions(getResources().getString(themer.getMapTheme())));
//        addNewMarker("Jerusalem", 31.746421, 35.239667);
    }

    public void addNewMarker(String name, double latX, double latY){
        LatLng pos = new LatLng(latX, latY);
        map.addMarker(new MarkerOptions().position(pos).title(name));
        map.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    private void themeChange(String name){
        switch(name){
            case DARK:
                themer.changeToTheme(this, 1);
                break;
            case LIGHT:
                themer.changeToTheme(this, 0);
                break;

            default:

        }
    }

    private void nextTheme() {
        themer.nextTheme(this);
    }

    public void appendLog(String text) {
//        System.out.println(String.valueOf(getDataDir()+ "/LiterallyNothing.txt"));
//        setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, filename);
//        File logFile = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)), "maplog.txt");
//        System.out.println(getFilesDir()+ "/logger.txt");
//        File logFile = new File("/storage/self/primary/Android/data/com.example.maptest/files/mmm.txt");
        System.out.println(Environment.getExternalStorageDirectory());

        if (!logFile.exists())
        {
            try
            {
                System.out.println("newFile");
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void dealWithPerms() {
//        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        requestPermissions(permissions, 2);
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 2); // your request code
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 3); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
}

