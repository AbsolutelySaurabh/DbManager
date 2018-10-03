package dexter.appsmoniac.androidsocket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import dexter.appsmoniac.androidsocket.database.BikesDBHelper;
import dexter.appsmoniac.androidsocket.database.ContactDBHelper;
import dexter.appsmoniac.androidsocket.database.ExtTestDBHelper;
import dexter.appsmoniac.androidsocket.database.PersonDBHelper;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Set<String> stringSet = new HashSet<>();
        stringSet.add("SetOne");
        stringSet.add("SetTwo");
        stringSet.add("SetThree");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences prefsOne = getSharedPreferences("prefOne", Context.MODE_PRIVATE);
        SharedPreferences prefsTwo = getSharedPreferences("prefTwo", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("testOne", "one").commit();
        sharedPreferences.edit().putInt("testTwo", 5).commit();
        sharedPreferences.edit().putLong("testThree", 10000L).commit();
        sharedPreferences.edit().putFloat("testFour", 4.21F).commit();
        sharedPreferences.edit().putBoolean("testFive", true).commit();
        sharedPreferences.edit().putStringSet("testSix", stringSet).commit();

        prefsOne.edit().putString("testOne", "one").commit();

        prefsTwo.edit().putString("testTwo", "two").commit();

        ContactDBHelper contactDBHelper = new ContactDBHelper(getApplicationContext());
        if (contactDBHelper.count() == 0) {
            for (int i = 0; i < 100; i++) {
                String name = "contact_name" + i;
                String phone = "phone_number" + i;
                String email = "email_address" + i;
                String street = "street_address" + i;
                String place = "place" + i;
                contactDBHelper.insertContact(name, phone, email, street, null);
            }
        }

        BikesDBHelper bikeDBHelper = new BikesDBHelper(getApplicationContext());
        if (bikeDBHelper.count() == 0) {
            for (int i = 0; i < 50; i++) {
                String name = "bike_name" + i;
                String color = "BLACK";
                float mileage = i + 10.45f;
                bikeDBHelper.insertBike(name, color, mileage);
            }
        }

        ExtTestDBHelper extTestDBHelper = new ExtTestDBHelper(getApplicationContext());
        if (extTestDBHelper.count() == 0) {
            for (int i = 0; i < 20; i++) {
                String value = "value_" + i;
                extTestDBHelper.insertTest(value);
            }
        }

        // Create Person encrypted database
        PersonDBHelper personDBHelper = new PersonDBHelper(getApplicationContext());
        if (personDBHelper.count() == 0) {
            for (int i = 0; i < 100; i++) {
                String firstName = PersonDBHelper.PERSON_COLUMN_FIRST_NAME + "_" + i;
                String lastName = PersonDBHelper.PERSON_COLUMN_LAST_NAME + "_" + i;
                String address = PersonDBHelper.PERSON_COLUMN_ADDRESS + "_" + i;
                personDBHelper.insertPerson(firstName, lastName, address);
            }
        }

        Utils.setCustomDatabaseFiles(getApplicationContext());
    }

    //show toast on click
    public void showDebugDbAddress(View view) {
        Utils.showDebugDBAddressLogToast(getApplicationContext());
    }
}
