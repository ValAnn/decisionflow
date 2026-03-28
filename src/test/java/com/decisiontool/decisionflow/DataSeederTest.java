package com.decisiontool.decisionflow;

import com.decisiontool.decisionflow.entities.*;
import com.decisiontool.decisionflow.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class DataSeederTest {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AnalystRepository analystRepository;
    @Autowired private DeveloperRepository developerRepository;

    @Test
    @Transactional
    @Rollback(false) // Чтобы данные сохранились в БД после теста
    void seedDatabase() {
        // // 1. Создаем Роли
        // // Role adminRole = new Role(); adminRole.setName("ADMIN");
        // // Role analystRole = new Role(); analystRole.setName("ANALYST");
        // // Role devRole = new Role(); devRole.setName("DEVELOPER");
        // // roleRepository.save(adminRole);
        // // roleRepository.save(analystRole);
        // // roleRepository.save(devRole);

        // // 2. Создаем Скиллы
        // Skill java = new Skill(); java.setName("Java Spring"); java.setCategory("Backend");
        // Skill vue = new Skill(); vue.setName("Vue.js"); vue.setCategory("Frontend");
        // skillRepository.save(java);
        // skillRepository.save(vue);

        // // 3. Создаем Юзеров
        // User anna = new User();
        // anna.setUsername("valova_anna");
        // anna.setFullName("Анна Валова");
        // anna.setPasswordHash(passwordEncoder.encode("password"));
        // anna.setRole(analystRole);
        // userRepository.save(anna);

        // User ivan = new User();
        // ivan.setUsername("ivanov_dev");
        // ivan.setFullName("Иван Иванов");
        // ivan.setPasswordHash(passwordEncoder.encode("password"));
        // ivan.setRole(devRole);
        // userRepository.save(ivan);

        // // 4. Создаем Профили (Связываем с юзерами)
        // AnalystProfile annaProfile = new AnalystProfile();
        // annaProfile.setUser(anna);
        // annaProfile.setGrade("Senior");
        // analystRepository.save(annaProfile);

        // DeveloperProfile ivanProfile = new DeveloperProfile();
        // ivanProfile.setUser(ivan);
        // ivanProfile.setGrade("Middle");
        // ivanProfile.setSpecialization(java);
        // developerRepository.save(ivanProfile);

        System.out.println("--- БД успешно наполнена тестовыми данными! ---");
    }
}