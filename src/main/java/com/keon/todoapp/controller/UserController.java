package com.keon.todoapp.controller;

import com.keon.todoapp.model.User;
import com.keon.todoapp.service.TodoService;
import com.keon.todoapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 사용자 관련 기능을 담당하는 컨트롤러
 * 프로필 페이지와 사용자 통계 정보를 제공
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TodoService todoService;

    /**
     * 사용자 프로필 페이지
     */
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());

        // 사용자 통계 정보
        long totalTodos = todoService.getTodosByUser(currentUser).size();
        long completedCount = todoService.getCompletedCount(currentUser);
        long pendingCount = todoService.getPendingCount(currentUser);

        // 완료율 계산
        double completionRate = totalTodos > 0 ? (double) completedCount / totalTodos * 100 : 0;

        model.addAttribute("user", currentUser);
        model.addAttribute("totalTodos", totalTodos);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completionRate", Math.round(completionRate * 100.0) / 100.0);

        return "user/profile";
    }

    /**
     * 사용자 대시보드 (통계 정보)
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());

        // 상세 통계 정보
        long totalTodos = todoService.getTodosByUser(currentUser).size();
        long completedCount = todoService.getCompletedCount(currentUser);
        long pendingCount = todoService.getPendingCount(currentUser);

        // 우선순위별 통계
        long highPriorityCount = todoService.getTodosByPriority(currentUser, "HIGH").size();
        long mediumPriorityCount = todoService.getTodosByPriority(currentUser, "MEDIUM").size();
        long lowPriorityCount = todoService.getTodosByPriority(currentUser, "LOW").size();

        model.addAttribute("user", currentUser);
        model.addAttribute("totalTodos", totalTodos);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("highPriorityCount", highPriorityCount);
        model.addAttribute("mediumPriorityCount", mediumPriorityCount);
        model.addAttribute("lowPriorityCount", lowPriorityCount);

        return "user/dashboard";
    }
}
