package com.decisiontool.decisionflow.controllers;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.decisiontool.decisionflow.dtos.DashboardOverviewDTO;
import com.decisiontool.decisionflow.dtos.WorkloadPointDTO;
import com.decisiontool.decisionflow.services.DashboardService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }
        DashboardOverviewDTO dto = dashboardService.getOverview(authentication.getName());
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/workload")
    public ResponseEntity<?> getWorkload(
        Authentication authentication,
        @RequestParam(name = "period", defaultValue = "week") String period
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }
        List<WorkloadPointDTO> dto = dashboardService.getWorkload(authentication.getName(), period);
        return ResponseEntity.ok(dto);
    }
}
