package com.pennypilot.service.impl;

import com.pennypilot.dto.request.CategoryRequest;
import com.pennypilot.dto.response.CategoryResponse;
import com.pennypilot.model.Category;
import com.pennypilot.model.Profile;
import com.pennypilot.repo.CategoryRepository;
import com.pennypilot.service.CategoryService;
import com.pennypilot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    @Override
    public ResponseEntity<?> createCategory(CategoryRequest categoryRequest) {
        try {
            log.info("Creating category with request: {}", categoryRequest);
            if (categoryRequest == null || categoryRequest.getName() == null || categoryRequest.getType() == null) {
                log.info("Invalid category request: {}", categoryRequest);
                return ResponseEntity.badRequest().body("Invalid category request");
            }
            if (categoryRepository.existsByNameAndProfileId(categoryRequest.getName(), profileService.getCurrentProfile().getId())) {
                log.info("Category with name '{}' already exists for the current user", categoryRequest.getName());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Category with this name already exists");
            }

            Profile currentProfile = profileService.getCurrentProfile();
            if (currentProfile == null) {
                log.info("User not authenticated :: createCategory");
                return ResponseEntity.status(403).body("User not authenticated");
            }
            Category categoryEntity = getCategoryEntity(categoryRequest, currentProfile);
            Category save = categoryRepository.save(categoryEntity);
            CategoryResponse response = mapToResponse(save);
            log.info("Category created successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error creating category: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllCategoriesForCurrentUser() {
        try {
            log.info("Fetching all categories for current user");
            Profile currentProfile = profileService.getCurrentProfile();
            if (currentProfile == null) {
                log.info("User not authenticated :: getAllCategoriesForCurrentUser");
                return ResponseEntity.status(403).body("User not authenticated");
            }
            List<Category> categories = categoryRepository.findByProfileId(currentProfile.getId());
            if (categories.isEmpty()) {
                log.info("No categories found for current user: {}", currentProfile.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No categories found for current user: " + currentProfile.getEmail());
            }
            List<CategoryResponse> categoryResponses = categories.stream().map(this::mapToResponse).toList();
            log.info("Successfully fetched {} categories for user: {}", categoryResponses.size(), currentProfile.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(categoryResponses);
        } catch (Exception e) {
            log.error("Error fetching categories for current user: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error fetching categories: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getCategoryByIdTypeAndCurrentUser(String type) {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            if (currentProfile == null) {
                log.info("User not authenticated :: getCategoryByIdTypeAndCurrentUser");
                return ResponseEntity.status(403).body("User not authenticated");
            }
            log.info("Fetching category by type: {}", type);
            List<Category> categories = categoryRepository.findByTypeAndProfileId(type, currentProfile.getId());
            if (categories.isEmpty()) {
                log.info("No categories found for type '{}' for user: {}", type, currentProfile.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No categories found for type '" + type + "' for user: " + currentProfile.getEmail());
            }
            log.info("Successfully fetched {} categories for type '{}' for user: {}", categories.size(), type, currentProfile.getEmail());
            List<CategoryResponse> categoryResponses = categories.stream().map(this::mapToResponse).toList();
            return ResponseEntity.ok(categoryResponses);
        } catch (Exception e) {
            log.error("Error fetching category by type: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error fetching category by type: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateCategoryByIdAndCurrentUser(Long id, CategoryRequest categoryRequest) {
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            if (currentProfile == null) {
                log.info("User not authenticated :: updateCategoryByIdAndCurrentUser");
                return ResponseEntity.status(403).body("User not authenticated");
            }
            log.info("Updating category with ID: {} for user: {}", id, currentProfile.getEmail());
            if (categoryRequest == null) {
                log.info("Invalid category request: {}", categoryRequest);
                return ResponseEntity.badRequest().body("Invalid category request");
            }
            Category categoryDetails = categoryRepository.findByIdAndProfileId(id, currentProfile.getId())
                    .orElseThrow(() -> {
                        log.info("Category with ID: {} not found for user: {}", id, currentProfile.getEmail());
                        return new RuntimeException("Category not found");
                    });
            categoryDetails.setType(categoryRequest.getType() != null ? categoryRequest.getType() : categoryDetails.getType());
            categoryDetails.setName(categoryRequest.getName() != null ? categoryRequest.getName() : categoryDetails.getName());
            categoryDetails.setIconUrl(categoryRequest.getIconUrl() != null ? categoryRequest.getIconUrl() : categoryDetails.getIconUrl());
            Category updatedCategory = categoryRepository.save(categoryDetails);
            CategoryResponse response = mapToResponse(updatedCategory);
            log.info("Category updated successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating category: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error updating category: " + e.getMessage());
        }
    }

    private Category getCategoryEntity(CategoryRequest request, Profile profile) {
        return Category.builder()
                .name(request.getName())
                .iconUrl(request.getIconUrl())
                .type(request.getType())
                .profile(profile)
                .build();
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .iconUrl(category.getIconUrl())
                .profileId(category.getProfile() != null ? category.getProfile().getId() : null)
                .type(category.getType())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

}
