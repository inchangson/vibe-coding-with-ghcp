package com.keon.todoapp.config;

import com.keon.todoapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Security 설정 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security 설정 테스트")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("공개 페이지 접근 허용")
    void publicPages_AccessAllowed() throws Exception {
        // 홈페이지
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // 로그인 페이지
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        // 회원가입 페이지
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        // About 페이지
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정적 리소스 접근 허용")
    void staticResources_AccessAllowed() throws Exception {
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("H2 Console 접근 허용")
    void h2Console_AccessAllowed() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 전용 페이지 - 비인증 시 로그인 페이지로 리다이렉트")
    void userPages_UnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/user/todos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    @DisplayName("사용자 전용 페이지 - 인증 시 접근 허용")
    void userPages_AuthenticatedAccess() throws Exception {
        // MockBean으로 UserService가 설정되어 있어 실제 호출은 되지 않지만,
        // Security 설정 자체는 테스트 가능
        mockMvc.perform(get("/user/todos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 처리")
    void login_Processing() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 처리")
    void logout_Processing() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("CSRF 보호 - POST 요청 시 CSRF 토큰 필요")
    void csrf_Protection() throws Exception {
        // CSRF 토큰 없이 POST 요청 시 403 에러
        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().isForbidden());

        // CSRF 토큰과 함께 POST 요청 시 정상 처리
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection());
    }
}
