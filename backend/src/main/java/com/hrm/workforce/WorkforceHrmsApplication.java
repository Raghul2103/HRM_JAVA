package com.hrm.workforce;

import com.hrm.workforce.dto.SiteDto;
import com.hrm.workforce.dto.WorkerDto;
import com.hrm.workforce.entity.Designation;
import com.hrm.workforce.service.AuthService;
import com.hrm.workforce.service.SiteService;
import com.hrm.workforce.service.WorkerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.math.BigDecimal;

@SpringBootApplication
@EnableAsync
public class WorkforceHrmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkforceHrmsApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedData(AuthService authService, WorkerService workerService, SiteService siteService) {
        return args -> {
            // Register default operators safely
            registerUserSafe(authService, "admin", "admin123", "ROLE_ADMIN");
            registerUserSafe(authService, "hr_user", "hr123", "ROLE_HR");
            registerUserSafe(authService, "supervisor", "super123", "ROLE_SITE_SUPERVISOR");
            registerUserSafe(authService, "payroll", "pay123", "ROLE_PAYROLL_OPERATOR");
            registerUserSafe(authService, "manager", "manage123", "ROLE_SITE_MANAGER");

            // Seed sites safely
            seedSiteSafe(siteService, "Greenfield Phase 2", "Sector 62, Noida", true);
            seedSiteSafe(siteService, "Metro Station Block C", "Indiranagar, Bangalore", true);
            seedSiteSafe(siteService, "Riverfront Promenade", "Sabarmati, Ahmedabad", false);

            // Seed workers safely
            seedWorkerSafe(workerService, "Rajesh Kumar", "9876543210", Designation.MASON, new BigDecimal("500.00"), true);
            seedWorkerSafe(workerService, "Amit Sharma", "8765432109", Designation.ELECTRICIAN, new BigDecimal("450.00"), true);
            seedWorkerSafe(workerService, "Vijay Singh", "7654321098", Designation.PLUMBER, new BigDecimal("400.00"), true);
            seedWorkerSafe(workerService, "Ramesh Yadav", "6543210987", Designation.HELPER, new BigDecimal("300.00"), true);
            seedWorkerSafe(workerService, "Suresh Patel", "9998887776", Designation.SUPERVISOR, new BigDecimal("700.00"), true);
        };
    }

    private void registerUserSafe(AuthService authService, String username, String password, String role) {
        try {
            authService.register(username, password, role);
            System.out.println("Successfully seeded user: " + username);
        } catch (Exception e) {
            System.out.println("Skipped seeding user " + username + " (likely already exists): " + e.getMessage());
        }
    }

    private void seedSiteSafe(SiteService siteService, String siteName, String location, boolean active) {
        try {
            siteService.createSite(SiteDto.builder().siteName(siteName).location(location).active(active).build());
            System.out.println("Successfully seeded site: " + siteName);
        } catch (Exception e) {
            System.out.println("Skipped seeding site " + siteName + " (likely already exists): " + e.getMessage());
        }
    }

    private void seedWorkerSafe(WorkerService workerService, String name, String phone, Designation designation, BigDecimal dailyWageRate, boolean active) {
        try {
            workerService.createWorker(WorkerDto.builder().name(name).phone(phone).designation(designation).dailyWageRate(dailyWageRate).active(active).build());
            System.out.println("Successfully seeded worker: " + name);
        } catch (Exception e) {
            System.out.println("Skipped seeding worker " + name + " (likely already exists): " + e.getMessage());
        }
    }
}
