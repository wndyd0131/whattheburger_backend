package com.whataburger.whataburgerproject;

import com.whataburger.whataburgerproject.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WhataburgerProjectApplication {

	public static void main(String[] args) {

		SpringApplication.run(WhataburgerProjectApplication.class, args);
	}
}
