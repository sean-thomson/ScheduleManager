package sthomson.cs420.schedulemanager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    private Date selectedDate;
    private WebView webView;
    private Context mainActivityContext;
    private boolean certificate = false;
    private ArrayList<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityContext = this;

        selectedDate = Calendar.getInstance().getTime();

        webView = findViewById(R.id.mainWebView);

        try {
            FileInputStream fis = openFileInput("events");
            ObjectInputStream ois = new ObjectInputStream(fis);
            eventList = (ArrayList<Event>) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            //Do nothing
            System.out.println("FILE NOT FOUND");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("IOEXCEPTION");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("CLASS NOT FOUND");
        }

        if (eventList == null) {
            eventList = new ArrayList<>();
        }

        Exception webException = null;
        try {
            webException = new WebRequester().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (webException == null) {
            certificate = true;
        }
        else {
            Toast toast = Toast.makeText(this, "Blackboard's certificate could not be verified", Toast.LENGTH_SHORT);
            toast.show();
            Toast toast2 = Toast.makeText(this, "Disabling Blackboard access", Toast.LENGTH_SHORT);
            toast2.show();
            Button blackboard = findViewById(R.id.viewBlackboardButton);
            certificate = false;
            blackboard.setVisibility(View.INVISIBLE);
        }

        if (this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
        }

        final TextView eventText = findViewById(R.id.todaysEventsView);
        eventText.setMovementMethod(new ScrollingMovementMethod());

        final CalendarView calendarView = findViewById(R.id.mainCalendar);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day) {
                if (mainActivityContext.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_GRANTED) {
                    selectedDate = new Calendar.Builder().setDate(year, month, day).build().getTime();
                    Calendar startTime = new Calendar.Builder().setDate(year, month, day).setTimeOfDay(0, 0, 0).build();
                    Calendar endTime = new Calendar.Builder().setDate(year, month, day).setTimeOfDay(23, 59, 59).build();

                    Cursor cur = null;
                    ContentResolver cr = getContentResolver();

                    String[] projection = {
                            "_id",
                            CalendarContract.Events.TITLE,
                            CalendarContract.Events.EVENT_LOCATION,
                            CalendarContract.Events.DTSTART,
                            CalendarContract.Events.DURATION,
                            CalendarContract.Events.DTEND,
                            CalendarContract.Events._ID

                    };

                    Uri uri = CalendarContract.Events.CONTENT_URI;

                    String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() +
                            " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";
                    String[] selectionArgs = new String[]{};

                    cur = cr.query(uri, projection, selection, selectionArgs, null);

                    LinearLayout linearLayout = findViewById(R.id.todaysEvents);
                    linearLayout.removeAllViews();


                    if (cur.getCount() == 0) {
                        TextView textView = new TextView(mainActivityContext);
                        textView.setText("No events scheduled on " + (month + 1) + "/" + day + "/" + year);
                        linearLayout.addView(textView);
                    }

                    while (cur.moveToNext()) {
                        String title = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
                        String loc = cur.getString(cur.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
                        if (loc.isEmpty()) {
                            loc = "None";
                        }

                        String timeBeg = cur.getString(cur.getColumnIndex(CalendarContract.Events.DTSTART));
                        Long longTimeBeg = Long.parseLong(timeBeg);
                        Date date = new Date();
                        date.setTime(longTimeBeg);
                        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        String formattedBegTime = timeFormat.format(date);

                        String timeEnd = cur.getString(cur.getColumnIndex(CalendarContract.Events.DTEND));

                        String formattedEndTime = "";
                        if (timeEnd != null) {
                            long longTimeEnd = Long.parseLong(timeEnd);
                            Date endDate = new Date();
                            endDate.setTime(longTimeEnd);
                            DateFormat endTimeFormat = new SimpleDateFormat("HH:mm");
                            formattedEndTime = endTimeFormat.format(endDate);
                        }
                        else {
                            String durationEnd = cur.getString(cur.getColumnIndex(CalendarContract.Events.DURATION));
                            String newDurEnd = durationEnd.replaceAll("\\D+", "");
                            int longDur = Integer.parseInt(newDurEnd);

                            Date endDate = new Date();
                            Date tempDate = (Date) date.clone();
                            tempDate.setSeconds(longDur);
                            endDate.setTime(tempDate.getTime());
                            DateFormat endTimeFormat = new SimpleDateFormat("HH:mm");
                            formattedEndTime = endTimeFormat.format(endDate);
                        }

                        TextView textView = new TextView(mainActivityContext);
                        String id = cur.getString(cur.getColumnIndex(CalendarContract.Events._ID));
                        textView.setId(Integer.parseInt(id));
                        textView.setText(title + ": " + formattedBegTime + " - " + formattedEndTime + "\n" + "Location: " + loc + "\n\n");
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, view.getId());
                                Intent intent = new Intent(Intent.ACTION_VIEW)
                                        .setData(uri);
                                startActivity(intent);
                            }
                        });
                        linearLayout.addView(textView);
                    }
                }

                else {
                    LinearLayout linearLayout = findViewById(R.id.todaysEvents);
                    linearLayout.removeAllViews();

                    if (eventList != null) {
                        selectedDate = new Calendar.Builder().setDate(year, month, day).build().getTime();
                        int dayOfWeek = selectedDate.getDay();
                        System.out.println("day of week: " + dayOfWeek);
                        String strDay = Integer.toString(dayOfWeek);

                        int viewCount = 0;

                        for (int i = 0; i < eventList.size(); i++) {
                            if (eventList.get(i).getRecurringDays().contains(strDay) || (eventList.get(i).getStartTime().getYear() == selectedDate.getYear() && eventList.get(i).getStartTime().getDate() == selectedDate.getDate() && eventList.get(i).getStartTime().getMonth() == selectedDate.getMonth())) {
                                final int[] count = {i};

                                String title = eventList.get(i).getTitle();
                                String loc = eventList.get(i).getLocation();

                                Date timeBeg = eventList.get(i).getStartTime();
                                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                String formattedBegTime = timeFormat.format(timeBeg);

                                Date timeEnd = eventList.get(i).getEndTime();
                                DateFormat endTimeFormat = new SimpleDateFormat("HH:mm");
                                String formattedEndTime = endTimeFormat.format(timeEnd);

                                TextView textView = new TextView(mainActivityContext);
                                textView.setText(title + ": " + formattedBegTime + " - " + formattedEndTime + "\n" + "Location: " + loc + "\n\n");
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(mainActivityContext, CreateEventActivity.class);
                                        intent.putExtra("task", "edit");
                                        intent.putExtra("event", eventList.get(count[0]));
                                        intent.putExtra("index", count[0]);
                                        intent.putExtra("date", selectedDate);
                                        intent.putExtra("permission", false);

                                        Bundle b = new Bundle();
                                        b.putSerializable("list", eventList);
                                        intent.putExtras(b);

                                        startActivity(intent);
                                    }
                                });
                                linearLayout.addView(textView);
                                viewCount++;
                            }
                        }

                        if (viewCount == 0) {
                            TextView textView = new TextView(mainActivityContext);
                            textView.setText("No events scheduled on " + (month + 1) + "/" + day + "/" + year);
                            linearLayout.addView(textView);
                        }
                    }

                    else {
                        selectedDate = new Calendar.Builder().setDate(year, month, day).build().getTime();
                        TextView textView = new TextView(mainActivityContext);
                        textView.setText("No events scheduled on " + (month + 1) + "/" + day + "/" + year);
                        linearLayout.addView(textView);
                    }
                }
            }
        });

        final Button createEventButton = findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCreateEvent();
            }
        });

        final Button viewBlackboardButton = findViewById(R.id.viewBlackboardButton);
        viewBlackboardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                webView = findViewById(R.id.mainWebView);
                webView.setVisibility(View.VISIBLE);

                webView.setWebViewClient(new WebViewClient());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                if (certificate) {
                    webView.loadUrl("https://blackboard.wm.edu");
                }
                LinearLayout layout = findViewById(R.id.todaysEvents);
                layout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();

        if (intent.hasExtra("list")) {
            Bundle b = intent.getExtras();
            eventList = (ArrayList<Event>) b.get("list");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.deleteFile("events");

        FileOutputStream fos = null;

        try {
            fos = openFileOutput("events", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FILE NOT FOUND IN WRITE");
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(eventList);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOEXCEPTION WHILE WRITING");
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.isFocused() && webView.canGoBack() && webView.getVisibility() == View.VISIBLE) {
            webView.goBack();
        }
        else if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.INVISIBLE);
            LinearLayout layout = findViewById(R.id.todaysEvents);
            layout.setVisibility(View.VISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    protected void startCreateEvent() {
        Intent intent = new Intent(this, CreateEventActivity.class);
        Bundle b = new Bundle();
        intent.putExtra("date", selectedDate);
        intent.putExtra("task", "create");
        intent.putExtra("permission", this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_GRANTED);
        System.out.println("PERMISSION: " + (this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_GRANTED));
        System.out.println("PERMISSION DENIED?: " + (this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_DENIED));
        if (this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_DENIED) {
            if (eventList == null) {
                eventList = new ArrayList<>();
            }
            b.putSerializable("list", eventList);
            System.out.println(eventList);
            intent.putExtras(b);
        }
        startActivity(intent);
    }

    public void startCreateClass(View v) {
        Intent intent = new Intent(this, CreateEventActivity.class);
        Bundle b = new Bundle();
        intent.putExtra("date", selectedDate);
        intent.putExtra("task", "class");
        intent.putExtra("permission", this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_GRANTED);
        if (this.checkCallingOrSelfPermission("Manifest.permission.READ_CALENDAR") == PackageManager.PERMISSION_DENIED) {
            if (eventList == null) {
                eventList = new ArrayList<>();
            }
            b.putSerializable("list", eventList);
            intent.putExtras(b);
        }
        startActivity(intent);
    }
}

class WebRequester extends AsyncTask <Context, Void, Exception> {
    protected Exception doInBackground(Context... context) {
        URL url = null;
        try {
            url = new URL("https://blackboard.wm.edu");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e;
        }

        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return e;
        }

        InputStream in = null;
        try {
            in = urlConnection.getInputStream();
        } catch (SSLException e) {
            e.printStackTrace();
            return e;
        } catch (IOException e) {
            e.printStackTrace();
            return e;
        }

        System.out.println(in);
        return null;
    }

    protected void onPostExecute(Exception result) {

    }
}