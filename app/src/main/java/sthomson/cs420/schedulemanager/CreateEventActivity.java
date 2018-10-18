package sthomson.cs420.schedulemanager;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CreateEventActivity extends AppCompatActivity {

    private Date selectedDate;

    private Context context = this;
    private int eventId;
    private int hour;
    private int minutes;
    private TimePickerDialog timePicker;
    private Date selectedDateStartTime;
    private Date selectedDateEndTime;
    private boolean permission;
    private ArrayList<Event> eventList;

    private String task;
    private Event editingEvent;
    private int eventIndex;
    private ArrayList<String> chosenDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Intent intent = getIntent();
        selectedDate = (Date) intent.getSerializableExtra("date");
        permission = intent.getBooleanExtra("permission", false);
        task = intent.getStringExtra("task");
        chosenDays = new ArrayList<>();

        if (!permission) {
            Bundle b = intent.getExtras();
            eventList = (ArrayList<Event>) b.get("list");
        }

        if (task.equals("edit")) {
            task = intent.getStringExtra("task");
            editingEvent = (Event) intent.getSerializableExtra("event");
            eventIndex = intent.getIntExtra("index", 0);
            Button delete = findViewById(R.id.deleteButton);
            delete.setVisibility(View.VISIBLE);
        }

        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy");
        Date formattedDate = Calendar.getInstance().getTime();

        try {
            formattedDate = format.parse(selectedDate.toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        format = new SimpleDateFormat("MMM dd, yyyy");
        String newDate = format.format(formattedDate);

        TextView header = findViewById(R.id.createEventHeader);
        header.append(" " + newDate);

        final EditText eventTitle = findViewById(R.id.eventTitleBox);
        final EditText eventDesc = findViewById(R.id.eventDescBox);
        final EditText eventStart = findViewById(R.id.startTimeBox);
        final EditText eventEnd = findViewById(R.id.endTimeBox);
        final EditText eventLocation = findViewById(R.id.locationBox);

        final Button addEventButton = findViewById(R.id.addEventButton);

        if (task.equals("class")) {
            eventTitle.setHint("Class Name");
            eventDesc.setHint("Class Description");
            eventStart.setHint("Class Time Start");
            eventEnd.setHint("Class Time End");
            eventLocation.setHint("Class Location");

            final CheckBox monday = findViewById(R.id.mondayCheckBox);
            monday.setVisibility(View.VISIBLE);

            final CheckBox tuesday = findViewById(R.id.tuesdayCheckBox);
            tuesday.setVisibility(View.VISIBLE);

            final CheckBox wednesday = findViewById(R.id.wednesdayCheckBox);
            wednesday.setVisibility(View.VISIBLE);

            final CheckBox thursday = findViewById(R.id.thursdayCheckBox);
            thursday.setVisibility(View.VISIBLE);

            final CheckBox friday = findViewById(R.id.fridayCheckBox);
            friday.setVisibility(View.VISIBLE);

            final CheckBox saturday = findViewById(R.id.saturdayCheckBox);
            saturday.setVisibility(View.VISIBLE);

            final CheckBox sunday = findViewById(R.id.sundayCheckBox);
            sunday.setVisibility(View.VISIBLE);

            TextView daysHeader = findViewById(R.id.whatDaysText);
            daysHeader.setVisibility(View.VISIBLE);

            addEventButton.setText("Create Class");
            addEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
                    String eventStartTime = eventStart.getText().toString();
                    Date eventStartTimeDate = Calendar.getInstance().getTime();
                    try {
                        eventStartTimeDate = timeFormat.parse(eventStartTime);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    selectedDateStartTime = (Date) selectedDate.clone();
                    selectedDateStartTime.setHours(eventStartTimeDate.getHours());
                    selectedDateStartTime.setMinutes(eventStartTimeDate.getMinutes());

                    SimpleDateFormat endTimeFormat = new SimpleDateFormat("hh:mm");
                    String eventEndTime = eventEnd.getText().toString();
                    Date eventEndTimeDate = Calendar.getInstance().getTime();
                    try {
                        eventEndTimeDate = endTimeFormat.parse(eventEndTime);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    selectedDateEndTime = (Date) selectedDate.clone();
                    selectedDateEndTime.setHours(eventEndTimeDate.getHours());
                    selectedDateEndTime.setMinutes(eventEndTimeDate.getMinutes());

                    String chosenDays = "";
                    String newChosenDays = "";

                    if (monday.isChecked()) {
                        chosenDays = "MO,";
                    }
                    if (tuesday.isChecked()) {
                        chosenDays = chosenDays + "TU,";
                    }
                    if (wednesday.isChecked()) {
                        chosenDays = chosenDays + "WE,";
                    }
                    if (thursday.isChecked()) {
                        chosenDays = chosenDays + "TH,";
                    }
                    if (friday.isChecked()) {
                        chosenDays = chosenDays + "FR,";
                    }
                    if (saturday.isChecked()) {
                        chosenDays = chosenDays + "SA,";
                    }
                    if (sunday.isChecked()) {
                        chosenDays = chosenDays + "SU,";
                    }
                    if (!chosenDays.isEmpty()) {
                        newChosenDays = ";BYDAY=" + chosenDays.substring(0, chosenDays.length() - 1);
                    }

                    if (permission) {
                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.Events.TITLE, eventTitle.getText().toString())
                                .putExtra(CalendarContract.Events.DESCRIPTION, eventDesc.getText().toString())
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, selectedDateStartTime.getTime())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, selectedDateEndTime.getTime())
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, eventLocation.getText().toString())
                                .putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;UNTIL=20201010T000000Z" + newChosenDays);
                        startActivity(intent);
                    }

                    else {
                        if (eventTitle.getText().toString().isEmpty()){
                            Toast toast = Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        else {
                            Event newEvent = new Event(eventTitle.getText().toString(), eventDesc.getText().toString(), eventLocation.getText().toString(), selectedDateStartTime, selectedDateEndTime, true);
                            newEvent.setRecurringDays(chosenDays);
                            eventList.add(newEvent);
                            Intent intent = new Intent(context, MainActivity.class);
                            Bundle b = new Bundle();
                            b.putSerializable("list", eventList);
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                    }
                }
            });
        }

        else if (task.equals("edit")) {
            eventTitle.setText(editingEvent.getTitle());
            eventDesc.setText(editingEvent.getDescription());
            //eventStart.setText(editingEvent.getStartTime().getTime());
            //eventEnd.setText(editingEvent.getEndTime().getTime());
            eventLocation.setText(editingEvent.getLocation());

            addEventButton.setText("Edit Event");

            addEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editingEvent.setTitle(eventTitle.getText().toString());
                    editingEvent.setDescription(eventDesc.getText().toString());
                    editingEvent.setLocation(eventLocation.getText().toString());

                    SimpleDateFormat startTimeFormat = new SimpleDateFormat("HH:mm");
                    String eventStartTime = eventStart.getText().toString();
                    Date eventStartTimeDate = (Date) selectedDate.clone();
                    Date timeToAdd = Calendar.getInstance().getTime();
                    try {
                        timeToAdd = startTimeFormat.parse(eventStartTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    eventStartTimeDate.setHours(timeToAdd.getHours());
                    eventStartTimeDate.setMinutes(timeToAdd.getMinutes());
                    editingEvent.setStartTime(eventStartTimeDate);


                    SimpleDateFormat endTimeFormat = new SimpleDateFormat("HH:mm");
                    String eventEndTime = eventEnd.getText().toString();
                    Date eventEndTimeDate = (Date) selectedDate.clone();
                    Date timeToAddEnd = Calendar.getInstance().getTime();
                    try {
                        timeToAddEnd = endTimeFormat.parse(eventEndTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    eventEndTimeDate.setHours(timeToAddEnd.getHours());
                    eventEndTimeDate.setMinutes(timeToAddEnd.getMinutes());
                    editingEvent.setEndTime(eventEndTimeDate);


                    eventList.set(eventIndex, editingEvent);

                    Intent intent = new Intent(context, MainActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("list", eventList);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
        }

        else {
            addEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    String eventStartTime = eventStart.getText().toString();
                    Date eventStartTimeDate = (Date) selectedDate.clone();
                    try {
                        eventStartTimeDate = timeFormat.parse(eventStartTime);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    selectedDateStartTime = (Date) selectedDate.clone();
                    selectedDateStartTime.setHours(eventStartTimeDate.getHours());
                    selectedDateStartTime.setMinutes(eventStartTimeDate.getMinutes());

                    SimpleDateFormat endTimeFormat = new SimpleDateFormat("HH:mm");
                    String eventEndTime = eventEnd.getText().toString();
                    Date eventEndTimeDate = (Date) selectedDate.clone();
                    try {
                        eventEndTimeDate = endTimeFormat.parse(eventEndTime);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    selectedDateEndTime = (Date) selectedDate.clone();
                    selectedDateEndTime.setHours(eventEndTimeDate.getHours());
                    selectedDateEndTime.setMinutes(eventEndTimeDate.getMinutes());

                    if (permission) {
                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, selectedDateStartTime.getTime())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, selectedDateEndTime.getTime())
                                .putExtra(CalendarContract.Events.TITLE, eventTitle.getText().toString())
                                .putExtra(CalendarContract.Events.DESCRIPTION, eventDesc.getText().toString())
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, eventLocation.getText().toString());
                        startActivity(intent);
                    } else {
                        if (eventTitle.getText().toString().isEmpty()) {
                            Toast toast = Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            Event newEvent = new Event(eventTitle.getText().toString(), eventDesc.getText().toString(), eventLocation.getText().toString(), selectedDateStartTime, selectedDateEndTime, false);
                            eventList.add(newEvent);
                            Intent intent = new Intent(context, MainActivity.class);
                            Bundle b = new Bundle();
                            b.putSerializable("list", eventList);
                            intent.putExtras(b);
                            startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    public void showTimePickerDialog(View v) {
        final EditText curEditText = (EditText) v;
        Calendar cal = Calendar.getInstance();
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        int curMin = cal.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                hour = hourOfDay;
                minutes = minute;
                EditText editText = curEditText;
                editText.setText(hour + ":" + minute);
            }};

        new TimePickerDialog(this, onStartTimeListener, curHour, curMin, DateFormat.is24HourFormat(this)).show();
    }

    public void deleteEvent(View view) {
        eventList.remove(eventIndex);
        Intent intent = new Intent(context, MainActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("list", eventList);
        intent.putExtras(b);
        startActivity(intent);
    }
}
