package com.pennypilot.controller;

import com.pennypilot.dto.request.ExpenseRequest;
import com.pennypilot.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> addNewExpense(@RequestBody ExpenseRequest expenseRequest) {
        return expenseService.addNewExpense(expenseRequest);
    }

    @GetMapping("/current-month")
    public ResponseEntity<?> getCurrentMonthExpenseForCurrentUser() {
        return expenseService.getCurrentMonthExpenseForCurrentUser();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExpenseById(@PathVariable Long id) {
        return expenseService.deleteExpenseById(id);
    }

}
