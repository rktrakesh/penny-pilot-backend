package com.pennypilot.controller;

import com.pennypilot.dto.request.CategoryRequest;
import com.pennypilot.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCategories() {
        return categoryService.getAllCategoriesForCurrentUser();
    }

    @GetMapping("/{type}")
    public ResponseEntity<?> getCategoryByIdAndType(@PathVariable("type") String type) {
        return categoryService.getCategoryByIdTypeAndCurrentUser(type);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @RequestBody CategoryRequest categoryRequest) {
        return categoryService.updateCategoryByIdAndCurrentUser(id, categoryRequest);
    }

}
