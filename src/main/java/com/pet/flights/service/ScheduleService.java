package com.pet.flights.service;

import com.pet.flights.model.DaySchedule;
import com.pet.flights.model.FlightSchedule;
import com.pet.flights.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ScheduleService {
    private static final int MIN_POSSIBLE_DAY = 1;
    private static final int MAX_POSSIBLE_DAY = 31;
    private final RestTemplate restTemplate;

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

    private final String schedulesApiBaseUrl;

    @Autowired
    public ScheduleService(RestTemplate restTemplate,@Value("${schedules.api}") String schedulesApiBaseUrl) {
        this.restTemplate = restTemplate;
        this.schedulesApiBaseUrl = schedulesApiBaseUrl;
    }

    public List<FlightSchedule> getAllSchedules(String departure, String arrival,
                                                LocalDateTime departureDate, LocalDateTime arrivalDate) {

        int departingYear = departureDate.getYear();
        int arrivingYear = arrivalDate.getYear();

        if (departingYear == arrivingYear) {
            return getFlightSchedulesWithinTheSameYear(departure, arrival, departureDate, arrivalDate, departingYear);
        } else {
            return getFlightsFromDepartingYearArrivingYearAndYearsInBetween(departure, arrival, departureDate, arrivalDate, departingYear, arrivingYear);
        }
    }

    private List<FlightSchedule> getFlightsFromDepartingYearArrivingYearAndYearsInBetween(String departure, String arrival, LocalDateTime departureDate, LocalDateTime arrivalDate, int departingYear, int arrivingYear) {
        List<FlightSchedule> validFlightsInDepartingYear = getFlightSchedulesWithinTheSameYear(departure, arrival,
                departureDate, departureDate.with(TemporalAdjusters.lastDayOfYear()), departingYear);
        List<FlightSchedule> validFlightsInArrivingYear = getFlightSchedulesWithinTheSameYear(departure, arrival,
                arrivalDate.with(TemporalAdjusters.firstDayOfYear()), arrivalDate, arrivingYear);

        List<Integer> years = IntStream.range(departingYear + 1, arrivingYear - 1)
                .boxed().toList();
        List<FlightSchedule> validFlightSchedulesBetweenYears = new ArrayList<>(Collections.emptyList());
        if(years.size() > 0) {
            validFlightSchedulesBetweenYears.addAll(years.stream()
                    .map(year -> LocalDateTime.of(year, 1, 1, 0, 0))
                    .map(firstDayOfYear ->
                            getFlightSchedulesWithinTheSameYear(departure, arrival, firstDayOfYear,
                                    firstDayOfYear.with(TemporalAdjusters.lastDayOfYear()), firstDayOfYear.getYear()))
                    .flatMap(List::stream).toList());

        }
        return Stream.of(validFlightsInDepartingYear, validFlightsInArrivingYear, validFlightSchedulesBetweenYears)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<FlightSchedule> getFlightSchedulesWithinTheSameYear(String departure, String arrival, LocalDateTime departureDate, LocalDateTime arrivalDate, int year) {

        Month departingMonth = departureDate.getMonth();
        Month arrivingMonth = arrivalDate.getMonth();

        int departingDay = departureDate.getDayOfMonth();
        int arrivingDay = arrivalDate.getDayOfMonth();

        if (departingMonth == arrivingMonth) {
            return getValidFlightSchedulesForDatesWithinMonth(departure, arrival, year, departingMonth, departingDay, arrivingDay);
        } else {
            List<FlightSchedule> validFlightSchedulesForDepartingMonth =
                    getValidFlightSchedulesForDatesWithinMonth(departure, arrival,
                            year, departingMonth, departingDay, departureDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth());
            List<FlightSchedule> validFlightSchedulesForArrivalMonth =
                    getValidFlightSchedulesForDatesWithinMonth(departure, arrival,
                            year, arrivingMonth, arrivalDate.with(TemporalAdjusters.firstDayOfMonth()).getDayOfMonth(), arrivingDay);

            List<Integer> months = IntStream.range(departingMonth.getValue() + 1, arrivingMonth.getValue() - 1)
                    .boxed().toList();
            List<FlightSchedule> validFlightSchedulesBetweenMonths = new ArrayList<>(Collections.emptyList());
            if(months.size() > 0) {
                validFlightSchedulesBetweenMonths.addAll(months.stream()
                        .map(month ->
                                getValidFlightSchedulesForDatesWithinMonth(departure, arrival, year,
                                        Month.of(month), MIN_POSSIBLE_DAY, MAX_POSSIBLE_DAY))
                        .flatMap(List::stream).toList());
            }

            return Stream.of(validFlightSchedulesForDepartingMonth, validFlightSchedulesForArrivalMonth, validFlightSchedulesBetweenMonths)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private List<FlightSchedule> getValidFlightSchedulesForDatesWithinMonth(String departure, String arrival, int year, Month month, int fromDay, int toDay) {
        Schedule schedulesForMonth = getSchedules(departure, arrival, year, month);
        List<Integer> days = IntStream.range(fromDay, toDay)
                .boxed().toList();
        return schedulesForMonth.getDays()
                .stream()
                .filter(daySchedule -> days.contains(daySchedule.getDay()))
                .map(daySchedule -> buildFlightDtoList(daySchedule, month, year))
                .flatMap(List::stream).toList();
    }

    private List<FlightSchedule> buildFlightDtoList(DaySchedule daySchedule, Month month, int year) {
        return daySchedule.getFlights().stream()
                .map(schedule -> buildFlightDto(schedule, month, year, daySchedule.getDay()))
                .collect(Collectors.toList());
    }

    private FlightSchedule buildFlightDto(FlightSchedule schedule, Month month, int year, int day) {
        String[] departureTime = schedule.getDepartureTime().split(":");
        int departingHour = Integer.parseInt(departureTime[0]);
        int departingMinute = Integer.parseInt(departureTime[1]);

        String[] arrivalTime = schedule.getArrivalTime().split(":");
        int arrivingHour = Integer.parseInt(arrivalTime[0]);
        int arrivingMinute = Integer.parseInt(arrivalTime[1]);


        LocalDateTime departureDateTime = LocalDateTime.of(year, month, day, departingHour, departingMinute);
        LocalDateTime arrivalDateTime = LocalDateTime.of(year, month, day, arrivingHour, arrivingMinute);


        return new FlightSchedule(schedule.getNumber(), departureDateTime.format(dateTimeFormatter),
                arrivalDateTime.format(dateTimeFormatter));
    }

    private Schedule getSchedules(String departure, String arrival, int departingYear, Month departingMonth) {
        String url = schedulesApiBaseUrl + departure + "/" + arrival
                + "/years/" + departingYear + "/months/" + departingMonth.getValue();

        return restTemplate.getForObject(url, Schedule.class);
    }

}
