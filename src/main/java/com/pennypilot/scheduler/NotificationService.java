package com.pennypilot.scheduler;

import com.pennypilot.dto.response.ExpenseResponse;
import com.pennypilot.model.Category;
import com.pennypilot.model.Profile;
import com.pennypilot.repo.CategoryRepository;
import com.pennypilot.repo.ProfileRepository;
import com.pennypilot.service.EmailService;
import com.pennypilot.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ExpenseService expenseService;
    private final EmailService emailService;
    private final CategoryRepository categoryRepository;
    private final ProfileRepository profileRepository;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 21 * * *", zone = "IST") // Every day at 9 PM
    public void sendDailyIncomeAndExpenseRemainder() {
        try {
            log.info("Starting daily income and expense report remainder:: STARTS.");
            List<Profile> allProfiles = profileRepository.findAll();
            for (Profile profile : allProfiles) {
                log.info("Generating remainder for profile: {}", profile.getEmail());
                String subject = "PennyPilot: Daily Income & Expense Reminder";
                String body = buildDailyIncomeExpenseEmail(profile);
                emailService.sendEmail(profile.getEmail(), subject, body);
            }
        } catch (Exception e) {
            log.error("Error while sending daily income and expense report: {}", e.getMessage());
        } finally {
            log.info("Starting daily income and expense report remainder:: ENDS.");
        }
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "IST") // Every day at 9 PM
    public void sendDailyExpenseSummery () {
        try {
            log.info("Starting daily expense summary sending process:: STARTS.");
            List<Profile> profiles = profileRepository.findAll();
             for(Profile profile: profiles) {
                log.info("Generating daily expense summary for profile: {}", profile.getEmail());
                LocalDate today = LocalDate.now();
                LocalDateTime startOfDay = today.atStartOfDay();
                LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
                List<ExpenseResponse> expenses = expenseService.findByProfileIdAndDateBetween(profile.getId(), startOfDay, endOfDay);
                log.info("Found {} expenses for profile: {}", expenses.size(), profile.getEmail());
                if (expenses.isEmpty()) {
                    log.info("No expenses found for profile: {}", profile.getEmail());
                    continue;
                }
                String subject = "PennyPilot: Daily Expense Summary";
                String body = buildDailyExpenseSummaryEmail(profile, expenses);
                emailService.sendEmail(profile.getEmail(), subject, body);
            }
        } catch (Exception e) {
            log.error("Error while sending daily expense summary: {}", e.getMessage());
        } finally {
            log.info("Starting daily expense summary sending process:: ENDS.");
        }
    }

    /**
     * Builds the HTML email body for daily income & expense reminder.
     */
    public String buildDailyIncomeExpenseEmail(Profile profile) {
        String name = profile != null && profile.getFullName() != null ? profile.getFullName() : "there";
        String url = frontendUrl;
        String year = String.valueOf(java.time.Year.now().getValue());

        String template = """
        <div style="display:flex; justify-content:center; align-items:center; text-align:center; width:100%%; height:100%%;">
            <div style="max-width:600px; margin:auto; padding:20px;">

                <div style="font-size:26px;font-weight:700;color:#f97316;margin-bottom:10px">
                    PennyPilot
                </div>

                <div style="font-size:20px;font-weight:600;color:#111827;margin-bottom:14px">
                    Daily Reminder
                </div>

                <div style="font-size:15px;color:#374151;margin-bottom:16px">
                    Hi %s 👋, don’t forget to log today’s <b>income</b> and <b>expenses</b> in your
                    <span style="color:#f97316;font-weight:600">PennyPilot</span> app.
                </div>

                <div style="font-size:15px;color:#374151;margin-bottom:24px">
                    Keeping your daily records updated helps you stay in control of your
                    <b>finances</b>, track spending habits, and plan smarter for the future.
                    It only takes a minute, but gives you long-term clarity!
                </div>

                <div style="margin-bottom:22px; font-size:16px;">
                    <b><a href="%s" target="_blank" style="color:#f97316; text-decoration:underline;">
                        Click here to Manage Income & Expenses
                    </a></b>
                </div>

                <div style="font-size:12px;color:#9ca3af;margin-top:12px">
                    © %s PennyPilot — Your trusted daily finance companion.
                </div>

            </div>
        </div>
        """;

        return String.format(template, name, url, year);
    }

    /**
     * Builds the HTML email body for daily expense summary.
     */
    private String buildDailyExpenseSummaryEmail(Profile profile, List<ExpenseResponse> expenses) {
        String name = profile != null && profile.getFullName() != null ? profile.getFullName() : "there";
        String year = String.valueOf(java.time.Year.now().getValue());

        // Build expense rows
        StringBuilder rows = new StringBuilder();
        for (ExpenseResponse exp : expenses) {
            Category category = categoryRepository.findById(exp.getCategoryId()).orElse(null);
            String categoryName = category != null ? category.getName() : "";
            rows.append(String.format("""
                        <tr>
                            <td style="padding:6px; border:1px solid #ddd;">%s</td>
                            <td style="padding:6px; border:1px solid #ddd;">%.2f</td>
                            <td style="padding:6px; border:1px solid #ddd;">%s</td>
                        </tr>
                    """, exp.getName(), exp.getAmount(), categoryName));
        }

        String template = """
                <div style="display:flex; justify-content:center; align-items:center; text-align:center; width:100%%;">
                    <div style="max-width:600px; margin:auto; padding:20px; font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
                
                        <div style="font-size:26px;font-weight:700;color:#f97316;margin-bottom:10px">
                            PennyPilot
                        </div>
                
                        <div style="font-size:20px;font-weight:600;color:#111827;margin-bottom:12px">
                            Daily Expense Summary
                        </div>
                
                        <div style="font-size:15px;color:#374151;margin-bottom:14px;line-height:1.4">
                            Hi %s 👋,<br>
                            💡 <i>“Small expenses, when tracked daily, create big savings tomorrow.”</i>
                        </div>
                
                        <table style="width:100%%; border-collapse:collapse; margin-bottom:16px;">
                            <thead>
                                <tr style="background-color:#f97316; color:white; text-align:left;">
                                    <th style="padding:6px; border:1px solid #ddd;">Name</th>
                                    <th style="padding:6px; border:1px solid #ddd;">Amount (₹)</th>
                                    <th style="padding:6px; border:1px solid #ddd;">Category</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                
                        <div style="font-size:12px;color:#9ca3af;margin-top:10px">
                            © %s PennyPilot — Your trusted daily finance companion.
                        </div>
                
                    </div>
                </div>
                """;

        return String.format(template, name, rows.toString().trim(), year);
    }

}
