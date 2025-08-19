package com.pennypilot.service;

import com.pennypilot.dto.request.ExpenseRequest;
import com.pennypilot.dto.response.ExpenseResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseService {

    ResponseEntity<?> addNewExpense(ExpenseRequest expenseRequest);

    ResponseEntity<?> getCurrentMonthExpenseForCurrentUser();

    ResponseEntity<?> deleteExpenseById(Long id);

    List<ExpenseResponse> getLast5ExpensesForCurrentUser();

    BigDecimal getTotalExpensesForCurrentUser();

    List<ExpenseResponse> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(LocalDateTime startDate,
                                                                           LocalDateTime endDate,
                                                                           String name,
                                                                           Sort sort);

    List<ExpenseResponse> findByProfileIdAndDateBetween(Long profileId, LocalDateTime startDate, LocalDateTime endDate);

}
