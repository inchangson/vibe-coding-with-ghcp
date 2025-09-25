package com.keon.todoapp.repository;

import com.keon.todoapp.model.Todo;
import com.keon.todoapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TodoRepository 단위 테스트
 */
@DataJpaTest
@DisplayName("TodoRepository 테스트")
class TodoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoRepository todoRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("사용자별 Todo 조회")
    void findByUser_Success() {
        // given
        Todo todo1 = createTodo("Todo 1", "업무", "HIGH", false);
        Todo todo2 = createTodo("Todo 2", "개인", "LOW", true);
        entityManager.persistAndFlush(todo1);
        entityManager.persistAndFlush(todo2);

        // when
        List<Todo> result = todoRepository.findByUser(testUser);

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(todo -> todo.getUser().equals(testUser)));
    }

    @Test
    @DisplayName("완료 상태별 Todo 조회")
    void findByUserAndCompleted_Success() {
        // given
        Todo completedTodo = createTodo("Completed Todo", "업무", "HIGH", true);
        Todo pendingTodo = createTodo("Pending Todo", "개인", "LOW", false);
        entityManager.persistAndFlush(completedTodo);
        entityManager.persistAndFlush(pendingTodo);

        // when - 완료된 Todo 조회
        List<Todo> completedResult = todoRepository.findByUserAndCompleted(testUser, true);
        List<Todo> pendingResult = todoRepository.findByUserAndCompleted(testUser, false);

        // then
        assertEquals(1, completedResult.size());
        assertTrue(completedResult.get(0).isCompleted());
        assertEquals("Completed Todo", completedResult.get(0).getTitle());

        assertEquals(1, pendingResult.size());
        assertFalse(pendingResult.get(0).isCompleted());
        assertEquals("Pending Todo", pendingResult.get(0).getTitle());
    }

    @Test
    @DisplayName("생성일 기준 내림차순 정렬 조회")
    void findByUserOrderByCreatedDateDesc_Success() {
        // given
        Todo oldTodo = createTodo("Old Todo", "업무", "HIGH", false);
        oldTodo.setCreatedDate(LocalDate.now().minusDays(2));

        Todo newTodo = createTodo("New Todo", "개인", "LOW", false);
        newTodo.setCreatedDate(LocalDate.now());

        entityManager.persistAndFlush(oldTodo);
        entityManager.persistAndFlush(newTodo);

        // when
        List<Todo> result = todoRepository.findByUserOrderByCreatedDateDesc(testUser);

        // then
        assertEquals(2, result.size());
        assertEquals("New Todo", result.get(0).getTitle()); // 최신이 먼저
        assertEquals("Old Todo", result.get(1).getTitle());
    }

    @Test
    @DisplayName("완료 상태별 개수 조회")
    void countByUserAndCompleted_Success() {
        // given
        createAndPersistTodos();

        // when
        long completedCount = todoRepository.countByUserAndCompleted(testUser, true);
        long pendingCount = todoRepository.countByUserAndCompleted(testUser, false);

        // then
        assertEquals(2, completedCount);
        assertEquals(3, pendingCount);
    }

    @Test
    @DisplayName("우선순위별 Todo 조회")
    void findByUserAndPriority_Success() {
        // given
        Todo highTodo = createTodo("High Priority", "업무", "HIGH", false);
        Todo mediumTodo = createTodo("Medium Priority", "개인", "MEDIUM", false);
        Todo lowTodo = createTodo("Low Priority", "학습", "LOW", false);

        entityManager.persistAndFlush(highTodo);
        entityManager.persistAndFlush(mediumTodo);
        entityManager.persistAndFlush(lowTodo);

        // when
        List<Todo> highPriorityTodos = todoRepository.findByUserAndPriority(testUser, "HIGH");
        List<Todo> mediumPriorityTodos = todoRepository.findByUserAndPriority(testUser, "MEDIUM");
        List<Todo> lowPriorityTodos = todoRepository.findByUserAndPriority(testUser, "LOW");

        // then
        assertEquals(1, highPriorityTodos.size());
        assertEquals("HIGH", highPriorityTodos.get(0).getPriority());

        assertEquals(1, mediumPriorityTodos.size());
        assertEquals("MEDIUM", mediumPriorityTodos.get(0).getPriority());

        assertEquals(1, lowPriorityTodos.size());
        assertEquals("LOW", lowPriorityTodos.get(0).getPriority());
    }

    @Test
    @DisplayName("카테고리별 Todo 조회")
    void findByUserAndCategory_Success() {
        // given
        Todo workTodo1 = createTodo("Work Todo 1", "업무", "HIGH", false);
        Todo workTodo2 = createTodo("Work Todo 2", "업무", "MEDIUM", true);
        Todo personalTodo = createTodo("Personal Todo", "개인", "LOW", false);

        entityManager.persistAndFlush(workTodo1);
        entityManager.persistAndFlush(workTodo2);
        entityManager.persistAndFlush(personalTodo);

        // when
        List<Todo> workTodos = todoRepository.findByUserAndCategory(testUser, "업무");
        List<Todo> personalTodos = todoRepository.findByUserAndCategory(testUser, "개인");

        // then
        assertEquals(2, workTodos.size());
        assertTrue(workTodos.stream().allMatch(todo -> "업무".equals(todo.getCategory())));

        assertEquals(1, personalTodos.size());
        assertEquals("개인", personalTodos.get(0).getCategory());
    }

    private Todo createTodo(String title, String category, String priority, boolean completed) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription("Description for " + title);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setCompleted(completed);
        todo.setUser(testUser);
        todo.setCreatedDate(LocalDate.now());
        return todo;
    }

    private void createAndPersistTodos() {
        // 완료된 Todo 2개
        entityManager.persistAndFlush(createTodo("Completed 1", "업무", "HIGH", true));
        entityManager.persistAndFlush(createTodo("Completed 2", "개인", "LOW", true));

        // 미완료 Todo 3개
        entityManager.persistAndFlush(createTodo("Pending 1", "학습", "MEDIUM", false));
        entityManager.persistAndFlush(createTodo("Pending 2", "운동", "HIGH", false));
        entityManager.persistAndFlush(createTodo("Pending 3", "취미", "LOW", false));
    }
}
