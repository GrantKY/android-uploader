package com.nightscout.android.mqtt;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import com.google.common.collect.Lists;
import com.nightscout.core.mqtt.Constants;
import com.nightscout.core.mqtt.MqttPinger;
import com.nightscout.core.mqtt.MqttPingerObservable;
import com.nightscout.core.mqtt.MqttPingerObserver;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.Locale;

public class AndroidMqttPinger implements MqttPinger, MqttPingerObservable {
    private static final String TAG = AndroidMqttPinger.class.getSimpleName();
    private Context context;
    private MqttPingerReceiver pingerReceiver;
    private PendingIntent pingerPendingIntent;
    private Intent pingerIntent;
    private int instanceId;
    private MqttClient mqttClient = null;
    private boolean active = false;
    private AlarmManager alarmMgr;
    private long keepAliveInterval;
    private String keepAliveTopic = "/users/%s/keepalive";
    private List<MqttPingerObserver> observers;

    public AndroidMqttPinger(Context context, int instanceId) {
        this.context = context;
        this.instanceId = instanceId;
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        observers = Lists.newArrayList();
    }

    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void setKeepAliveTopic(String keepAliveTopic) {
        this.keepAliveTopic = keepAliveTopic;
    }

    @Override
    public void ping() {
        if (! isActive()){
            return;
        }
        Log.d(TAG, "Sending keepalive to " + mqttClient.getServerURI() + " deviceID=>" + "connection_"+instanceId);
        MqttMessage message = new MqttMessage(Constants.MQTT_KEEP_ALIVE_MESSAGE);
        message.setQos(Constants.MQTT_KEEP_ALIVE_QOS);
        try {
            mqttClient.publish(String.format(Locale.US, keepAliveTopic, "connection_"+instanceId),message);
            reset();
        } catch (MqttException e) {
            Log.wtf(TAG,"Exception during ping",e);
            Log.wtf(TAG,"Reason code:"+e.getReasonCode());
            for (MqttPingerObserver observer:observers){
                observer.onFailedPing();
            }
        }
    }

    @Override
    public void start() {
        if (! isActive()) {
            pingerReceiver = new MqttPingerReceiver(this);
            context.registerReceiver(pingerReceiver, new IntentFilter(Constants.KEEPALIVE_INTENT_FILTER));
            active = true;
            ping();
        }
    }

    @Override
    public void stop() {
        if (isActive()) {
            context.unregisterReceiver(pingerReceiver);
            active = false;
            alarmMgr.cancel(pingerPendingIntent);
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setKeepAliveInterval(long ms) {
        keepAliveInterval = ms;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void reset() {
        if (! isActive()){
            return;
        }
        Log.d(TAG,"Canceling previous alarm");
        alarmMgr.cancel(pingerPendingIntent);
        Log.d(TAG,"Setting next keep alive to trigger in "+(Constants.KEEPALIVE_INTERVAL-3000)/1000+" seconds");
        pingerIntent = new Intent(Constants.KEEPALIVE_INTENT_FILTER);
        pingerIntent.putExtra("device", instanceId);
        pingerPendingIntent = PendingIntent.getBroadcast(context, 61, pingerIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + keepAliveInterval - 3000, pingerPendingIntent);
        else
            alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + keepAliveInterval - 3000, pingerPendingIntent);

    }

    @Override
    public boolean isNetworkActive() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isAvailable() &&
                cm.getActiveNetworkInfo().isConnected());
    }

    @Override
    public void registerObserver(MqttPingerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterObserver(MqttPingerObserver observer) {
        observers.remove(observer);
    }
}
