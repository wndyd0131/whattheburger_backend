package com.whattheburger.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(
		locations = "classpath:application-integrationtest.properties"
)
class WhattheburgerProjectApplicationTests {

	@Test
	void contextLoads() {
	}

}
