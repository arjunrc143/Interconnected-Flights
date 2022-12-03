package com.pet.flights.service;

import com.pet.flights.Exception.TravelDateException;
import com.pet.flights.model.FlightDetails;
import com.pet.flights.model.FlightSchedule;
import com.pet.flights.model.Leg;
import com.pet.flights.model.Route;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FlightDetailsServiceTest {

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private FlightDetailsService testee;

    @Test
    void shouldGetDirectFlightDetails() throws TravelDateException {

        String departure = "DUB";
        String arrival = "WRO";
        String departureDateTime = "2023-03-01T07:00";
        String arrivalDateTime = "2023-03-01T07:00";

        String expectedDepartureTime1 = "2023-03-01T16:00";
        String expectedArrivalTime1 = "2023-03-01T18:00";
        String expectedDepartureTime2 = "2023-03-01T12:00";
        String expectedArrivalTime2 = "2023-03-01T13:00";


        Leg expectedDirectLeg1 = new Leg(departure, arrival, expectedDepartureTime1, expectedArrivalTime1);
        Leg expectedDirectLeg2 = new Leg(departure, arrival, expectedDepartureTime2, expectedArrivalTime2);
        FlightDetails expectedDirectFlight1 = new FlightDetails(List.of(expectedDirectLeg1));
        FlightDetails expectedDirectFlight2 = new FlightDetails(List.of(expectedDirectLeg2));

        BDDMockito.given(routeService.getAllRoutes())
                .willReturn(getDummyRoutes());
        List<FlightSchedule> dummyFlightSchedules = List.of(
                getDummyFlightSchedule(expectedDepartureTime1, expectedArrivalTime1),
                getDummyFlightSchedule(expectedDepartureTime2, expectedArrivalTime2));
        BDDMockito.given(scheduleService.getAllSchedules(anyString(), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(dummyFlightSchedules);

        List<FlightDetails> actualFlightDetails =
                testee.getFlightDetails(departure, arrival, departureDateTime, arrivalDateTime);

        BDDAssertions.then(actualFlightDetails).hasSize(2);
        FlightDetails actualFlight1 = actualFlightDetails.get(0);
        FlightDetails actualFlight2 = actualFlightDetails.get(1);

        BDDAssertions.then(actualFlight1.toString()).isEqualTo(expectedDirectFlight1.toString());
        BDDAssertions.then(actualFlight2.toString()).isEqualTo(expectedDirectFlight2.toString());
    }

    @Test
    void shouldThrowExceptionIfDepartureDateIsAfterArrivalDate() {
        String departure = "DUB";
        String arrival = "WRO";
        String departureDateTime = "2023-03-01T07:00";
        String arrivalDateTime = "2020-03-01T07:00";

        String expectedMessage = "Departure date is after arrival dates";

        TravelDateException travelDateException = Assertions.assertThrows(TravelDateException.class,
                () -> testee.getFlightDetails(departure, arrival, departureDateTime, arrivalDateTime));

        BDDAssertions.then(travelDateException.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void shouldGetInterConnectedFlightDetails() throws TravelDateException {

        String departure1 = "DUB";
        String arrival1 = "STN";
        String departure2 = "STN";
        String arrival2 = "WRO";
        String departureDateTime = "2023-03-01T07:00";
        String arrivalDateTime = "2023-03-03T07:00";

        String expectedDepartureTime1 = "2023-03-02T16:00";
        String expectedArrivalTime1 = "2023-03-02T18:00";
        String expectedDepartureTime2 = "2023-03-02T19:00";
        String expectedArrivalTime2 = "2023-03-02T21:00";


        Leg expectedLeg1 = new Leg(departure1, arrival1, expectedDepartureTime1, expectedArrivalTime1);
        Leg expectedLeg2 = new Leg(departure2, arrival2, expectedDepartureTime2, expectedArrivalTime2);
        FlightDetails expectedDirectFlight1 = new FlightDetails(List.of(expectedLeg1, expectedLeg2));

        BDDMockito.given(routeService.getAllRoutes())
                .willReturn(getDummyRoutes());
        List<FlightSchedule> dummyFlightSchedule1 = List.of(
                getDummyFlightSchedule(expectedDepartureTime1, expectedArrivalTime1));
        List<FlightSchedule> dummyFlightSchedule2 = List.of(
                getDummyFlightSchedule(expectedDepartureTime2, expectedArrivalTime2));

        lenient().when(scheduleService.getAllSchedules(departure1, arrival1,
                LocalDateTime.parse(departureDateTime, dateTimeFormatter),
                LocalDateTime.parse(arrivalDateTime, dateTimeFormatter))).thenReturn(dummyFlightSchedule1);

        lenient().when(scheduleService.getAllSchedules(departure2, arrival2,
                LocalDateTime.parse(departureDateTime, dateTimeFormatter),
                LocalDateTime.parse(arrivalDateTime, dateTimeFormatter))).thenReturn(dummyFlightSchedule2);

        List<FlightDetails> actualFlightDetails =
                testee.getFlightDetails(departure1, arrival2, departureDateTime, arrivalDateTime);

        BDDAssertions.then(actualFlightDetails).hasSize(1);
        FlightDetails actualFlight1 = actualFlightDetails.get(0);

        BDDAssertions.then(actualFlight1.toString()).isEqualTo(expectedDirectFlight1.toString());
    }

    private List<Route> getDummyRoutes() {
        Route route1 = new Route("DUB", "WRO",
                null, true, true, "RYANAIR", "dummyGroup");
        Route route2 = new Route("STN", "WRO",
                null, true, true, "RYANAIR", "dummyGroup");
        Route route3 = new Route("DUB", "STN",
                null, true, true, "RYANAIR", "dummyGroup");
        Route route4 = new Route("DUB", "COK",
                null, true, true, "RYANAIR", "dummyGroup");
        Route route5 = new Route("BER", "PRG",
                null, true, true, "RYANAIR", "dummyGroup");
        return List.of(route1, route2, route3, route4, route5);
    }

    private FlightSchedule getDummyFlightSchedule(String departureTime, String arrivalTime) {
        return new FlightSchedule(1, departureTime, arrivalTime);
    }


}