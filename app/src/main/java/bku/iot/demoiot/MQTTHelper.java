package bku.iot.demoiot;
//Change to main brandch
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.TimeUnit;


public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;

    public final String[] arrayTopics = {"/feeds/deviceActive", "/feeds/announceUser", "/feeds/temp", "/feeds/moist", "/feeds/temp_predict", "/feeds/moist_predict"};

    final String clientId = "310503";
    final String username = "IOT_232";
    final String key = "";

    final String serverUri = "tcp://io.adafruit.com:1883";

    public MQTTHelper(Context context, String username, String key){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
//        this.username = username;
//        this.key = key;
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public interface ConnectionFailedListener {
        void onConnectionFailed();
    }

    private ConnectionFailedListener connectionFailedListener;
    public void setConnectionFailedListener(ConnectionFailedListener connectionFailedListener) {
        this.connectionFailedListener = connectionFailedListener;
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(this.username);
        mqttConnectOptions.setPassword(this.key.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                    if (connectionFailedListener != null) {
                        connectionFailedListener.onConnectionFailed();
                    }
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void subscribeToTopic() {
        for (String arrayTopic : arrayTopics) {
            final String fullTopic = username + arrayTopic;
            try {
                mqttAndroidClient.subscribe(username + arrayTopic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("TEST", "Subscribed");
//                        requestLastData(fullTopic + "/get");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d("TEST", "Subscribed fail!");

                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exceptionst subscribing");
                ex.printStackTrace();
            }
        }
    }
    public void requestLastData(String topic) {
        try {
            mqttAndroidClient.publish(topic, new MqttMessage("".getBytes())); // Publish to get the last data
        } catch (MqttException e) {
            Log.e("Mqtt", "Failed to request data from: " + topic, e);
        }
    }
}