package com.pet.flights.model;

public class Leg {

    String departureAirport;
    String arrivalAirport;
    String departureDateTime;
    String arrivalDateTime;

    public Leg(String departureAirport, String arrivalAirport, String departureDateTime, String arrivalDateTime) {
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public String getDepartureDateTime() {
        return departureDateTime;
    }

    public String getArrivalDateTime() {
        return arrivalDateTime;
    }

    @Override
    public String toString() {
        return departureAirport + "," + arrivalAirport+ "," + departureDateTime + ","  + arrivalDateTime;
    }
}
