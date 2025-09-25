package com.keon.todoapp.service;

import com.keon.todoapp.model.Todo;
import com.keon.todoapp.model.User;
import com.keon.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TodoService 단위 테스트
 */
@DisplayName("TodoService 테스트")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setCategory("업무");
        testTodo.setPriority("HIGH");
        testTodo.setCompleted(false);
        testTodo.setUser(testUser);
        testTodo.setCreatedDate(LocalDate.now());
    }

    @Test
    @DisplayName("Todo 생성 - 성공")
    void createTodo_Success() {
        // given
        Todo newTodo = new Todo();
        newTodo.setTitle("New Todo");
        newTodo.setDescription("New Description");

        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // when
        Todo result = todoService.createTodo(newTodo, testUser);

        // then
        assertNotNull(result);
        assertEquals(testUser, newTodo.getUser());
        assertEquals(LocalDate.now(), newTodo.getCreatedDate());
        verify(todoRepository).save(newTodo);
    }

    @Test
    @DisplayName("사용자별 Todo 조회 - 성공")
    void getTodosByUser_Success() {
        // given
        List<Todo> todoList = Arrays.asList(testTodo);
        when(todoRepository.findByUserOrderByCreatedDateDesc(testUser)).thenReturn(todoList);

        // when
        List<Todo> result = todoService.getTodosByUser(testUser);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo, result.get(0));
        verify(todoRepository).findByUserOrderByCreatedDateDesc(testUser);
    }

    @Test
    @DisplayName("완료 상태별 Todo 조회 - 성공")
    void getTodosByUserAndCompleted_Success() {
        // given
        List<Todo> completedTodos = Arrays.asList(testTodo);
        when(todoRepository.findByUserAndCompleted(testUser, true)).thenReturn(completedTodos);

        // when
        List<Todo> result = todoService.getTodosByUserAndCompleted(testUser, true);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(todoRepository).findByUserAndCompleted(testUser, true);
    }

    @Test
    @DisplayName("Todo ID로 조회 - 성공")
    void getTodoById_Success() {
        // given
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // when
        Todo result = todoService.getTodoById(1L, testUser);

        // then
        assertNotNull(result);
        assertEquals(testTodo, result);
        verify(todoRepository).findById(1L);
    }

    @Test
    @DisplayName("Todo ID로 조회 - Todo 없음")
    void getTodoById_TodoNotFound() {
        // given
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            todoService.getTodoById(99L, testUser);
        });

        assertTrue(exception.getMessage().contains("Todo를 찾을 수 없습니다"));
        verify(todoRepository).findById(99L);
    }

    @Test
    @DisplayName("Todo ID로 조회 - 권한 없음")
    void getTodoById_AccessDenied() {
        // given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            todoService.getTodoById(1L, otherUser);
        });

        assertTrue(exception.getMessage().contains("접근할 권한이 없습니다"));
        verify(todoRepository).findById(1L);
    }

    @Test
    @DisplayName("Todo 수정 - 성공")
    void updateTodo_Success() {
        // given
        Todo updatedTodo = new Todo();
        updatedTodo.setTitle("Updated Title");
        updatedTodo.setDescription("Updated Description");
        updatedTodo.setCategory("개인");
        updatedTodo.setPriority("LOW");

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // when
        Todo result = todoService.updateTodo(1L, updatedTodo, testUser);

        // then
        assertNotNull(result);
        assertEquals("Updated Title", testTodo.getTitle());
        assertEquals("Updated Description", testTodo.getDescription());
        assertEquals("개인", testTodo.getCategory());
        assertEquals("LOW", testTodo.getPriority());
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(testTodo);
    }

    @Test
    @DisplayName("Todo 삭제 - 성공")
    void deleteTodo_Success() {
        // given
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // when
        todoService.deleteTodo(1L, testUser);

        // then
        verify(todoRepository).findById(1L);
        verify(todoRepository).delete(testTodo);
    }

    @Test
    @DisplayName("Todo 완료 상태 토글 - 성공")
    void toggleComplete_Success() {
        // given
        testTodo.setCompleted(false);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // when
        Todo result = todoService.toggleComplete(1L, testUser);

        // then
        assertNotNull(result);
        assertTrue(testTodo.isCompleted());
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(testTodo);
    }

    @Test
    @DisplayName("완료된 Todo 개수 조회 - 성공")
    void getCompletedCount_Success() {
        // given
        when(todoRepository.countByUserAndCompleted(testUser, true)).thenReturn(5L);

        // when
        long result = todoService.getCompletedCount(testUser);

        // then
        assertEquals(5L, result);
        verify(todoRepository).countByUserAndCompleted(testUser, true);
    }

    @Test
    @DisplayName("미완료 Todo 개수 조회 - 성공")
    void getPendingCount_Success() {
        // given
        when(todoRepository.countByUserAndCompleted(testUser, false)).thenReturn(3L);

        // when
        long result = todoService.getPendingCount(testUser);

        // then
        assertEquals(3L, result);
        verify(todoRepository).countByUserAndCompleted(testUser, false);
    }

    @Test
    @DisplayName("우선순위별 Todo 조회 - 성공")
    void getTodosByPriority_Success() {
        // given
        List<Todo> highPriorityTodos = Arrays.asList(testTodo);
        when(todoRepository.findByUserAndPriority(testUser, "HIGH")).thenReturn(highPriorityTodos);

        // when
        List<Todo> result = todoService.getTodosByPriority(testUser, "HIGH");

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(todoRepository).findByUserAndPriority(testUser, "HIGH");
    }

    @Test
    @DisplayName("카테고리별 Todo 조회 - 성공")
    void getTodosByCategory_Success() {
        // given
        List<Todo> workTodos = Arrays.asList(testTodo);
        when(todoRepository.findByUserAndCategory(testUser, "업무")).thenReturn(workTodos);

        // when
        List<Todo> result = todoService.getTodosByCategory(testUser, "업무");

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(todoRepository).findByUserAndCategory(testUser, "업무");
    }
}
