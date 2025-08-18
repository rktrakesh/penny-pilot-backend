package com.pennypilot.service.impl;

import com.pennypilot.dto.RecentTransactionDto;
import com.pennypilot.dto.response.ExpenseResponse;
import com.pennypilot.dto.response.IncomeResponse;
import com.pennypilot.model.Profile;
import com.pennypilot.service.DashboardService;
import com.pennypilot.service.ExpenseService;
import com.pennypilot.service.IncomeService;
import com.pennypilot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;


    @Override
    public ResponseEntity<?> getDashboardDataForCurrentUser() {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            log.info("Fetching dashboard data for user: {}", currentProfile.getFullName());
            List<IncomeResponse> latestIncomes = incomeService.getLast5IncomesForCurrentUser();
            BigDecimal totalIncomes = incomeService.getTotalIncomesForCurrentUser();
            List<ExpenseResponse> latestExpenses = expenseService.getLast5ExpensesForCurrentUser();
            BigDecimal totalExpenses = expenseService.getTotalExpensesForCurrentUser();

            Map<String, Object> dashboardData = new LinkedHashMap<>();

            List<RecentTransactionDto> recentTransactions = concat(latestIncomes.stream().map(
                            income -> RecentTransactionDto.builder()
                                    .id(income.getId())
                                    .profileId(currentProfile.getId())
                                    .name(income.getName())
                                    .icon(income.getIcon())
                                    .amount(income.getAmount())
                                    .date(income.getDate().toLocalDate())
                                    .createdAt(income.getCreatedAt())
                                    .updatedAt(income.getUpdatedAt())
                                    .type("income")
                                    .build()),
                    latestExpenses.stream().map(
                                    expense -> RecentTransactionDto.builder()
                                            .id(expense.getId())
                                            .profileId(currentProfile.getId())
                                            .name(expense.getName())
                                            .icon(expense.getIcon())
                                            .amount(expense.getAmount())
                                            .date(expense.getDate().toLocalDate())
                                            .createdAt(expense.getCreatedAt())
                                            .updatedAt(expense.getUpdatedAt())
                                            .type("expense")
                                            .build())
                            .sorted((a, b) -> {
                                int dateComparison = b.getDate().compareTo(a.getDate());
                                if (dateComparison == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                                }
                                return dateComparison;
                            })).toList();

            log.info("Latest incomes: {}, Latest expenses: {}", latestIncomes.size(), latestExpenses.size());
            dashboardData.put("totalBalance", totalIncomes.subtract(totalExpenses));
            dashboardData.put("totalIncomes", totalIncomes);
            dashboardData.put("totalExpenses", totalExpenses);
            dashboardData.put("recent5Expenses", latestExpenses);
            dashboardData.put("recent5Incomes", latestIncomes);
            dashboardData.put("recentTransactions", recentTransactions);
            log.info("Dashboard data fetched successfully for user: {}", currentProfile.getFullName());
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error while fetching dashboard data: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to fetch dashboard data");
        }
    }
}
