package com.pennypilot.controller;

import com.pennypilot.dto.FilterDto;
import com.pennypilot.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/filters")
@RequiredArgsConstructor
@Slf4j
public class FilterController {

    private final FilterService filterService;

    @PostMapping
    public ResponseEntity<?> filterTransactions (@RequestBody FilterDto filterDto) {
        return filterService.filterTransactions(filterDto);
    }

}
