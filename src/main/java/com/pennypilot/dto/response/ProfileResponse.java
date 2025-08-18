package com.pennypilot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String profileImageUrl;
    private String activationToken;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
