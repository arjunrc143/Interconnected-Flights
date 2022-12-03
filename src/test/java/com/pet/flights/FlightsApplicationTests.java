package com.pet.flights;

import com.pet.flights.model.FlightSchedule;
import com.pet.flights.model.Route;
import com.pet.flights.service.FlightDetailsService;
import com.pet.flights.service.RouteService;
import com.pet.flights.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class FlightsApplicationTests {
	@Test
	void contextLoads() {
	}
}
