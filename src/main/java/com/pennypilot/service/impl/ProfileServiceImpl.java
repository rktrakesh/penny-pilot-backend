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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Value("${penny.pilot.backend.url}")
    private String activationUrl;

    @Value("${email.activation.endpoint}")
    private String activationEndpoint;

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
            String accountActivationLink = activationUrl + activationEndpoint + profile.getActivationToken();
            String subject = "Activate your Penny Pilot account";
            String body = buildActivationEmailBody(profile, accountActivationLink);
            emailService.sendEmail(profile.getEmail(), subject, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProfileDtoMapper.mapToDto(register));
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate email detected: {}", request);
            return ResponseEntity.status(HttpStatus.OK).body("Activation email has already been sent.");
        } catch (Exception e) {
            log.error("Exception while registerNewProfile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception while registerNewProfile: " + e.getMessage());
        }
    }

    public String buildActivationEmailBody(Profile profile, String activationUrl) {
        String name = profile != null && profile.getFullName() != null ? profile.getFullName() : "there";
        String year = String.valueOf(java.time.Year.now().getValue());

        return String.format("""
        <div style="display:flex; justify-content:center; align-items:center; text-align:center; width:100%%;">
            <div style="max-width:600px; margin:auto; padding:20px; font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">

                <div style="font-size:26px;font-weight:700;color:#f97316;margin-bottom:10px">
                    PennyPilot
                </div>

                <div style="font-size:20px;font-weight:600;color:#111827;margin-bottom:12px">
                    Activate Your Account
                </div>

                <div style="font-size:15px;color:#374151;margin-bottom:14px;line-height:1.4">
                    Hi %s 👋,<br>
                    You're just one step away from unlocking your personal finance dashboard.
                </div>

                <div style="font-size:15px;color:#374151;margin-bottom:20px;line-height:1.4">
                    To get started, please activate your account by clicking the link below.
                    This helps us verify your email and keep your data secure.
                </div>

                <div style="margin-bottom:22px; font-size:16px;">
                    <b><a href="%s" target="_blank" style="color:#f97316; text-decoration:underline;">
                        Click here to Activate Your Account
                    </a></b>
                </div>

                <div style="font-size:12px;color:#9ca3af;margin-top:10px">
                    © %s PennyPilot — Your trusted daily finance companion.
                </div>

            </div>
        </div>
        """, name, activationUrl, year);
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
                    "user", getPublicProfile(authDto.getEmail())
            );
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {  // specific exception
            log.error("Invalid credentials: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            log.error("Exception while generating token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
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
