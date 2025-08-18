package com.pennypilot.mapper;

import com.pennypilot.dto.ProfileRequest;
import com.pennypilot.model.Profile;

public class ProfileMapper {
    public static Profile mapToEntity (ProfileRequest request) {
        return Profile.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .profileImageUrl(request.getProfileImageUrl())
                .build();
    }
}
