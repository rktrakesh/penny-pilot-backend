package com.pennypilot.service;

import com.pennypilot.dto.AuthDto;
import com.pennypilot.dto.ProfileRequest;
import org.springframework.http.ResponseEntity;

public interface ProfileService {

    ResponseEntity<?> registerNewProfile(ProfileRequest request);

    ResponseEntity<?> activateProfile(String token);

    boolean isAccountActive (String email);

    ResponseEntity<?> authenicateAndGenerateToken(AuthDto authDto);
}
