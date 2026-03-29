package com.decisiontool.decisionflow.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decisiontool.decisionflow.dtos.JwtResponse;
import com.decisiontool.decisionflow.dtos.LoginRequest;
import com.decisiontool.decisionflow.dtos.RegisterRequest;
import com.decisiontool.decisionflow.entities.User;
import com.decisiontool.decisionflow.repositories.UserRepository;
import com.decisiontool.decisionflow.utils.JwtUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder; 

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        // 1. Проверяем, не занято ли имя
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // 2. Создаем нового пользователя
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setFullName(signUpRequest.getFullName());
        
        // КРИТИЧНО: Хэшируем пароль перед сохранением
        user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    
    // 1. Проверяем логин и пароль
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // 2. Генерируем токен
    String jwt = jwtUtils.generateToken(authentication);
    
    // 3. Получаем объект пользователя из базы или из Principal
    // Предположим, у тебя есть UserRepository
    User user = userRepository.findByUsername(loginRequest.getUsername())
    .orElseThrow(() -> new RuntimeException("Ошибка: Пользователь не найден в БД после аутентификации"));
    
    // 4. Возвращаем всё вместе
    return ResponseEntity.ok(new JwtResponse(
        jwt, 
        user.getUsername(), 
        user.getEmail(), 
        user.getFullName(), 
        user.getId()
    ));
}
}