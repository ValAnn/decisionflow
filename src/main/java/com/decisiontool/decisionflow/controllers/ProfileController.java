package com.decisiontool.decisionflow.controllers;
import com.decisiontool.decisionflow.entities.AnalystProfile;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.services.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    @GetMapping("/analyst/{userId}")
    public AnalystProfile getAnalyst(@PathVariable Long userId) {
        return profileService.getAnalystProfile(userId);
    }
    @GetMapping("/developer/{userId}")
    public DeveloperProfile getDeveloper(@PathVariable Long userId) {
        return profileService.getDeveloperProfile(userId);
    }
}
