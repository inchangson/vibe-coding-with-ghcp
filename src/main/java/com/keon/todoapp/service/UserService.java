package com.keon.todoapp.service;

import com.keon.todoapp.model.User;
import com.keon.todoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 * Spring Security의 UserDetailsService를 구현하여 인증 시스템과 연동
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Security에서 사용자 인증 시 호출되는 메서드
     * 
     * @param username 사용자명
     * @return UserDetails 구현체 (User 엔티티)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 새로운 사용자를 등록합니다.
     * 
     * @param user 등록할 사용자 정보
     * @return 등록된 사용자 정보
     * @throws RuntimeException 이미 존재하는 사용자명인 경우
     */
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다: " + user.getUsername());
        }
        
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    /**
     * 사용자명으로 사용자를 조회합니다.
     * 
     * @param username 사용자명
     * @return 사용자 정보
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 사용자명 중복 여부를 확인합니다.
     * 
     * @param username 확인할 사용자명
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
