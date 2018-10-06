package sthomson.cs420.schedulemanager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
        }

        final CalendarView calendarView = findViewById(R.id.mainCalendar);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day) {
                selectedDate = new Calendar.Builder().setDate(year, month, day).build();
                Calendar startTime = new Calendar.Builder().setDate(year, month, day).setTimeOfDay(0, 0, 0).build();
                Calendar endTime = new Calendar.Builder().setDate(year, month, day).setTimeOfDay(23, 59, 59).build();

                Cursor cur = null;
                ContentResolver cr = getContentResolver();

                String[] projection = {
                        "_id",
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND
                };

                Uri uri = CalendarContract.Events.CONTENT_URI;

                String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() +
                        " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";
                String[] selectionArgs = new String[]{};

                cur = cr.query(uri, projection, selection, selectionArgs, null);


                if (cur.getCount() == 0) {
                    TextView textView = findViewById(R.id.todaysEventsView);
                    textView.setText("No events scheduled on " + (month+1) + "/" + day + "/" + year);
                }

                while (cur.moveToNext()) {
                    String title = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
                    TextView textView = findViewById(R.id.todaysEventsView);
                    textView.setText(title);
                }
            }
        });

        final Button createEventButton = findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI);
                startActivity(intent);
                */

                startCreateEvent();
            }
        });
    }

    protected void startCreateEvent() {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("date", selectedDate);
        startActivity(intent);
    }
}
