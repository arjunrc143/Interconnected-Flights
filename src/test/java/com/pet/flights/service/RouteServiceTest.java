package com.pet.flights.service;

import com.pet.flights.model.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    private static final String RYANAIR = "RYANAIR";
    @Mock
    private RestTemplate restTemplate;

    @Test
    public void shouldCallApiToGetRoutes() {

        Route route1 = Mockito.mock(Route.class);
        BDDMockito.given(route1.getOperator()).willReturn(RYANAIR);
        Route route2 = Mockito.mock(Route.class);
        BDDMockito.given(route2.getOperator()).willReturn(RYANAIR);

        String expectedRoutesApi = "https://dummy.url";
        BDDMockito.given(restTemplate.getForObject(expectedRoutesApi, Route[].class))
                .willReturn(new Route[]{route1, route2});

        RouteService testee = new RouteService(restTemplate, expectedRoutesApi);
        testee.getAllRoutes();

        Mockito.verify(restTemplate, Mockito.times(1)).getForObject(expectedRoutesApi, Route[].class);
    }

}