package com.keon.todoapp.config;

import com.keon.todoapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 설정 클래스
 * 인증 및 권한 관리, 로그인/로그아웃 처리를 담당
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;

    /**
     * BCrypt 패스워드 인코더 빈 등록
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider 설정
     * UserDetailsService와 PasswordEncoder를 연동
     *
     * @return DaoAuthenticationProvider 인스턴스
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager 설정
     *
     * @param auth AuthenticationManagerBuilder
     * @throws Exception 설정 중 예외 발생 시
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    /**
     * HTTP Security 설정
     * URL별 접근 권한, 로그인/로그아웃 설정
     *
     * @param http HttpSecurity 객체
     * @throws Exception 설정 중 예외 발생 시
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // URL별 접근 권한 설정
            .authorizeRequests()
                // 공개 접근 허용 경로
                .antMatchers("/", "/login", "/register", "/about").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                // 사용자 전용 경로는 인증 필요
                .antMatchers("/user/**").authenticated()
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            .and()

            // 폼 로그인 설정
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/user/todos", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            .and()

            // 로그아웃 설정
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()

            // 개발용 설정 (운영환경에서는 제거 필요)
            .csrf().disable() // H2 Console 사용을 위해 비활성화
            .headers().frameOptions().disable(); // H2 Console iframe 허용
    }
}
