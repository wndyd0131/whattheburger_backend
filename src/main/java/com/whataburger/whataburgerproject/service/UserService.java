package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.User;
import com.whataburger.whataburgerproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // optimization
@RequiredArgsConstructor // auto create constructor for field with final
public class UserService {

//    @Autowired // userRepository will be fixed, so it's not flexible
    private final UserRepository userRepository; // final causes compile error if there's no injection in constructor

//    @Autowired // allows more flexible change for userRepository (ex. for testing, mock injection), but it's setter so not safe
//    public setUserRepository(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

//    @Autowired // flexible + safe
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    @Transactional
    public Long join(User user) {
        validateDuplicateEmail(user);
        userRepository.save(user);
        return user.getId();
    }

    private void validateDuplicateEmail(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new IllegalArgumentException("The user already exists");
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("There is no such user"));
    }
}
