package sthomson.cs420.schedulemanager;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable{
    private String title;
    private String description;
    private String location;
    private Date startTime;
    private Date endTime;
    private boolean isClass;
    private String recurringDays;

    public Event(String newTitle, String newDesc, String newLoc, Date newStart, Date newEnd, boolean classBool) {
        title = newTitle;
        description = newDesc;
        location = newLoc;
        startTime = newStart;
        endTime = newEnd;
        isClass = classBool;
        recurringDays = "";
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public void setDescription(String newDesc) {
        description = newDesc;
    }

    public void setLocation(String newLoc) {
        location = newLoc;
    }

    public void setStartTime(Date newStart) {
        startTime = newStart;
    }

    public void setEndTime(Date newEnd) {
        endTime = newEnd;
    }

    public void setRecurringDays(String recur) {
        recur = recur.replace("SU", "0");
        recur = recur.replace("MO", "1");
        recur = recur.replace("TU", "2");
        recur = recur.replace("WE", "3");
        recur = recur.replace("TH", "4");
        recur = recur.replace("FR", "5");
        recur = recur.replace("SA", "6");

        System.out.println(recur);

        recurringDays = recur;
    }

    public String getRecurringDays() {
        return recurringDays;
    }
}
