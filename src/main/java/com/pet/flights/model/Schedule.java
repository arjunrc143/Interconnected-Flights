package com.pet.flights.model;

import java.util.List;

public class Schedule {
    int month;
    List<DaySchedule> days;

    public Schedule() {
    }

    public Schedule(int month, List<DaySchedule> dayScheduleList) {
        this.month = month;
        this.days = dayScheduleList;
    }

    public int getMonth() {
        return month;
    }

    public List<DaySchedule> getDays() {
        return days;
    }
}
