package com.example.womensafety;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton selectContactsButton; // Button to trigger contact selection
    private TextView contactsTextView; // Optional: separate TextView for displaying contacts
    private ActivityResultLauncher<String[]> multiplePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        selectContactsButton = findViewById(R.id.select_contacts);
        // Uncomment and adjust if there's a separate TextView for displaying contacts
        // contactsTextView = findViewById(R.id.contacts_text_view);

        // Set click listener to show popup menu for changing contacts
        selectContactsButton.setOnClickListener(v -> showPopupMenu(v));

        // Set up click listeners for other sections
        findViewById(R.id.first).setOnClickListener(this);
        findViewById(R.id.second).setOnClickListener(this);

        // Initialize permissions and notification channel
        setupPermissions();
        createNotificationChannel();

        LinearLayout fakeCallLayout = findViewById(R.id.fake_call);
        fakeCallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FakeCallActivity.class);
                startActivity(intent);
            }
        });
    }

    /** Sets up the permission request launcher */
    private void setupPermissions() {
        multiplePermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            showPermissionSnackbar(entry.getKey());
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContactList();
    }

    /** Refreshes the SOS contact list from SharedPreferences */
    private void refreshContactList() {
        SharedPreferences sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        Set<String> contacts = sp.getStringSet("sos_contacts", new HashSet<>());

        if (contacts.isEmpty()) {
            startActivity(new Intent(this, SelectContactsActivity.class));
        } else {
            // Update UI if there's a TextView for contacts
            // updateContactTextView(contacts);
        }
    }

    /** Updates the TextView with the list of SOS contacts */
    private void updateContactTextView(Set<String> contacts) {
        // Uncomment and use if contactsTextView is present in the layout
        // StringBuilder builder = new StringBuilder("SOS Contacts:\n");
        // for (String contact : contacts) {
        //     builder.append(contact).append("\n");
        // }
        // contactsTextView.setText(builder.toString());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.second) {
            startActivity(new Intent(this, LawsActivity.class));
        } else if (id == R.id.first) {
            startActivity(new Intent(this, DefenseActivity.class));
        }
    }

    // --- Service Control Methods ---

    /** Starts the emergency service if permissions are granted */
    public void startServiceV(View view) {
        if (hasRequiredPermissions()) {
            Intent serviceIntent = new Intent(this, ServiceMine.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Snackbar.make(findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show();
        } else {
            requestPermissions();
        }
    }

    /** Sends a stop intent to the emergency service */
    public void stopService(View view) {
        Intent stopIntent = new Intent(this, ServiceMine.class);
        stopIntent.setAction("STOP");
        startService(stopIntent); // Changed to startService since the service is already running
        Snackbar.make(findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show();
    }

    // --- Popup Menu Handling ---

    /** Shows a popup menu to change SOS contacts */
    public void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.changeNum) {
                startActivity(new Intent(this, SelectContactsActivity.class));
                return true;
            }
            return false;
        });

        popup.show();
    }

    // --- Permission Handling ---

    /** Checks if all required permissions are granted */
    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    /** Requests required permissions */
    private void requestPermissions() {
        // Add permission rationale here if needed using shouldShowRequestPermissionRationale
        multiplePermissions.launch(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_CONTACTS
        });
    }

    /** Shows a Snackbar when a permission is denied */
    private void showPermissionSnackbar(String permission) {
        Snackbar.make(findViewById(android.R.id.content),
                        "Permission required for core functionality",
                        Snackbar.LENGTH_INDEFINITE)
                .setAction("GRANT", v -> multiplePermissions.launch(new String[]{permission}))
                .show();
    }

    /** Creates a notification channel for emergency alerts */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "SAFETY_CHANNEL",
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}