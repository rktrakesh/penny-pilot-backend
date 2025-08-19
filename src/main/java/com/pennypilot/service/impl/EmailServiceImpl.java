package com.pennypilot.service.impl;

import com.pennypilot.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mail;

    @Value("${email.id}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String sub, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(sub);
            message.setText(body);
            mail.send(message);
        } catch (Exception e) {
            log.error("Exception while sending email: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
