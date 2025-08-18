package com.pennypilot.service;

import com.pennypilot.dto.request.CategoryRequest;
import org.springframework.http.ResponseEntity;

public interface CategoryService {

    ResponseEntity<?> createCategory (CategoryRequest categoryRequest);
    ResponseEntity<?> getAllCategoriesForCurrentUser ();
    ResponseEntity<?> getCategoryByIdTypeAndCurrentUser(String type);
    ResponseEntity<?> updateCategoryByIdAndCurrentUser(Long id, CategoryRequest categoryRequest);

}
