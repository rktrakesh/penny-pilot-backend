package com.pennypilot.service;

import com.pennypilot.dto.FilterDto;
import org.springframework.http.ResponseEntity;

public interface FilterService {

    ResponseEntity<?> filterTransactions (FilterDto filterDto);

}
