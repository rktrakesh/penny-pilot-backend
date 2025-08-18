package com.pennypilot.service.impl;

import com.pennypilot.dto.request.IncomeRequest;
import com.pennypilot.dto.response.IncomeResponse;
import com.pennypilot.model.Category;
import com.pennypilot.model.Expense;
import com.pennypilot.model.Income;
import com.pennypilot.model.Profile;
import com.pennypilot.repo.CategoryRepository;
import com.pennypilot.repo.IncomeRepository;
import com.pennypilot.service.IncomeService;
import com.pennypilot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;

    @Override
    public ResponseEntity<?> addNewIncome(IncomeRequest incomeRequest) {
        try {
            if (incomeRequest == null && incomeRequest.getCategoryId() == null) {
                log.error("Income request or category ID is null");
                return ResponseEntity.badRequest().body("Invalid income request");
            }
            Profile profile = profileService.getCurrentProfile();
            Category categoryDetails = categoryRepository.findById(incomeRequest.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            Income income = mapToExpense(incomeRequest, categoryDetails, profile);
            log.info("Adding new income: {}", income);
            Income savedIncome = incomeRepository.save(income);
            IncomeResponse incomeResponse = mapToExpenseResponse(savedIncome);
            log.info("Income added successfully: {}", incomeResponse);
            return ResponseEntity.ok(incomeResponse);
        } catch (Exception e) {
            log.error("Error while adding new income: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add income");
        }
    }

    @Override
    public ResponseEntity<?> getCurrentMonthIncomeForCurrentUser() {
        try {
            log.info("Fetching current month income for user");
            Profile currentProfile = profileService.getCurrentProfile();
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
            log.info("Current profile ID: {}", currentProfile.getId());
            List<Income> incomeDetails = incomeRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startOfMonth, endOfMonth);
            log.info("Income details fetched: {}", incomeDetails);
            if (incomeDetails.isEmpty()) {
                log.info("No income found for the current month");
                return ResponseEntity.ok("No income found for the current month");
            }
            List<IncomeResponse> incomeResponses = incomeDetails.stream().map(this::mapToExpenseResponse).toList();
            log.info("Current month income responses: {}", incomeResponses);
            return ResponseEntity.ok(incomeResponses);
        } catch (Exception e) {
            log.info("Error while fetching current month income: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to fetch current month income");
        }
    }

    @Override
    public ResponseEntity<?> deleteIncomeById(Long incomeId) {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            Income incomeDetails = incomeRepository.findById(incomeId).orElse(null);
            if (incomeDetails == null) {
                log.error("Income with ID {} not found", incomeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Income not found");
            }
            if (incomeDetails.getProfile() == null || !incomeDetails.getProfile().getId().equals(currentProfile.getId())) {
                log.error("User {} trying to access income {}", currentProfile.getId(), incomeId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            log.info("Deleting income with ID: {}", incomeId);
            incomeRepository.deleteById(incomeId);
            log.info("Income with ID {} deleted successfully", incomeId);
            return ResponseEntity.ok("Income deleted successfully");
        } catch (Exception e) {
            log.error("Error while deleting income by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to delete income");
        }
    }

    @Override
    public List<IncomeResponse> getLast5IncomesForCurrentUser() {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            log.info("Fetching last 5 incomes for user with profile ID: {}", currentProfile.getId());
            List<Income> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(currentProfile.getId());
            if (incomes.isEmpty()) {
                log.info("No incomes found for the current user");
                return List.of();
            }
            log.info("Last 5 incomes fetched successfully: {}", incomes);
            return incomes.stream().map(this::mapToExpenseResponse).toList();
        } catch (Exception e) {
            log.info("Error while fetching last 5 incomes: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public BigDecimal getTotalIncomesForCurrentUser() {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            log.info("Fetching total incomes for user with profile ID: {}", currentProfile.getId());
            BigDecimal totalIncomes = incomeRepository.findTotalIncomeByProfileId(currentProfile.getId());
            if (totalIncomes == null) {
                log.info("No incomes found for the current user, returning zero");
                return BigDecimal.ZERO;
            }
            log.info("Total incomes fetched successfully: {}", totalIncomes);
            return totalIncomes;
        } catch (Exception e) {
            log.info("Error while fetching total incomes: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Income mapToExpense(IncomeRequest incomeRequest, Category category, Profile profile) {
        return Income.builder()
                .name(incomeRequest.getName())
                .icon(incomeRequest.getIcon())
                .amount(incomeRequest.getAmount())
                .date(incomeRequest.getDate())
                .category(category)
                .profile(profile)
                .build();
    }

    private IncomeResponse mapToExpenseResponse(Income income) {
        return IncomeResponse.builder()
                .id(income.getId())
                .name(income.getName())
                .icon(income.getIcon())
                .amount(income.getAmount())
                .date(income.getDate())
                .categoryId(income.getCategory().getId())
                .categoryName(income.getCategory().getName())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }

}
