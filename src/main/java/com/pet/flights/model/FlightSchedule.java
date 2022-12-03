package com.pet.flights.model;

import java.time.LocalDateTime;

public class FlightSchedule {
    int number;
    String departureTime;
    String arrivalTime;

    public FlightSchedule() {
    }

    public FlightSchedule(int number, String departureTime, String arrivalTime) {
        this.number = number;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public int getNumber() {
        return number;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    @Override
    public String toString() {
        return number + "," + departureTime + "," + arrivalTime;
    }
}
