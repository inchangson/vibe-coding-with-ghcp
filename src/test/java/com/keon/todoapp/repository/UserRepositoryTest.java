package com.keon.todoapp.repository;

import com.keon.todoapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository 단위 테스트
 */
@DataJpaTest
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 성공")
    void findByUsername_Success() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        entityManager.persistAndFlush(user);

        // when
        Optional<User> result = userRepository.findByUsername("testuser");

        // then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 사용자 없음")
    void findByUsername_NotFound() {
        // when
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("사용자명 존재 여부 확인 - 존재함")
    void existsByUsername_True() {
        // given
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("password123");
        entityManager.persistAndFlush(user);

        // when
        boolean exists = userRepository.existsByUsername("existinguser");

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("사용자명 존재 여부 확인 - 존재하지 않음")
    void existsByUsername_False() {
        // when
        boolean exists = userRepository.existsByUsername("nonexistent");

        // then
        assertFalse(exists);
    }

    @Test
    @DisplayName("사용자 저장 및 조회")
    void saveAndFind() {
        // given
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("hashedpassword");

        // when
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertTrue(foundUser.isPresent());
        assertEquals("newuser", foundUser.get().getUsername());
        assertEquals("hashedpassword", foundUser.get().getPassword());
        assertNotNull(foundUser.get().getId());
    }
}
