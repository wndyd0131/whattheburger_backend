package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.domain.User;
import com.whataburger.whataburgerproject.repository.UserRepository;
import com.whataburger.whataburgerproject.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/users")
    public String check() {
        return "Jojo";
    }
    @PostMapping("/api/v1/users")
    public Long saveMember(@RequestBody User user) {
        Long id = userService.join(user);
        return id;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
        private int age;
    }

}
