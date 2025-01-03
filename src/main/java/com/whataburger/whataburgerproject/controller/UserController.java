package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.UserCreateResponseDto;
import com.whataburger.whataburgerproject.controller.dto.UserDto;
import com.whataburger.whataburgerproject.controller.dto.UserReadRequestDto;
import com.whataburger.whataburgerproject.controller.dto.UserReadResponseDTO;
import com.whataburger.whataburgerproject.domain.User;
import com.whataburger.whataburgerproject.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/users")
    public ResponseEntity<UserReadResponseDTO> getUser(@RequestBody UserReadRequestDto userReadRequestDto) {
        // service
        User user = userService.findUser(userReadRequestDto.getUserId());
        return new ResponseEntity<>(
                new UserReadResponseDTO(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNum(),
                        user.getZipcode(),
                        user.getEmail()),
                HttpStatusCode.valueOf(200)
        );
    }
    @PostMapping("/api/v1/users")
    public ResponseEntity<UserCreateResponseDto> saveUser(@RequestBody UserDto userDto) {
        Long userId = userService.join(userDto.toEntity());
        return new ResponseEntity<>(
                new UserCreateResponseDto(userId),
                HttpStatusCode.valueOf(201)
        );
    }

    @Data
    static class CreateMemberRequest {
        private String name;
        private int age;
    }

}
