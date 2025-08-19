package com.pennypilot.service.impl;

import com.pennypilot.dto.FilterDto;
import com.pennypilot.dto.response.ExpenseResponse;
import com.pennypilot.dto.response.IncomeResponse;
import com.pennypilot.service.ExpenseService;
import com.pennypilot.service.FilterService;
import com.pennypilot.service.IncomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterServiceImpl implements FilterService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @Override
    public ResponseEntity<?> filterTransactions(FilterDto filterDto) {
        try {
            if (filterDto == null) {
                log.warn("Filter request is null");
                return ResponseEntity.badRequest().body("Invalid filter request");
            }
            log.info("Filtering transactions with criteria: {}", filterDto);
            LocalDateTime startDate = filterDto.getStartDate() != null
                    ? filterDto.getStartDate()
                    : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime endDate = filterDto.getEndDate() != null
                    ? filterDto.getEndDate()
                    : LocalDateTime.of(9999, 12, 31, 23, 59);
            String keyword = filterDto.getKeyword() != null ? filterDto.getKeyword() : "";
            String sortedField = filterDto.getSortField() != null ? filterDto.getSortField() : "date";
            Sort.Direction direction = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortedField);
            log.info("Using sort criteria: {}", sort);

            if ("income".equalsIgnoreCase(filterDto.getType())) {
                List<IncomeResponse> incomes = incomeService.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        startDate, endDate, keyword, sort);
                log.info("Found {} incomes matching criteria", incomes.size());
                return ResponseEntity.ok(incomes);
            } else if ("expense".equalsIgnoreCase(filterDto.getType())) {
                List<ExpenseResponse> expenses = expenseService.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        startDate, endDate, keyword, sort);
                log.info("Found {} expenses matching criteria", expenses.size());
                return ResponseEntity.ok(expenses);
            } else {
                log.warn("Invalid transaction type: {}", filterDto.getType());
                return ResponseEntity.badRequest().body("Invalid transaction type");
            }
        } catch (Exception e) {
            log.error("Error while filtering transactions: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to filter transactions");
        }
    }

}
