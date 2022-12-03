package com.pet.flights.model;

import java.util.List;

public class FlightDetails {
    int stops;
    List<Leg> legs;

    public FlightDetails(List<Leg> legs) {
        this.legs = legs;
        stops = legs.size()-1;
    }

    public int getStops() {
        return stops;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    @Override
    public String toString() {
        return stops + "," + legs.toString();
    }
}
