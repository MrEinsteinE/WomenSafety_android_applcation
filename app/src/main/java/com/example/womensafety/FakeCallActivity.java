package com.example.womensafety; // Replace with your package name

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FakeCallActivity extends AppCompatActivity {

    private TextView callTimer;
    private Button endCallButton;
    private Handler handler;
    private Runnable timerRunnable;
    private long startTime;
    private MediaPlayer mediaPlayer; // For playing the ringtone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fake_call);

        // Initialize UI elements
        callTimer = findViewById(R.id.call_timer);
        endCallButton = findViewById(R.id.end_call_button);

        // Set up the timer
        handler = new Handler();
        startTime = System.currentTimeMillis();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
                callTimer.setText(String.format("%02d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        // Start the timer
        handler.post(timerRunnable);

        // Play the ringtone
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone); // Audio file in res/raw
        mediaPlayer.setLooping(true); // Keep playing until stopped
        mediaPlayer.start();

        // End the call when the button is clicked
        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRingtone(); // Stop the audio
                finish(); // Close the activity
            }
        });
    }

    // Method to stop and release the ringtone
    private void stopRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable); // Stop the timer
        stopRingtone(); // Stop the ringtone when the activity ends
    }
}