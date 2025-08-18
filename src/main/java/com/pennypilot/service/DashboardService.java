package com.pennypilot.service;

import org.springframework.http.ResponseEntity;

public interface DashboardService {

    ResponseEntity<?> getDashboardDataForCurrentUser();

}
