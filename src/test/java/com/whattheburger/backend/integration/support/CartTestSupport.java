package com.whattheburger.backend.integration.support;

import com.whattheburger.backend.controller.dto.cart.CartCreateRequestDto;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.repository.UserRepository;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class CartTestSupport {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenUtil jwtTokenUtil;

    public CartCreateRequestDto buildAddToCartRequest(StoreProduct storeProduct, int quantity) {
        return buildAddToCartRequest(storeProduct.getId(), quantity);
    }

    public CartCreateRequestDto buildAddToCartRequest(Long storeProductId, int quantity) {
        return CartCreateRequestDto.builder()
                .storeProductId(storeProductId)
                .quantity(quantity)
                .customRuleRequests(Collections.emptyList())
                .build();
    }

    public User saveUser(Role role) {
        String uniqueEmail = "cart-test-" + UUID.randomUUID() + "@example.com";
        User user = new User(
                "Test",
                "User",
                "512-123-4567",
                "78701",
                uniqueEmail,
                passwordEncoder.encode("password123"),
                role
        );
        return userRepository.save(user);
    }

    public String createAccessToken(User user) {
        return jwtTokenUtil.createAccessToken(user.getEmail());
    }

    public String guestSessionKey(Long storeId, UUID guestId) {
        return "cart:store:" + storeId + ":" + guestId;
    }

    public String userSessionKey(Long storeId, String email) {
        return "cart:store:" + storeId + ":" + email;
    }
}
