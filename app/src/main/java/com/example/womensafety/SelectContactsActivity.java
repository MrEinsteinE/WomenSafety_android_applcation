package com.example.womensafety;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class SelectContactsActivity extends AppCompatActivity {

    private static final int MAX_CONTACTS = 5;
    private Set<String> contactSet = new HashSet<>();

    private Button addContactBtn, doneBtn;
    private TextView selectedContactsTV;

    // 1) Create an ActivityResultLauncher for picking a contact
    private final ActivityResultLauncher<Intent> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Uri contactUri = result.getData().getData();
                                if (contactUri != null) {
                                    // Query contact number
                                    try (Cursor cursor = getContentResolver().query(
                                            contactUri,
                                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                            null, null, null)) {
                                        if (cursor != null && cursor.moveToFirst()) {
                                            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                            String number = cursor.getString(numberIndex).replaceAll("\\s+", "");
                                            if (contactSet.size() < MAX_CONTACTS) {
                                                contactSet.add(number);
                                                updateUI();
                                            } else {
                                                Toast.makeText(SelectContactsActivity.this, "Max 5 contacts allowed!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(SelectContactsActivity.this, "Error selecting contact", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);

        // Ensure IDs match the layout file
        addContactBtn = findViewById(R.id.add_contact_btn);
        doneBtn = findViewById(R.id.done_btn);
        selectedContactsTV = findViewById(R.id.selected_contacts_tv);

        // Set click listeners
        addContactBtn.setOnClickListener(v -> {
            if (contactSet.size() < MAX_CONTACTS) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                contactPickerLauncher.launch(pickContactIntent);
            } else {
                Toast.makeText(this, "Max 5 contacts allowed!", Toast.LENGTH_SHORT).show();
            }
        });

        doneBtn.setOnClickListener(v -> {
            saveContacts();
            finish();
        });
    }
    // Update the UI to show selected contacts
    private void updateUI() {
        if (contactSet.isEmpty()) {
            selectedContactsTV.setText("No contacts selected");
        } else {
            StringBuilder builder = new StringBuilder("Selected Contacts:\n");
            for (String contact : contactSet) {
                builder.append(contact).append("\n");
            }
            selectedContactsTV.setText(builder.toString());
        }
    }

    // Save contacts to SharedPreferences
    private void saveContacts() {
        SharedPreferences sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("ENUM", contactSet);
        editor.apply();
    }
}