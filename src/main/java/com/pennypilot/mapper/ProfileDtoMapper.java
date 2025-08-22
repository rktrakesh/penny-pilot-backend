package com.pennypilot.mapper;

import com.pennypilot.dto.response.ProfileResponse;
import com.pennypilot.model.Profile;

public class ProfileDtoMapper {
    public static ProfileResponse mapToDto (Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .profileImageUrl(profile.getProfileImageUrl())
                .activationToken(profile.getActivationToken())
                .createdAt(profile.getCreatedAt())
                .modifiedAt(profile.getModifiedAt())
                .build();
    }
}
