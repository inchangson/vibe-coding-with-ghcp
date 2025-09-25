package com.keon.todoapp.repository;

import com.keon.todoapp.model.Todo;
import com.keon.todoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Todo 항목에 대한 데이터 접근을 담당하는 리포지토리
 * 사용자별 Todo 관리와 다양한 필터링 기능을 제공
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * 특정 사용자의 모든 Todo를 조회합니다.
     *
     * @param user 사용자 정보
     * @return 사용자의 Todo 목록
     */
    List<Todo> findByUser(User user);

    /**
     * 특정 사용자의 완료/미완료 Todo를 조회합니다.
     *
     * @param user 사용자 정보
     * @param completed 완료 상태
     * @return 필터링된 Todo 목록
     */
    List<Todo> findByUserAndCompleted(User user, boolean completed);

    /**
     * 특정 사용자의 Todo를 생성일 기준 내림차순으로 조회합니다.
     *
     * @param user 사용자 정보
     * @return 날짜순으로 정렬된 Todo 목록
     */
    List<Todo> findByUserOrderByCreatedDateDesc(User user);

    /**
     * 특정 사용자의 완료/미완료 Todo 개수를 조회합니다.
     *
     * @param user 사용자 정보
     * @param completed 완료 상태
     * @return Todo 개수
     */
    long countByUserAndCompleted(User user, boolean completed);

    /**
     * 특정 사용자의 우선순위별 Todo를 조회합니다.
     *
     * @param user 사용자 정보
     * @param priority 우선순위 (HIGH, MEDIUM, LOW)
     * @return 우선순위별 Todo 목록
     */
    List<Todo> findByUserAndPriority(User user, String priority);

    /**
     * 특정 사용자의 카테고리별 Todo를 조회합니다.
     *
     * @param user 사용자 정보
     * @param category 카테고리
     * @return 카테고리별 Todo 목록
     */
    List<Todo> findByUserAndCategory(User user, String category);
}
