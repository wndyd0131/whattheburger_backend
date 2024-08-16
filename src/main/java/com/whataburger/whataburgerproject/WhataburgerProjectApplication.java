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
public class WhataburgerProjectApplication implements CommandLineRunner {

	public static void main(String[] args) {

//		SpringApplication.run(WhataburgerProjectApplication.class, args);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPersistenceUnit");
		EntityManager em = emf.createEntityManager();
		System.out.println("===");

		EntityTransaction tx = em.getTransaction();
		System.out.println("===");

		tx.begin();
		System.out.println("===");


		try {
			Product product1 = new Product();
			product1.setName("Whataburger");
			product1.setPrice(7);
			em.persist(product1);
			System.out.println("===");
			tx.commit();
			System.out.println("===");
		}
		catch (Exception e){
			tx.rollback();
		}
		finally {
			em.close();
		}
	}

	@Override
	public void run(String... args) throws Exception {

	}
}
