package com.keon.todoapp.controller;

import com.keon.todoapp.model.User;
import com.keon.todoapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AppController 통합 테스트
 */
@WebMvcTest(AppController.class)
@DisplayName("AppController 테스트")
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("홈페이지 접근")
    void index_Success() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("로그인 페이지 접근")
    void login_Success() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 에러 파라미터")
    void login_WithError() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 로그아웃 파라미터")
    void login_WithLogout() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("logoutMessage"));
    }

    @Test
    @DisplayName("회원가입 페이지 접근")
    void register_Get_Success() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("회원가입 처리 - 성공")
    void register_Post_Success() throws Exception {
        // given
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");

        when(userService.isUsernameExists("testuser")).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        // when & then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).isUsernameExists("testuser");
        verify(userService).registerUser(any(User.class));
    }

    @Test
    @DisplayName("회원가입 처리 - 중복된 사용자명")
    void register_Post_DuplicateUsername() throws Exception {
        // given
        when(userService.isUsernameExists("duplicate")).thenReturn(true);

        // when & then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "duplicate")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).isUsernameExists("duplicate");
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    @DisplayName("회원가입 처리 - 짧은 비밀번호")
    void register_Post_ShortPassword() throws Exception {
        // when & then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    @DisplayName("회원가입 처리 - 서비스 예외")
    void register_Post_ServiceException() throws Exception {
        // given
        when(userService.isUsernameExists("testuser")).thenReturn(false);
        when(userService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("About 페이지 접근")
    void about_Success() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    @DisplayName("인증 필요한 페이지 접근 - 비로그인 상태")
    void authenticatedPage_WithoutLogin() throws Exception {
        mockMvc.perform(get("/user/todos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    @DisplayName("인증 필요한 페이지 접근 - 로그인 상태")
    void authenticatedPage_WithLogin() throws Exception {
        // 이 테스트는 실제로는 TodoController에서 처리되지만,
        // Security 설정이 올바르게 작동하는지 확인
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk());
    }
}
