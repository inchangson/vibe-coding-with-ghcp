package com.keon.todoapp.service;

import com.keon.todoapp.model.User;
import com.keon.todoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 */
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 성공")
    void loadUserByUsername_Success() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        UserDetails result = userService.loadUserByUsername("testuser");

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 사용자 없음")
    void loadUserByUsername_UserNotFound() {
        // given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("새 사용자 등록 - 성공")
    void registerUser_Success() {
        // given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        User result = userService.registerUser(newUser);

        // then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("새 사용자 등록 - 중복된 사용자명")
    void registerUser_DuplicateUsername() {
        // given
        User duplicateUser = new User();
        duplicateUser.setUsername("existinguser");
        duplicateUser.setPassword("password123");
        
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(duplicateUser);
        });
        
        assertTrue(exception.getMessage().contains("이미 존재하는 사용자명"));
        verify(userRepository).existsByUsername("existinguser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자명으로 사용자 찾기 - 성공")
    void findByUsername_Success() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        User result = userService.findByUsername("testuser");

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(1L, result.getId());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("사용자명으로 사용자 찾기 - 사용자 없음")
    void findByUsername_UserNotFound() {
        // given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findByUsername("nonexistent");
        });
        
        assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("사용자명 존재 여부 확인 - 존재함")
    void isUsernameExists_True() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // when
        boolean result = userService.isUsernameExists("testuser");

        // then
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("사용자명 존재 여부 확인 - 존재하지 않음")
    void isUsernameExists_False() {
        // given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // when
        boolean result = userService.isUsernameExists("newuser");

        // then
        assertFalse(result);
        verify(userRepository).existsByUsername("newuser");
    }
}
