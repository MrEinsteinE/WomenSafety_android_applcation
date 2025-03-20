package com.example.womensafety;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.List;

public class EmergencyService extends Service {

    private static final String CHANNEL_ID = "MYID";
    private List<String> emergencyContacts = Arrays.asList("+1234567890", "+0987654321"); // Replace with actual numbers
    private static final String EMERGENCY_MESSAGE = "I'm in danger! Please help. My last known location: ";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        sendEmergencyAlerts();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopSelf();
        } else {
            startForeground(1, getNotification("Emergency Alert is Active"));
            sendEmergencyAlerts();
        }
        return START_STICKY;
    }

    private void sendEmergencyAlerts() {
        for (String contact : emergencyContacts) {
            sendSMS(contact, EMERGENCY_MESSAGE + getLocation());
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "Alert Sent to: " + phoneNumber, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("SMS", "Failed to send SMS", e);
            Toast.makeText(getApplicationContext(), "Failed to send alert", Toast.LENGTH_LONG).show();
        }
    }

    private String getLocation() {
        return "Latitude: 28.6139, Longitude: 77.2090"; // Replace with actual GPS fetch logic
    }

    private Notification getNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Women Safety App")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Emergency Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Emergency Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
