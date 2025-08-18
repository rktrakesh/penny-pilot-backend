package com.pennypilot.service.impl;

import com.pennypilot.dto.AuthDto;
import com.pennypilot.dto.request.ProfileRequest;
import com.pennypilot.dto.response.ProfileResponse;
import com.pennypilot.mapper.ProfileDtoMapper;
import com.pennypilot.mapper.ProfileMapper;
import com.pennypilot.model.Profile;
import com.pennypilot.repo.ProfileRepository;
import com.pennypilot.service.EmailService;
import com.pennypilot.service.ProfileService;
import com.pennypilot.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${email.activation.link}")
    private String activationLink;

    @Override
    public ResponseEntity<?> registerNewProfile(ProfileRequest request) {
        try {
            log.info("Register new profile for user: {}",request);
            if (request == null) {
                log.warn("Profile details must not be null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile details must not be null");
            }
            Profile profile = ProfileMapper.mapToEntity(request);
            profile.setActivationToken(UUID.randomUUID().toString());
            profile.setPassword(passwordEncoder.encode(profile.getPassword()));
            Profile register = profileRepository.save(profile);
            log.info("Register new profile completed.");

            // Mail activation
            log.info("Email verification link send starts.");
            String accountActivationLink = activationLink + profile.getActivationToken();
            String subject = "Activate your Penny Pilot account";
            String body = "Click the following link to activate your account: " + accountActivationLink;
            emailService.sendEmail(profile.getEmail(), subject, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProfileDtoMapper.mapToDto(register));
        } catch (Exception e) {
            log.error("Exception while registerNewProfile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception while registerNewProfile: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> activateProfile(String token) {
        try {
            if (token != null) {
                if (token.startsWith("token=")) {
                    token = token.substring(6);
                }
                Optional<Profile> byActivationToken = profileRepository.findByActivationToken(token);
                if (byActivationToken.isPresent()) {
                    Profile profile = byActivationToken.get();
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return ResponseEntity.ok("Profile activated successfully.");
                } else {
                    log.warn("No profile found with token: {}", token);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid activation token.");
                }
            }
            log.warn("Unable to activate the profile, token is null.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to activate the account.");
        } catch (Exception e) {
            log.error("Exception while activate the profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception while activate the profile: " + e.getMessage());
        }
    }

    @Override
    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(Profile::getIsActive)
                .orElse(false);
    }

    @Override
    public ResponseEntity<?> authenicateAndGenerateToken(AuthDto authDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword())
            );
            String jwtToken = jwtUtil.generateToken(authDto.getEmail());
            Map<String, Object> token = Map.of(
                    "token", jwtToken,
                    "user", getPublicProfile(authDto.getEmail() != null ? authDto.getEmail() : null)
            );
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Exception while authenticating and generating token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid email or password: " + e.getMessage());
        }
    }

    @Override
    public Profile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Profile not found for email: " + authentication.getName()));
    }

    public ProfileResponse getPublicProfile(String email) {
        Profile currentUser = null;
        if (email == null || email.isEmpty()) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Profile not found for email: " + email));
        }
        return ProfileDtoMapper.mapToDto(currentUser);
    }

}
