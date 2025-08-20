package com.pennypilot.controller;

import com.pennypilot.dto.AuthDto;
import com.pennypilot.dto.request.ProfileRequest;
import com.pennypilot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<?> registerNewProfile (@RequestBody ProfileRequest request) {
        return profileService.registerNewProfile(request);
    }

    @GetMapping("/email/activation")
    public ResponseEntity<?> activateProfile (@RequestParam("token") String token) {
        return profileService.activateProfile(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> isAccountActive(@RequestBody AuthDto authDto) {
        if (authDto == null || authDto.getEmail() == null || authDto.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password must not be null"));
        }

        boolean active = profileService.isAccountActive(authDto.getEmail());
        if (!active) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account is not active, please activate first"));
        }

        return profileService.authenicateAndGenerateToken(authDto);
    }

}
