package com.shabab.a.websocket;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shabab.a.websocket.util.DeviceInfo;
import com.websocket.client.QSocket;
import com.websocket.client.QSocketOptions;
import com.websocket.client.channel.Channel;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.channel.ChannelUnsubscriptionEventListener;
import com.websocket.client.connection.ConnectionEventListener;
import com.websocket.client.connection.ConnectionState;
import com.websocket.client.connection.ConnectionStateChange;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/*import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;*/

public class SecondActivity extends AppCompatActivity {
  //  StompClient mStompClient;
  private QSocket qSocket;
    private final long startTime = System.currentTimeMillis();
    private Channel channel;
    private TextView dataTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);
        dataTV = (TextView) findViewById(R.id.dataTV);
        Button button=findViewById(R.id.button2);
        QSocketOptions options = new QSocketOptions().setAuthorizationToken("1234567890").setEncrypted(false);
        options.setHost("192.168.1.55:8080/hello");
       // options.setWsPort(8080);
     //   options.
        qSocket = new QSocket(options, this);
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        connect();
    }
});

    //connect();
    }


    public void connect() {
        qSocket.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("WebsocketConnection", String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                        change.getPreviousState(), change.getCurrentState()));


                 Log.e("WebsocketConnection", String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                        change.getPreviousState(), change.getCurrentState()));



                doDisplay(String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                        change.getPreviousState(), change.getCurrentState()));
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("WebsocketConnection", String.format("[%d] An error was received with message [%s], code [%s], exception [%s]",
                        timestamp(), message, code, e));
                doDisplay(String.format("[%d] An error was received with message [%s], code [%s], exception [%s]",
                        timestamp(), message, code, e));
            }
        }, ConnectionState.ALL);
    }

    public void disconnect(View view) {
        qSocket.disconnect();
    }

    public void subscribeChannel(View view) {
        channel = qSocket.subscribe("/topic/greetings2", new ChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channelName) {
                Log.i("channelSubscription", String.format("[%d] Subscription to channel [%s] succeeded", timestamp(), channelName));
                doDisplay(String.format("[%d] Subscription to channel [%s] succeeded", timestamp(), channelName));
            }

            @Override
            public void onEvent(String channelName, String eventName, String data) {
                Log.i("ReceivedEventData:", String.format("[%d] Received event [%s] on channel [%s] with data [%s]", timestamp(),
                        eventName, channelName, data));
                doDisplay(String.format("[%d] Received event [%s] on channel [%s] with data [%s]", timestamp(),
                        eventName, channelName, data));
            }
        });
    }

    public void unsubscribeChannel(View view) {
        qSocket.unsubscribe("Channel B", new ChannelUnsubscriptionEventListener() {
            @Override
            public void onUnsubscribed(String channelName) {
                Log.i("channelUnsubscription", String.format("[%d] Unsubscription to channel [%s] succeeded", timestamp(), channelName));
                doDisplay(String.format("[%d] Unsubscription to channel [%s] succeeded", timestamp(), channelName));
            }
        });
    }

    private long timestamp() {
        return System.currentTimeMillis() - startTime;
    }

    private void doDisplay(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dataTV.setText(data);
            }
        });
    }




}
