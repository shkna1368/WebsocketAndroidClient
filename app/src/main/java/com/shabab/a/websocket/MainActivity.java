package com.shabab.a.websocket;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shabab.a.websocket.util.DeviceInfo;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;*/

public class MainActivity extends AppCompatActivity {
    //  StompClient mStompClient;
    private WebSocketClient mWebSocketClient;
    private WebSocketClient mWebSocketClientSeekbar;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = (SeekBar) findViewById(R.id.seekBar);


        // getDeviceInfo();
        askForContactPermission();

        connectWebSocket();
        connectWebSocketSeekbar();

        final Button button = findViewById(R.id.button);
        final EditText editText = findViewById(R.id.message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = editText.getText().toString();
                editText.setText("");
                try {
                    mWebSocketClient.send(msg);
                    // mStompClient.send("/topic/greetings3",Build.MANUFACTURER + " " + Build.MODEL);
                    //   mStompClient.send("salam az android......");

                } catch (Exception e) {

                    Log.e("exception", e.toString());
                }
            }
        });


        // ...


        //  new LongOperation().execute("");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                // Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setText("Covered: " + progress + "/" + seekBar.getMax());
                // Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
                sendSeekbarValue(seekBar.getProgress());
            }
        });
    }


    private void getDeviceInfo() {


        // Android version is lesser than 6.0 or the permission is already granted.
        DeviceInfo.getInstance(this);


    }


    public void askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                getDeviceInfo();
            }
        } else {
            getDeviceInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceInfo();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //ToastMaster.showMessage(this,"No permission for contacts");
                    Toast.makeText(this, "No permission for contacts", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.1.55:8080/name/" + random());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {


            @Override
            public void onOpen(ServerHandshake serverHandshake) {

                Log.e("Websocket", "Opened");

                //  mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);

/*try {
    mWebSocketClient.send(" client to server: salam az android ....");
   // mStompClient.send("/topic/greetings3",Build.MANUFACTURER + " " + Build.MODEL);
 //   mStompClient.send("salam az android......");

}
catch (Exception e){

    Log.e("exception",e.toString());
}*/
            }

            @Override
            public void onMessage(String s) {
                Log.e("message recived=", s.toString());
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) findViewById(R.id.messages);
                        textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.e("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();

    }


    private int random() {


        Random randomGenerator = new Random();

        int randomInt = randomGenerator.nextInt(100);
        return randomInt;
    }


    private void connectWebSocketSeekbar() {
        URI uri;
        try {
            uri = new URI("ws://192.168.1.55:8080/seekbar/" + random());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClientSeekbar = new WebSocketClient(uri) {


            @Override
            public void onOpen(ServerHandshake serverHandshake) {

                Log.e("Websocket", "Opened");

            }

            @Override
            public void onMessage(final String s) {
                Log.e("message recived=", s.toString());
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TextView textView = (TextView)findViewById(R.id.messages);
                        // textView.setText(textView.getText() + "\n" + message);
                        seekBar.setProgress(Integer.parseInt(s));
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.e("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClientSeekbar.connect();

    }


    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.message);
        mWebSocketClient.send(editText.getText().toString());
        editText.setText("");
    }


    public void sendSeekbarValue(int value) {

        mWebSocketClientSeekbar.send(String.valueOf(value));

    }





}
