package com.pennypilot.controller;

import com.pennypilot.dto.AuthDto;
import com.pennypilot.dto.ProfileRequest;
import com.pennypilot.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
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
        try {
            if (authDto == null || authDto.getEmail() == null) {
                return ResponseEntity.badRequest().body("Email must not be null");
            }
            boolean active = profileService.isAccountActive(authDto.getEmail());
            if (!active) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Account is not active, please activate your account first");
            }
            return profileService.authenicateAndGenerateToken(authDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }


}
