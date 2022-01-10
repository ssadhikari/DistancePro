package com.example.distancepro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SessionDialog.SessionDialogListener {
    private static final int REQUEST_ENABLE_BT=0;
    private static final int REQUEST_DISCOVER_BT=1;
    private final static int MESSAGE_READ = 2;


    private Handler handler; // Our main handler that will receive callback notifications
    private ConnectedThread connectedThread;

    MediaPlayer player;

    public boolean status1= true;
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    androidx.appcompat.widget.Toolbar toolbar;

    TextView distance;
    private ListView paredDevices;
    Button turnOn,turnOff,discoverable,pairedList,startBtn,stopBtn,uploadBtn;
    RadioGroup radioGroup;
    RadioButton meter,centiMeter;
    ImageView status;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    public BluetoothSocket btsocket;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    DBhelper DB;
    public boolean stat;
    public String dataOut = null;

    @SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB = new DBhelper(this);
        stat = false;



        distance = findViewById(R.id.distance);
        paredDevices = (ListView)findViewById(R.id.pairedDevices);

        turnOn = findViewById(R.id.turnOn);
        turnOff = findViewById(R.id.turnOff);
        discoverable = findViewById(R.id.discoverable);
        pairedList = findViewById(R.id.getPaired);

        startBtn = findViewById(R.id.btn_start);
        stopBtn = findViewById(R.id.btn_stop);
        uploadBtn = findViewById(R.id.upload_btn);

        status = findViewById(R.id.blueStatus);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        nav=(NavigationView)findViewById(R.id.naveMenu);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle( this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
//        getSupportActionBar().set
        btArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        radioGroup = findViewById(R.id.radioGroup);
        meter = findViewById(R.id.meter);
        centiMeter = findViewById(R.id.centimeter);
        // get a handle on the bluetooth radio
        player = MediaPlayer.create(MainActivity.this,R.raw.alarm1);






        // navigation bar algorithm
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.menuHome :
                        Toast.makeText(getApplicationContext(),"Home Panel is Open",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.uploadData :
                        Toast.makeText(getApplicationContext(), "your session data are uploaded",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.downloadData : // offline mode action
                        Toast.makeText(getApplicationContext(),"you can load previous data",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        status1 = true;

                        paredDevices.setAdapter(btArrayAdapter);
                        Cursor res = DB.getData(); // get data from database
                        btArrayAdapter.clear();
                        while (res.moveToNext()){
                            String sessions = res.getString(0)+"="+res.getString(1);
                            btArrayAdapter.add(sessions);
                        }



                        //setContentView(R.layout.activity_offline_mode);
                        //Intent i = new Intent(MainActivity.this,OfflineMode.class);
                        //startActivity(i);
                        break;
                    case R.id.settings :
                        Toast.makeText(getApplicationContext(),"settings panel is open",Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;



                }
                return true;
            }
        });
        handler = new Handler(){
            @SuppressLint("HandlerLeak")
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    String values[] = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                        values =  readMessage.split(",");


                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //String finalReadMessage = readMessage;
                    float value = 0;
                    if (values[0].length()!=0){
                        value = Float.parseFloat(values[0]);
                    }
                    if (!stat){

                    if ( value <= 20){
                        distance.setTextColor(Color.parseColor("#FF0000"));
                        player.start();
                    }else {
                        distance.setTextColor(Color.parseColor("#0039FF"));
                        player.pause();
                    }}
                    String output = null;
                    if (!meter.isChecked() & !centiMeter.isChecked()){
                        output = "select units";
                    }



                    if (meter.isChecked()){
                        value =value/100;
                        output = String.valueOf(value)+" m";

                    }else if (centiMeter.isChecked()){
                        output = values[0]+" cm";
                    }
                    distance.setText(output);
                    stopBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dataOut = distance.getText().toString();
                            distance.setText("stopped");
                            stopBtn.setEnabled(false);
                            startBtn.setEnabled(true);
                            uploadBtn.setEnabled(true);
                            showToast("stopped streaming data");
                            stat = true;
                        }
                    });
                    if (stat){
                        distance.setTextColor(Color.parseColor("#0039FF"));
                        distance.setText("stopped");
                        player.pause();
                    }



                }

            }
        };
        //set image according to bluetooth status
        if (mBluetoothAdapter.isEnabled()){
            status.setImageResource(R.drawable.ic_baseline_bluetooth_24);
        }
        else {
            status.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
        }
        centiMeter.setActivated(true);
        player.start();
        player.pause();
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        uploadBtn.setEnabled(false);


        //turn on button action
        turnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()){
                    Toast.makeText(getApplicationContext(),"turning on",Toast.LENGTH_SHORT).show();
                    //mBluetoothAdapter.enable();
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent,REQUEST_ENABLE_BT);
                    status.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                }
                else {
                    Toast.makeText(getApplicationContext(),"already turned On",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //discover bluetooth btn
        discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isDiscovering()){
                    showToast("Making your device Discoverable ");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent,REQUEST_DISCOVER_BT);
                }

            }
        });

        //off btn click
        turnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.disable();
                    showToast("Turning Bluetooth Off");
                    status.setImageResource(R.drawable.ic_baseline_bluetooth_disabled_24);
                }
                else {
                    showToast("Already Turned Off");
                }

            }
        });


        //get paired list
        pairedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status1= false;
                if (!status1){
                paredDevices.setAdapter(btArrayAdapter);
                //paredDevices.setOnItemClickListener(mDeviceClickListner);
                if (mBluetoothAdapter.isEnabled()){

                    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices(); //get paired devices
                    btArrayAdapter.clear();
                    for (BluetoothDevice device: devices){
                        String name = device.getName() + "\n" + device.getAddress();
                        btArrayAdapter.add(name);
                    }

                }
                else {
                    //if bluetooth is off
                    showToast("Turn on bluetooth to get paired devices");
                }
                paredDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String info = ((TextView) view).getText().toString();
                        final String address = info.substring(info.length() - 17);
                        final String name = info.substring(0,info.length() - 17);

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                        try {
                            btsocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                            btsocket.connect();
                            if (btsocket.isConnected()){
                                showToast("Connection successful");
                                connectedThread = new ConnectedThread(btsocket);
                                connectedThread.start(); //data capturing start


                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                    }
                });

            }}
        });
        //start button action
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);
                uploadBtn.setEnabled(false);
                stat = false;

            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
                //showToast("file is saving");

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode==RESULT_OK){
                    //bluetooth on
                    status.setImageResource(R.drawable.ic_baseline_bluetooth_24);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void showToast (String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    public void openDialog(){
        SessionDialog sessionDialog = new SessionDialog();
        sessionDialog.show(getSupportFragmentManager(),"session dialog");
    }

    @Override
    public void applyTexts(String sessionId) {
        System.out.println(DB.getWritableDatabase());
         DB.insertsessiondata(sessionId,dataOut);
        System.out.println(DB.getData());
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        InputStreamReader aReader;
        BufferedReader mBufferReader;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                //System.out.println(tmpIn);
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[10240];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?

                        try{
                            bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                            handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget(); // Send the obtained bytes to the UI activity
                        }catch (ArrayIndexOutOfBoundsException e) {
                            //System.out.println(bytes);
                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    //private void



}