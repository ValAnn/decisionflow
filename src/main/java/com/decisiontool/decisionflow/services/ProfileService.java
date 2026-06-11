package com.decisiontool.decisionflow.services;
import com.decisiontool.decisionflow.entities.AnalystProfile;
import com.decisiontool.decisionflow.entities.DeveloperProfile;
import com.decisiontool.decisionflow.repositories.AnalystRepository;
import com.decisiontool.decisionflow.repositories.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final AnalystRepository analystRepository;
    private final DeveloperRepository developerRepository;
    public AnalystProfile getAnalystProfile(Long userId) {
        return analystRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Профиль аналитика не найден"));
    }
    public DeveloperProfile getDeveloperProfile(Long userId) {
        return developerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Профиль разработчика не найден"));
    }
}
