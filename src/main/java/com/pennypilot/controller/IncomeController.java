package com.pennypilot.controller;

import com.pennypilot.dto.request.IncomeRequest;
import com.pennypilot.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> addNewIncome(@RequestBody IncomeRequest incomeRequest) {
        return incomeService.addNewIncome(incomeRequest);
    }

    @GetMapping("/current-month")
    public ResponseEntity<?> getCurrentMonthIncomeForCurrentUser() {
        return incomeService.getCurrentMonthIncomeForCurrentUser();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteIncomeById(@PathVariable Long id) {
        return incomeService.deleteIncomeById(id);
    }

}
