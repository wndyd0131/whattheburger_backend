package com.whattheburger.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenInvalidJwtProvided_thenReturn401() throws Exception {
        mockMvc.perform(
                get("/api/v1/users")
                .header("Authorization", "Bearer invalid.jwt.token")
        ).andExpect(status().isUnauthorized());
    }
}
