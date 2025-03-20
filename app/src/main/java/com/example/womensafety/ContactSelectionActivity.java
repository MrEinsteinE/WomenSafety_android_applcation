package com.example.womensafety;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactSelectionActivity extends AppCompatActivity {

    private static final int MAX_CONTACTS = 5;
    private static final int REQUEST_READ_CONTACTS = 100;

    private ListView listView;
    private Button doneButton;

    // We'll keep an internal list of all phone numbers from the device
    private List<String> allContacts = new ArrayList<>();

    // The user’s current selection (up to 5 contacts)
    private Set<String> selectedContacts = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        listView = findViewById(R.id.contacts_list_view);
        doneButton = findViewById(R.id.done_btn);

        // 1. Check permission for READ_CONTACTS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadAndDisplayContacts();
        } else {
            requestReadContactsPermission();
        }

        // Done button returns the selected contacts to calling Activity
        doneButton.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putStringArrayListExtra("SELECTED_CONTACTS", new ArrayList<>(selectedContacts));
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void loadAndDisplayContacts() {
        allContacts.clear(); // Clear list before loading

        // 2. Load phone numbers from the device
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            // Use a Set to avoid duplicates
            Set<String> tempSet = new HashSet<>();
            while (cursor.moveToNext()) {
                // Get phone number and strip spaces
                String number = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                ).replaceAll("\\s+", "");

                tempSet.add(number);
            }
            cursor.close();

            // Convert the set to a list
            allContacts.addAll(tempSet);
        }

        // 3. Sort the contact list for better UX
        Collections.sort(allContacts);

        // 4. Display in a multiple-choice ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                allContacts
        );
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // 5. Handle item clicks
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedNumber = allContacts.get(position);
            if (listView.isItemChecked(position)) {
                if (selectedContacts.size() < MAX_CONTACTS) {
                    selectedContacts.add(selectedNumber);
                } else {
                    // If already 5 selected, uncheck the new one
                    listView.setItemChecked(position, false);
                    Toast.makeText(ContactSelectionActivity.this,
                            "Max 5 contacts allowed!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            } else {
                selectedContacts.remove(selectedNumber);
            }
        });
    }

    private void requestReadContactsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_READ_CONTACTS
        );
    }

    // 6. Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAndDisplayContacts();
            } else {
                Toast.makeText(this,
                        "Permission Denied! Cannot load contacts.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
