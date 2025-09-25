package com.keon.todoapp.service;

import com.keon.todoapp.model.Todo;
import com.keon.todoapp.model.User;
import com.keon.todoapp.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Todo 관련 비즈니스 로직을 처리하는 서비스
 * CRUD 기능과 사용자별 Todo 관리 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    /**
     * 새로운 Todo를 생성합니다.
     *
     * @param todo 생성할 Todo 정보
     * @param user Todo를 소유할 사용자
     * @return 생성된 Todo
     */
    @Transactional
    public Todo createTodo(Todo todo, User user) {
        todo.setUser(user);
        todo.setCreatedDate(LocalDate.now());
        return todoRepository.save(todo);
    }

    /**
     * 특정 사용자의 모든 Todo를 조회합니다.
     *
     * @param user 사용자
     * @return 사용자의 Todo 목록 (생성일 기준 내림차순)
     */
    public List<Todo> getTodosByUser(User user) {
        return todoRepository.findByUserOrderByCreatedDateDesc(user);
    }

    /**
     * 특정 사용자의 완료/미완료 Todo를 조회합니다.
     *
     * @param user 사용자
     * @param completed 완료 상태
     * @return 필터링된 Todo 목록
     */
    public List<Todo> getTodosByUserAndCompleted(User user, boolean completed) {
        return todoRepository.findByUserAndCompleted(user, completed);
    }

    /**
     * Todo ID로 Todo를 조회합니다.
     *
     * @param id Todo ID
     * @param user 소유자 확인을 위한 사용자
     * @return Todo 정보
     * @throws RuntimeException Todo를 찾을 수 없거나 소유자가 다른 경우
     */
    public Todo getTodoById(Long id, User user) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo를 찾을 수 없습니다: " + id));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 Todo에 접근할 권한이 없습니다.");
        }

        return todo;
    }

    /**
     * Todo를 수정합니다.
     *
     * @param id 수정할 Todo ID
     * @param updatedTodo 수정할 정보
     * @param user 소유자 확인을 위한 사용자
     * @return 수정된 Todo
     */
    @Transactional
    public Todo updateTodo(Long id, Todo updatedTodo, User user) {
        Todo existingTodo = getTodoById(id, user);

        existingTodo.setTitle(updatedTodo.getTitle());
        existingTodo.setDescription(updatedTodo.getDescription());
        existingTodo.setCategory(updatedTodo.getCategory());
        existingTodo.setPriority(updatedTodo.getPriority());
        existingTodo.setDueDate(updatedTodo.getDueDate());

        return todoRepository.save(existingTodo);
    }

    /**
     * Todo를 삭제합니다.
     *
     * @param id 삭제할 Todo ID
     * @param user 소유자 확인을 위한 사용자
     */
    @Transactional
    public void deleteTodo(Long id, User user) {
        Todo todo = getTodoById(id, user);
        todoRepository.delete(todo);
    }

    /**
     * Todo의 완료 상태를 토글합니다.
     *
     * @param id Todo ID
     * @param user 소유자 확인을 위한 사용자
     * @return 상태가 변경된 Todo
     */
    @Transactional
    public Todo toggleComplete(Long id, User user) {
        Todo todo = getTodoById(id, user);
        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }

    /**
     * 특정 사용자의 완료된 Todo 개수를 조회합니다.
     *
     * @param user 사용자
     * @return 완료된 Todo 개수
     */
    public long getCompletedCount(User user) {
        return todoRepository.countByUserAndCompleted(user, true);
    }

    /**
     * 특정 사용자의 미완료 Todo 개수를 조회합니다.
     *
     * @param user 사용자
     * @return 미완료 Todo 개수
     */
    public long getPendingCount(User user) {
        return todoRepository.countByUserAndCompleted(user, false);
    }

    /**
     * 특정 사용자의 우선순위별 Todo를 조회합니다.
     *
     * @param user 사용자
     * @param priority 우선순위
     * @return 우선순위별 Todo 목록
     */
    public List<Todo> getTodosByPriority(User user, String priority) {
        return todoRepository.findByUserAndPriority(user, priority);
    }

    /**
     * 특정 사용자의 카테고리별 Todo를 조회합니다.
     *
     * @param user 사용자
     * @param category 카테고리
     * @return 카테고리별 Todo 목록
     */
    public List<Todo> getTodosByCategory(User user, String category) {
        return todoRepository.findByUserAndCategory(user, category);
    }
}
