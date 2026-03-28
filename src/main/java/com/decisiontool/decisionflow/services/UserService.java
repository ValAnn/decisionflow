package com.decisiontool.decisionflow.services;

import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @Transactional
    public User createUser(User user) {
        // Шифруем пароль перед сохранением (вспоминаем наш SecurityConfig)
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public User getUserByJiraId(String jiraId) {
        return userRepository.findByJiraId(jiraId)
                .orElseThrow(() -> new RuntimeException("Пользователь с Jira ID " + jiraId + " не найден"));
    }
}