package com.pet.flights.service;

import com.pet.flights.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class RouteService {

    private static final String RYANAIR = "RYANAIR";
    private final RestTemplate restTemplate;
    private final String routesApi;

    @Autowired
    public RouteService(RestTemplate restTemplate, @Value("${routes.api}") String routesApi) {
        this.restTemplate = restTemplate;
        this.routesApi = routesApi;
    }

    public List<Route> getAllRoutes() {
        return Stream.of(
                        Objects.requireNonNull(
                                restTemplate.getForObject(routesApi, Route[].class)))
                .filter(route -> null == route.getConnectingAirport())
                .filter(route -> route.getOperator().equals(RYANAIR))
                .toList();
    }

}
