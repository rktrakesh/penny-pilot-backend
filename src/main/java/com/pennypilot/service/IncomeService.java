package com.pennypilot.service;

import com.pennypilot.dto.request.IncomeRequest;
import com.pennypilot.dto.response.IncomeResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeService {

    ResponseEntity<?> addNewIncome(IncomeRequest incomeRequest);

    ResponseEntity<?> getCurrentMonthIncomeForCurrentUser();

    ResponseEntity<?> deleteIncomeById(Long id);

    List<IncomeResponse> getLast5IncomesForCurrentUser();

    BigDecimal getTotalIncomesForCurrentUser();

}
