package com.pet.flights.model;

import java.util.List;

public class DaySchedule {
    int day;
    List<FlightSchedule> flights;

    public DaySchedule() {
    }

    public DaySchedule(int day, List<FlightSchedule> flights) {
        this.day = day;
        this.flights = flights;
    }

    public int getDay() {
        return day;
    }

    public List<FlightSchedule> getFlights() {
        return flights;
    }
}
