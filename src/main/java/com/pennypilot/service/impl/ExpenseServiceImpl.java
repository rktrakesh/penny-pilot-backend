package com.pennypilot.service.impl;

import com.pennypilot.dto.request.ExpenseRequest;
import com.pennypilot.dto.response.ExpenseResponse;
import com.pennypilot.model.Category;
import com.pennypilot.model.Expense;
import com.pennypilot.model.Profile;
import com.pennypilot.repo.CategoryRepository;
import com.pennypilot.repo.ExpenseRepository;
import com.pennypilot.service.ExpenseService;
import com.pennypilot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public ResponseEntity<?> addNewExpense(ExpenseRequest expenseRequest) {
        try {
            if (expenseRequest == null && expenseRequest.getCategoryId() == null) {
                log.error("Expense request or category ID is null");
                return ResponseEntity.badRequest().body("Invalid Expense request");
            }
            Profile profile = profileService.getCurrentProfile();
            Category categoryDetails = categoryRepository.findById(expenseRequest.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            Expense expense = mapToExpense(expenseRequest, categoryDetails, profile);
            log.info("Adding new expense: {}", expense);
            Expense expenses = expenseRepository.save(expense);
            ExpenseResponse expenseResponse = mapToExpenseResponse(expenses);
            log.info("Expense added successfully: {}", expenseResponse);
            return ResponseEntity.ok(expenseResponse);
        } catch (Exception e) {
            log.error("Error while adding new Expense: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add Expense");
        }
    }

    @Override
    public ResponseEntity<?> getCurrentMonthExpenseForCurrentUser() {
        try {
            log.info("Fetching current month expense for user");
            Profile currentProfile = profileService.getCurrentProfile();
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
            log.info("Current profile ID: {}", currentProfile.getId());
            List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startOfMonth, endOfMonth);
            log.info("Expense details fetched: {}", expenses);
            if (expenses.isEmpty()) {
                log.info("No Expense found for the current month");
                return ResponseEntity.ok("No Expense found for the current month");
            }
            List<ExpenseResponse> expenseResponses = expenses.stream().map(this::mapToExpenseResponse).toList();
            log.info("Current month expense responses: {}", expenseResponses);
            return ResponseEntity.ok(expenseResponses);
        } catch (Exception e) {
            log.info("Error while fetching current month expense: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to fetch current month expense");
        }
    }

    @Override
    public ResponseEntity<?> deleteExpenseById(Long expenseId) {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            Expense expense = expenseRepository.findById(expenseId).orElse(null);
            if (expense == null) {
                log.error("Expense with ID {} not found", expenseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found");
            }
            if (expense.getProfile() == null || !expense.getProfile().getId().equals(currentProfile.getId())) {
                log.error("User {} trying to access Expense {}", currentProfile.getId(), expenseId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            log.info("Deleting expense with ID: {}", expenseId);
            expenseRepository.deleteById(expenseId);
            log.info("Expense with ID {} deleted successfully", expenseId);
            return ResponseEntity.ok("Expense deleted successfully");
        } catch (Exception e) {
            log.error("Error while deleting expense by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to delete expense");
        }
    }

    @Override
    public List<ExpenseResponse> getLast5ExpensesForCurrentUser() {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            log.info("Fetching last 5 expenses for user with profile ID: {}", currentProfile.getId());
            List<Expense> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(currentProfile.getId());
            if (expenses.isEmpty()) {
                log.info("No expenses found for the current user");
                return List.of();
            }
            log.info("Last 5 expenses fetched successfully: {}", expenses);
            return expenses.stream().map(this::mapToExpenseResponse).toList();
        } catch (Exception e) {
            log.info("Error while fetching last 5 expenses: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public BigDecimal getTotalExpensesForCurrentUser() {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            log.info("Fetching total expenses for user with profile ID: {}", currentProfile.getId());
            BigDecimal totalExpenses = expenseRepository.findTotalExpensesByProfileId(currentProfile.getId());
            if (totalExpenses == null) {
                log.info("Total expenses for profile ID {} is null", currentProfile.getId());
                return BigDecimal.ZERO;
            }
            log.info("Total expenses fetched successfully: {}", totalExpenses);
            return totalExpenses;
        } catch (Exception e) {
            log.info("Error while fetching total expenses: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    public List<ExpenseResponse> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(LocalDateTime startDate,
                                                                                  LocalDateTime endDate,
                                                                                  String name,
                                                                                  Sort sort) {
        try {
            log.info("Finding expenses by date range: {} to {}, name: {}", startDate, endDate, name);
            Profile currentProfile = profileService.getCurrentProfile();
            List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentProfile.getId(), startDate, endDate, name, sort);
            if (expenses.isEmpty()) {
                log.info("No expenses found for the given criteria");
                return List.of();
            }
            log.info("Expenses found: {}", expenses);
            return expenses.stream().map(this::mapToExpenseResponse).toList();
        } catch (Exception e) {
            log.error("Error while finding expenses by profile ID, date range, and name: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<ExpenseResponse> findByProfileIdAndDateBetween(Long profileId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.info("Fetching today expense for user");
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
            List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetween(profileId, startOfMonth, endOfMonth);
            if (expenses.isEmpty()) {
                log.info("No Expense found for the Today.");
                return List.of();
            }
            return expenses.stream().map(this::mapToExpenseResponse).toList();
        } catch (Exception e) {
            log.info("Error while fetching today expense: {}", e.getMessage());
            return List.of();
        }
    }

    private Expense mapToExpense(ExpenseRequest expense, Category category, Profile profile) {
        return Expense.builder()
                .name(expense.getName())
                .icon(expense.getIcon())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .category(category)
                .profile(profile)
                .build();
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .name(expense.getName())
                .icon(expense.getIcon())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

}
