package com.pet.flights.service;

import com.pet.flights.Exception.TravelDateException;
import com.pet.flights.model.FlightSchedule;
import com.pet.flights.model.FlightDetails;
import com.pet.flights.model.Leg;
import com.pet.flights.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FlightDetailsService {

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
    private final RouteService routeService;
    private final ScheduleService scheduleService;

    @Autowired
    public FlightDetailsService(RouteService routeService, ScheduleService scheduleService) {
        this.routeService = routeService;
        this.scheduleService = scheduleService;
    }

    public List<FlightDetails> getFlightDetails(String departure, String arrival,
                                                String departureDateAndTime, String arrivalDateAndTime)
            throws TravelDateException {

        LocalDateTime departureDateTime = LocalDateTime.parse(departureDateAndTime, dateTimeFormatter);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDateAndTime, dateTimeFormatter);

        if (departureDateTime.isAfter(arrivalDateTime)) {
            throw new TravelDateException("Departure date is after arrival dates");
        }

        List<Route> allRoutes = routeService.getAllRoutes();

        List<FlightDetails> flightDetails =
                getDetailsOfDirectFlights(departure, arrival, departureDateTime, arrivalDateTime, allRoutes);

        List<FlightDetails> interConnectionFlightDetails =
                getDetailsOfInterConnectedFlights(departure, arrival, departureDateTime, arrivalDateTime, allRoutes);

        return Stream.of(flightDetails, interConnectionFlightDetails)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<FlightDetails> getDetailsOfInterConnectedFlights(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, List<Route> allRoutes) {
        List<Route> allRoutesFromDeparture = getAllRoutes(allRoutes,
                route -> isHavingRoutes(route.getAirportFrom(), departure));
        List<Route> allRoutesToArrival = getAllRoutes(allRoutes,
                route -> isHavingRoutes(route.getAirportTo(), arrival));

        List<String> departureLocationsOfAllRoutesToArrival =
                getFlightDetails(allRoutesToArrival, Route::getAirportFrom);

        List<Route> routesOfFirstLeg = allRoutesFromDeparture.stream()
                .filter(route -> departureLocationsOfAllRoutesToArrival.contains(route.getAirportTo()))
                .toList();

        List<Leg> possibleFirstLegs = routesOfFirstLeg.stream()
                .map(route -> getDirectLegsBetweenAirports(route, departureDateTime, arrivalDateTime))
                .flatMap(List::stream).toList();

        List<String> arrivalAirportsOfFirstLeg = getFlightDetails(routesOfFirstLeg, Route::getAirportTo);

        List<Route> routesOfSecondLeg = allRoutesToArrival.stream()
                .filter(route -> arrivalAirportsOfFirstLeg.contains(route.getAirportFrom()))
                .toList();

        List<Leg> possibleSecondLegs = routesOfSecondLeg.stream()
                .map(route -> getDirectLegsBetweenAirports(route, departureDateTime, arrivalDateTime))
                .flatMap(List::stream).toList();

        List<FlightDetails> interConnectionFlightDetails = new ArrayList<>(Collections.emptyList());

        possibleFirstLegs.forEach(firstLeg ->
                createFlightDetailsWithProperSecondLegs
                        (possibleSecondLegs, interConnectionFlightDetails, firstLeg));

        return interConnectionFlightDetails;
    }

    private void createFlightDetailsWithProperSecondLegs(List<Leg> possibleSecondLegs, List<FlightDetails> interConnectionFlightDetails, Leg firstLeg) {
        LocalDateTime arrivalDateTimeOfFirstLeg =
                LocalDateTime.parse(firstLeg.getArrivalDateTime(), dateTimeFormatter);
        LocalDateTime maximumLayoverDateTime = arrivalDateTimeOfFirstLeg.plusHours(2);
        String connectingAirport = firstLeg.getArrivalAirport();
        possibleSecondLegs.stream()
                .filter(secondLeg -> secondLeg.getDepartureAirport().equals(connectingAirport))
                .forEach(secondLeg ->
                        createFlightDetailsIfFirstLegsArrivalTimeIsBeforeSecondLegsDepartureTimeAndWithinMaximumLayoverTime
                                (interConnectionFlightDetails, firstLeg, arrivalDateTimeOfFirstLeg, maximumLayoverDateTime, secondLeg));
    }

    private void createFlightDetailsIfFirstLegsArrivalTimeIsBeforeSecondLegsDepartureTimeAndWithinMaximumLayoverTime(List<FlightDetails> interConnectionFlightDetails, Leg firstLeg, LocalDateTime arrivalDateTimeOfFirstLeg, LocalDateTime maximumLayoverDateTime, Leg secondLeg) {
        LocalDateTime departureDateTimeOfSecondLeg =
                LocalDateTime.parse(secondLeg.getDepartureDateTime(), dateTimeFormatter);
        if (arrivalDateTimeOfFirstLeg.isBefore(departureDateTimeOfSecondLeg)
                && departureDateTimeOfSecondLeg.isBefore(maximumLayoverDateTime)) {
            interConnectionFlightDetails.add(new FlightDetails(List.of(firstLeg, secondLeg)));
        }
    }

    private List<FlightDetails> getDetailsOfDirectFlights(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, List<Route> allRoutes) {
        Optional<Route> directRoutes = allRoutes.stream()
                .filter(route ->
                        route.getAirportFrom().equals(departure)
                                && route.getAirportTo().equals(arrival))
                .findFirst();

        List<FlightDetails> flightDetails = new ArrayList<>(Collections.emptyList());
        directRoutes.ifPresent(route ->
                flightDetails.addAll(getDirectFlights(departure, arrival, departureDateTime, arrivalDateTime, route)));
        return flightDetails;
    }

    private List<FlightDetails> getDirectFlights(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Route route1) {
        List<FlightSchedule> allSchedules = scheduleService.getAllSchedules(route1.getAirportFrom(),
                route1.getAirportTo(), departureDateTime, arrivalDateTime);

        List<Leg> directLegs = allSchedules.stream()
                .map(flightSchedule -> createLeg(flightSchedule, departure, arrival)).toList();

        return directLegs.stream()
                .map(leg -> new FlightDetails(List.of(leg))).collect(Collectors.toList());
    }

    private List<Leg> getDirectLegsBetweenAirports(Route route, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        String airportFrom = route.getAirportFrom();
        String airportTo = route.getAirportTo();

        List<FlightSchedule> allSchedules = scheduleService
                .getAllSchedules(airportFrom, airportTo, departureDateTime, arrivalDateTime);

        return allSchedules.stream()
                .map(flightSchedule -> createLeg(flightSchedule, airportFrom, airportTo)).toList();
    }

    private Leg createLeg(FlightSchedule flightSchedule, String departureAirport, String arrivalAirport) {
        return new Leg(departureAirport, arrivalAirport, flightSchedule.getDepartureTime(), flightSchedule.getArrivalTime());
    }

    private List<String> getFlightDetails(List<Route> routes, Function<Route, String> mappingFunction) {
        return routes.stream()
                .map(mappingFunction)
                .collect(Collectors.toList());
    }

    private static boolean isHavingRoutes(String route, String departure) {
        return route.equals(departure);
    }

    private List<Route> getAllRoutes(List<Route> allRoutes, Predicate<Route> routeSelector) {
        return allRoutes.stream()
                .filter(routeSelector)
                .collect(Collectors.toList());
    }

}
