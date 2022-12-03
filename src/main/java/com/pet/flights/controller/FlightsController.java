package com.pet.flights.controller;

import com.pet.flights.Exception.TravelDateException;
import com.pet.flights.model.FlightDetails;
import com.pet.flights.service.FlightDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/flights")
public class FlightsController {

    private final FlightDetailsService flightDetailsService;

    @Autowired
    public FlightsController(FlightDetailsService flightDetailsService) {
        this.flightDetailsService = flightDetailsService;
    }

    @GetMapping("/interconnections")
    @ResponseStatus(HttpStatus.OK)
    public List<FlightDetails> getFlightDetails(@RequestParam(name = "departure") String departure,
                                                @RequestParam(name = "arrival") String arrival,
                                                @RequestParam(name = "departureDateTime") String departureDateTime,
                                                @RequestParam(name = "arrivalDateTime") String arrivalDateTime)
            throws TravelDateException {
        return flightDetailsService.getFlightDetails(departure, arrival, departureDateTime, arrivalDateTime);
    }

}
