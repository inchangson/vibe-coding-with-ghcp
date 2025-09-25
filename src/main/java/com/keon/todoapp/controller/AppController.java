package com.keon.todoapp.controller;

import com.keon.todoapp.model.User;
import com.keon.todoapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 공개 페이지를 담당하는 컨트롤러
 * 홈페이지, 로그인, 회원가입, About 페이지 등을 처리
 */
@Controller
@RequiredArgsConstructor
public class AppController {

    private final UserService userService;

    /**
     * 홈페이지
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "사용자명 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "성공적으로 로그아웃되었습니다.");
        }
        return "login";
    }

    /**
     * 회원가입 페이지 (GET)
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * 회원가입 처리 (POST)
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                              RedirectAttributes redirectAttributes) {
        try {
            // 사용자명 중복 확인
            if (userService.isUsernameExists(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "이미 사용 중인 사용자명입니다.");
                return "redirect:/register";
            }

            // 비밀번호 검증 (최소 4자 이상)
            if (user.getPassword() == null || user.getPassword().length() < 4) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "비밀번호는 최소 4자 이상이어야 합니다.");
                return "redirect:/register";
            }

            // 사용자 등록
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                "회원가입이 완료되었습니다. 로그인해주세요.");

            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * About 페이지
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
