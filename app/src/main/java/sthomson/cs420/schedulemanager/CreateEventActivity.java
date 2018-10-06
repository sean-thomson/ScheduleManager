package sthomson.cs420.schedulemanager;

import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateEventActivity extends AppCompatActivity {

    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Intent intent = getIntent();
        selectedDate = (Calendar) intent.getSerializableExtra("date");

        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy");
        Date formattedDate = selectedDate.getTime();

        try {
            formattedDate = format.parse(selectedDate.getTime().toString());
        }
        catch (java.text.ParseException e) {
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
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, eventTitle.getText().toString())
                        .putExtra(CalendarContract.Events.DESCRIPTION, eventDesc.getText().toString())
                        //.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventStart.getText())
                        //.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventEnd.getText())
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, eventLocation.getText());
                startActivity(intent);
            }
        });
    }
}
