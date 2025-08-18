package com.pennypilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String type;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long profileId;

}
