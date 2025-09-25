package com.keon.todoapp.repository;

import com.keon.todoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 정보에 대한 데이터 접근을 담당하는 리포지토리
 * Spring Data JPA를 사용하여 기본 CRUD 기능과 커스텀 쿼리 메서드를 제공
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명으로 사용자를 조회합니다.
     *
     * @param username 사용자명
     * @return 사용자 정보 (Optional로 감싸서 반환)
     */
    Optional<User> findByUsername(String username);

    /**
     * 사용자명 존재 여부를 확인합니다.
     *
     * @param username 사용자명
     * @return 존재 여부
     */
    boolean existsByUsername(String username);
}
