package com.pennypilot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data@NoArgsConstructor@AllArgsConstructor@Builder
public class ExpenseRequest {

    private String name;
    private String icon;
    private BigDecimal amount;
    private LocalDateTime date;
    private Long categoryId;

}
