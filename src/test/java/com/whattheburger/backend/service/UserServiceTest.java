package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.UserCreateRequestDto;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.repository.UserRepository;
import com.whattheburger.backend.security.enums.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;

@Transactional
@ExtendWith(MockitoExtension.class) // Enable Mockito Annotations
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // used to inject private or protected fields during testing, used to directly access encapsulated field
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    @Test
    public void givenUser_whenJoin_thenPasswordIsEncrypted() throws Exception {
        UserCreateRequestDto userDto = new UserCreateRequestDto("Test", "User", "512-1234-5678", "12345", "test@email.com", "testing");

        User user = new User(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getPhoneNum(),
                userDto.getZipcode(),
                userDto.getEmail(),
                userDto.getPassword(),
                Role.USER
        );
        // thenAnswer: returns the index-th argument that was passed into the mocked method
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.join(userDto);

        Assertions.assertThat(savedUser.getPassword()).isNotEqualTo(user.getPassword());
    }

//    @Test
//    public void checkDuplicate() throws Exception {
//        User user1 = new User();
//        user1.setEmail("kim@gmail.com");
//        User user2 = new User();
//        user2.setEmail("kim@gmail.com");
//
//        userService.join(user1);
//        try {
//            userService.join(user2);
//        } catch(IllegalStateException e) {
//            return;
//        }
//    }
}