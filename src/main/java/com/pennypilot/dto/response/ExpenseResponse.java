package com.pennypilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor@NoArgsConstructor@Data@Builder
public class ExpenseResponse {

    private Long id;
    private String name;
    private String icon;
    private BigDecimal amount;
    private LocalDateTime date;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
