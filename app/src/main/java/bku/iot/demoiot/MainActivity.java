package bku.iot.demoiot;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.appcompat.widget.SwitchCompat;

        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.os.Build;
        import android.os.Handler;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Locale;
        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.CompoundButton;
        import android.widget.RelativeLayout;
        import android.widget.TextView;

        import com.github.angads25.toggle.interfaces.OnToggledListener;
        import com.github.angads25.toggle.model.ToggleableView;
        import com.github.angads25.toggle.widget.LabeledSwitch;

        import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
        import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
        import org.eclipse.paho.client.mqttv3.MqttException;
        import org.eclipse.paho.client.mqttv3.MqttMessage;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.nio.charset.Charset;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.Locale;

        import androidx.appcompat.widget.SwitchCompat;
        import androidx.core.app.NotificationCompat;
        import androidx.core.app.NotificationManagerCompat;

        import android.widget.CompoundButton;
        import android.widget.Toast;

// test git on android studio
public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    String username = "IOT_232";
    String key = "";
    int ADD_ACTIVITY_REQUEST_CODE = 1;
    String cycle, flow1, flow2, flow3, hour1, minute1, hour2, minute2, area;
    TextView txtTemp, txtTime, txtHumid, txtStatus, txtTemp_Predict, txtHumid_Predict;
    Handler timeHandler = new Handler();
    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the time using a simple date format
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            txtTime.setText(currentTime);

            // Post the task again with a delay of 1000 milliseconds (1 second)
            timeHandler.postDelayed(this, 1000);
        }
    };
//    SwitchCompat btnLed, btnPump;
//    String username, key;
    RelativeLayout TempCard, HumidCard, LightCard;

//    private boolean isWifiConnected = false, isOn = false;

//    private ArrayList<String> LightList = new ArrayList<>();
    private ArrayList<String> TempList = new ArrayList<>();
    private ArrayList<String> HumidList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent intent = getIntent();
////        Gui va nhan key tu login activity
//        username = intent.getStringExtra("username");
//        key = intent.getStringExtra("key");
        createNotificationChannel();
        txtTemp = findViewById(R.id.TempValue);
        txtHumid = findViewById(R.id.HumidValue);
        txtStatus = findViewById(R.id.SystemValue);
        txtTime = findViewById(R.id.time);
        txtTemp_Predict = findViewById(R.id.TempPredictValue);
        txtHumid_Predict = findViewById(R.id.HumidPredictValue);
        timeHandler.post(timeRunnable);
//        txtTime.setText(timeFormat);
//        txtAI = findViewById(R.id.AIValue);
//        btnLed = findViewById(R.id.switchLight);
//        btnPump = findViewById(R.id.switchPump);
        TempCard = findViewById(R.id.TempCard);
        HumidCard = findViewById(R.id.HumidCard);
        LightCard = findViewById(R.id.LightCard);


        TempCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] templist= TempList.toArray(new String[0]);
                Intent intent = new Intent(MainActivity.this, TempActivity.class);
                intent.putExtra("TempList", templist);
                startActivity(intent);
            }
        });
        HumidCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] humidlist = HumidList.toArray(new String[0]);
                Intent intent = new Intent(MainActivity.this, HumidActivity.class);
                intent.putExtra("HumidList", humidlist);
                startActivity(intent);
            }
        });
        LightCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LightActivity.class);

                startActivityForResult(intent, ADD_ACTIVITY_REQUEST_CODE);
            }
        });
        startMQTT();
    }
    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel Name"; // The user-visible name of the channel.
            String description = "My Channel Description"; // The user-visible description of the channel.
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // Set the importance level
            String CHANNEL_ID = "1104"; // Channel ID
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void sendNotification() {
        String CHANNEL_ID = "1104";

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.user)
                .setContentTitle("The envinronment has change recently, time to set new schedule!")
                .setContentText("Tap to open app")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(123, builder.build());
    }
    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){
        }
    }
    public void startMQTT() {
        mqttHelper = new MQTTHelper(this, username, key);
        mqttHelper.setConnectionFailedListener(new MQTTHelper.ConnectionFailedListener() {
            @Override
            public void onConnectionFailed() {
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
                finish();
            }
        });
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived (String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if (topic.contains("deviceActive")) {
                    if (message.toString().equals("0")) {
                        txtStatus.setText("NO DEVICE IS RUNNING");
                    } else if (message.toString().equals("1")) {
                        txtStatus.setText("MIXER1 IS RUNNING");
                    } else if (message.toString().equals("2")) {
                        txtStatus.setText("MIXER2 IS RUNNING");
                    } else if (message.toString().equals("3")) {
                        txtStatus.setText("MIXER3 IS RUNNING");
                    } else if (message.toString().equals("4")) {
                        txtStatus.setText("PUMPING IN");
                    } else if (message.toString().equals("5")) {
                        txtStatus.setText("CHOOSING AREA 1");
                    } else if (message.toString().equals("6")) {
                        txtStatus.setText("CHOOSING AREA 2");
                    } else if (message.toString().equals("7")) {
                        txtStatus.setText("CHOOSING AREA 3");
                    } else if (message.toString().equals("8")) {
                        txtStatus.setText("PUMPING OUT");
                    } else if (message.toString().equals("9")) {
                        txtStatus.setText("FINISH");
                    }
                } else if (topic.contains("announceUser")) {
                    if (message.toString().equals("1")){
                        Toast.makeText(MainActivity.this, "It's time to add schedule", Toast.LENGTH_SHORT).show();
                        sendNotification();
                    }
                } else if(topic.contains("temp_predict")){
                    txtTemp_Predict.setText(message.toString() + "°C");
                } else if(topic.contains("temp")){
                    txtTemp.setText(message.toString() + "°C");
                    TempList.add(message.toString());
                } else if(topic.contains("moist_predict")){
                    txtHumid_Predict.setText(message.toString() + "%");
                } else if(topic.contains("moist")){
                    txtHumid.setText(message.toString() + "%");
                    HumidList.add(message.toString());
                }
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String jsonData = data.getStringExtra("jsonData");
                sendDataMQTT(username + "/feeds/command", jsonData);
                Log.d("TEST","Publish command");
            }
        }
    }

}