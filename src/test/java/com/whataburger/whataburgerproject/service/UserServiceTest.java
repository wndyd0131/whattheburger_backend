package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.User;
import com.whataburger.whataburgerproject.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UserServiceTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;


    @Test
    public void register() throws Exception {
//        // given
//        User user = new User();
//        user.setEmail("kim@gmail.com");
//        // when
//        Long userId = userService.join(user);
//        User regiUser = userRepository.findUser(userId);
//        // then
//        Assertions.assertThat(regiUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void checkDuplicate() throws Exception {
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
    }
}