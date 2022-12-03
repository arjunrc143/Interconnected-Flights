package com.pet.flights.service;

import com.pet.flights.model.DaySchedule;
import com.pet.flights.model.FlightSchedule;
import com.pet.flights.model.Schedule;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void shouldCallApiToGetSchedules() {

        Schedule schedule = Mockito.mock(Schedule.class);
        String dummyDeparture = "dummyDeparture";
        String dummyArrival = "dummyArrival";
        String departureDateTime = "2023-03-01T07:00";
        String arrivalDateTime = "2023-03-03T07:00";
        LocalDateTime departureDateTimeld = LocalDateTime.parse(departureDateTime, dateTimeFormatter);
        LocalDateTime arrivalDateTimeld = LocalDateTime.parse(arrivalDateTime, dateTimeFormatter);

        String expectedSchedulesApi = "https://dummy.url/";
        String expectedUrl = expectedSchedulesApi + "dummyDeparture/dummyArrival/years/2023/months/3";

        BDDMockito.given(restTemplate.getForObject(expectedUrl, Schedule.class))
                .willReturn(schedule);

        ScheduleService testee = new ScheduleService(restTemplate, expectedSchedulesApi);
        testee.getAllSchedules(dummyDeparture, dummyArrival, departureDateTimeld, arrivalDateTimeld);

        Mockito.verify(restTemplate, Mockito.times(1))
                .getForObject(expectedUrl, Schedule.class);

    }

    @Test
    public void shouldGetProperSchedules() {

        String expectedDepartureTime1 = "2023-03-02T16:00";
        String expectedArrivalTime1 = "2023-03-02T18:00";
        String expectedDepartureTime2 = "2023-03-02T12:00";
        String expectedArrivalTime2 = "2023-03-02T13:00";
        String dummyDepartureTime1 = "16:00";
        String dummyArrivalTime1 = "18:00";
        String dummyDepartureTime2 = "12:00";
        String dummyArrivalTime2 = "13:00";
        FlightSchedule expectedFlightSchedule1 = getDummyFlightSchedule(dummyDepartureTime1, dummyArrivalTime1);
        FlightSchedule expectedFlightSchedule2 = getDummyFlightSchedule(dummyDepartureTime2, dummyArrivalTime2);
        List<FlightSchedule> dummyFlightSchedules = List.of(expectedFlightSchedule1, expectedFlightSchedule2);
        Schedule dummySchedule = getDummySchedule(dummyFlightSchedules);
        String dummyDeparture = "dummyDeparture";
        String dummyArrival = "dummyArrival";
        String departureDateTime = "2023-03-01T07:00";
        String arrivalDateTime = "2023-03-03T07:00";
        LocalDateTime departureDateTimeld = LocalDateTime.parse(departureDateTime, dateTimeFormatter);
        LocalDateTime arrivalDateTimeld = LocalDateTime.parse(arrivalDateTime, dateTimeFormatter);

        String expectedSchedulesApi = "https://dummy.url/";
        String expectedUrl = expectedSchedulesApi + "dummyDeparture/dummyArrival/years/2023/months/3";

        BDDMockito.given(restTemplate.getForObject(expectedUrl, Schedule.class))
                .willReturn(dummySchedule);

        ScheduleService testee = new ScheduleService(restTemplate, expectedSchedulesApi);
        List<FlightSchedule> actualSchedules =
                testee.getAllSchedules(dummyDeparture, dummyArrival, departureDateTimeld, arrivalDateTimeld);

        BDDAssertions.then(actualSchedules).hasSize(2);
        FlightSchedule actualFlightSchedule1 = actualSchedules.get(0);
        FlightSchedule actualFlightSchedule2 = actualSchedules.get(1);

        BDDAssertions.then(actualFlightSchedule1.getNumber())
                .isEqualTo(expectedFlightSchedule1.getNumber());

        BDDAssertions.then(actualFlightSchedule2.getNumber())
                .isEqualTo(expectedFlightSchedule2.getNumber());

        BDDAssertions.then(actualFlightSchedule1.getDepartureTime())
                .isEqualTo(expectedDepartureTime1);

        BDDAssertions.then(actualFlightSchedule1.getArrivalTime())
                .isEqualTo(expectedArrivalTime1);

        BDDAssertions.then(actualFlightSchedule2.getDepartureTime())
                .isEqualTo(expectedDepartureTime2);

        BDDAssertions.then(actualFlightSchedule2.getArrivalTime())
                .isEqualTo(expectedArrivalTime2);

    }

    private Schedule getDummySchedule(List<FlightSchedule> flightSchedules) {
        DaySchedule daySchedule = new DaySchedule(2, flightSchedules);
        return new Schedule(3, List.of(daySchedule));
    }

    private FlightSchedule getDummyFlightSchedule(String departureTime, String arrivalTime) {
        return new FlightSchedule(1, departureTime, arrivalTime);
    }

}