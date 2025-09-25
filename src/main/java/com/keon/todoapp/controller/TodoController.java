package com.keon.todoapp.controller;

import com.keon.todoapp.model.Todo;
import com.keon.todoapp.model.User;
import com.keon.todoapp.service.TodoService;
import com.keon.todoapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 인증된 사용자의 Todo 관련 기능을 담당하는 컨트롤러
 * Todo CRUD 작업과 상태 변경 기능을 제공
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;

    /**
     * Todo 목록 페이지
     */
    @GetMapping("/todos")
    public String todoList(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Todo> todos = todoService.getTodosByUser(currentUser);

        // 통계 정보
        long completedCount = todoService.getCompletedCount(currentUser);
        long pendingCount = todoService.getPendingCount(currentUser);

        model.addAttribute("todos", todos);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("totalCount", todos.size());

        return "user/todos";
    }

    /**
     * Todo 추가 폼 페이지
     */
    @GetMapping("/todos/new")
    public String newTodoForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "user/add-todo";
    }

    /**
     * Todo 추가 처리
     */
    @PostMapping("/todos")
    public String createTodo(@ModelAttribute Todo todo,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            todoService.createTodo(todo, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Todo가 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/user/todos";
    }

    /**
     * Todo 수정 폼 페이지
     */
    @GetMapping("/todos/{id}/edit")
    public String editTodoForm(@PathVariable Long id,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            Todo todo = todoService.getTodoById(id, currentUser);
            model.addAttribute("todo", todo);
            return "user/edit-todo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo를 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/user/todos";
        }
    }

    /**
     * Todo 수정 처리
     */
    @PostMapping("/todos/{id}")
    public String updateTodo(@PathVariable Long id,
                            @ModelAttribute Todo todo,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            todoService.updateTodo(id, todo, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Todo가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/user/todos";
    }

    /**
     * Todo 삭제 처리
     */
    @PostMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            todoService.deleteTodo(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Todo가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/user/todos";
    }

    /**
     * Todo 완료 상태 토글
     */
    @PostMapping("/todos/{id}/toggle")
    public String toggleComplete(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            todoService.toggleComplete(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Todo 상태가 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/user/todos";
    }

    /**
     * 완료된 Todo만 보기
     */
    @GetMapping("/todos/completed")
    public String completedTodos(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Todo> completedTodos = todoService.getTodosByUserAndCompleted(currentUser, true);

        model.addAttribute("todos", completedTodos);
        model.addAttribute("filterType", "completed");
        model.addAttribute("pageTitle", "완료된 Todo");

        return "user/todos";
    }

    /**
     * 미완료 Todo만 보기
     */
    @GetMapping("/todos/pending")
    public String pendingTodos(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Todo> pendingTodos = todoService.getTodosByUserAndCompleted(currentUser, false);

        model.addAttribute("todos", pendingTodos);
        model.addAttribute("filterType", "pending");
        model.addAttribute("pageTitle", "미완료 Todo");

        return "user/todos";
    }
}
