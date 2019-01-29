package com.example.android.todolist.settings;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.android.todolist.R;
import com.example.android.todolist.utilities.ReminderUtils;
import com.example.android.todolist.utilities.NotificationUtils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.ll_set_time)
    LinearLayout ll;
    @BindView(R.id.tv_display_time)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        ActionBar actionBar = this.getSupportActionBar();

        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        showNotificationScheduler();
    }

    // This method allows the user to set reminder notification on UI for the future or not
    private void showNotificationScheduler() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        boolean notification = sharedPreferences.getBoolean(getString(R.string.pref_key_notification), getResources().getBoolean(R.bool.notification_by_default));
        if (notification) {
            ll.setVisibility(View.VISIBLE);

            //Use the current time as the default values for the time picker dialog
            final Calendar c = Calendar.getInstance();
            final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
            final int minute = c.get(Calendar.MINUTE);

            // TextView updated to the current time in "12:00" format
            tv.setText(String.valueOf(hourOfDay) + ":" + String.format("%02d", minute));

            // Set onClickListener on LinearLayout view group with invoking showTimePickerDialog method
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTimePickerDialog(hourOfDay, minute);
                }
            });
        } else {
            ll.setVisibility(View.INVISIBLE);
            // If no reminder is required, cancel the already existing one.
            ReminderUtils.cancelReminder(SettingsActivity.this, NotificationUtils.class);
        }
    }

    // Helper method to pick a time from TimePickerDialog for reminder
    private void showTimePickerDialog(int hour, int minute) {

        TimePickerDialog builder = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int h, int m) {
                //Display the user changed time on TextView
                tv.setText(String.valueOf(h) + ":" + String.format("%02d", m));
                // Set reminder with these time values for the notification
                ReminderUtils.setReminder(SettingsActivity.this, NotificationUtils.class,
                        h, m);
            }
        }, hour, minute, true);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    // Updates the screen if the shared preferences change.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_key_notification))) {
            showNotificationScheduler();
        }
    }
}
