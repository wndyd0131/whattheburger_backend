package com.whattheburger.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattheburger.backend.controller.dto.UserCreateRequestDto;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.ProductService;
import com.whattheburger.backend.service.StoreProductService;
import com.whattheburger.backend.service.StoreService;
import com.whattheburger.backend.service.UserService;
import com.whattheburger.backend.service.dto.product.ProductCreateDto;
import com.whattheburger.backend.util.MockMultipartFile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class DataConfig {
    @Value("${seeding.user}")
    private Boolean userSeedingFlag;
    @Value("${seeding.store}")
    private Boolean storeSeedingFlag;
    @Value("${seeding.product}")
    private Boolean productSeedingFlag;
    @Value("${seeding.storeProduct}")
    private Boolean storeProductSeedingFlag;

    @Bean
    public CommandLineRunner seedRunner(
            UserService userService,
            ProductService productService,
            StoreProductService storeProductService,
            StoreService storeService
    ) throws IOException {
        return args -> {
            if (userSeedingFlag) {
                User user = userService.join(
                        UserCreateRequestDto
                                .builder()
                                .email("admin@gmail.com")
                                .firstName("Admin")
                                .lastName("Istrator")
                                .password("1234")
                                .phoneNum("5121234567")
                                .zipCode("12345")
                                .build()
                );
            }
            if (storeSeedingFlag) {
                System.out.println("Loading stores...");
                try {
                    ProcessBuilder pb = new ProcessBuilder("python3", "scripts/script.py"); // Assuming hello.py exists
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python Output: " + line);
                    }

                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Python Error: " + line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("Python script exited with code: " + exitCode);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Stores loaded successfully");
            }
            List<Store> stores = storeService.findAllStores();

            if (productSeedingFlag) {
                ObjectMapper objectMapper = new ObjectMapper();
                ClassPathResource classPathResource = new ClassPathResource("seeds/catalog.json");
                InputStream inputStream = classPathResource.getInputStream();
                List<ProductCreateDto> productCreateDtos = objectMapper.readValue(inputStream, new TypeReference<List<ProductCreateDto>>() {});
                log.info("Products loaded count: {}", productCreateDtos.size());

                for (int i = 0; i < productCreateDtos.size(); i++) {
                    ProductCreateDto productCreateDto = productCreateDtos.get(i);
                    productService.createProduct(productCreateDto);
                }
            }

            List<Product> products = productService.getAllProducts();

            if (storeProductSeedingFlag) {
                // Register products to stores
                List<Long> allStoreIds = stores.stream()
                        .map(Store::getId)
                        .toList();
                System.out.println("Registering products to stores...");
                products.stream()
                        .forEach(product -> {
                            storeProductService.registerProductToStores(product.getId(), allStoreIds);
                        });
                System.out.println("Products successfully registered");
            }
        };
    }
}
