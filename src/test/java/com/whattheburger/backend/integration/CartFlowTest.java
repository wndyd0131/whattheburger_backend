package com.whattheburger.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattheburger.backend.controller.dto.cart.CartCreateRequestDto;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.cart.CartSessionStorage;
import com.whattheburger.backend.domain.cart.CartValidator;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.integration.support.CartTestSupport;
import com.whattheburger.backend.integration.support.CatalogIntegrationFixture;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CartFlowTest extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CatalogIntegrationFixture catalog;

    @Autowired
    CartTestSupport cartTestSupport;

    @Autowired
    CartSessionStorage cartSessionStorage;

    @MockBean
    S3Service s3Service;

    Store store;
    StoreProduct storeProduct;

    @BeforeEach
    void setUp() {
        store = catalog.saveStore("Branch 1");
        storeProduct = catalog.saveMinimalStoreProduct(store);
    }

    @Nested
    class AddToCart {

        @Test
        void guestRequest_returns201AndSavesToCart() throws Exception {
            UUID guestId = UUID.randomUUID();
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.storeProductId").value(storeProduct.getId()))
                    .andExpect(jsonPath("$.productName").value("Burger"))
                    .andExpect(jsonPath("$.quantity").value(1));

            CartList cartList = cartSessionStorage.load(cartTestSupport.guestSessionKey(store.getId(), guestId))
                    .orElseThrow();
            assertThat(cartList.getCarts()).hasSize(1);
            assertThat(cartList.getCarts().get(0).getStoreProductId()).isEqualTo(storeProduct.getId());
        }

        @Test
        void authenticatedUserRequest_returns201AndSavesToCart() throws Exception {
            User user = cartTestSupport.saveUser(Role.USER);
            String accessToken = cartTestSupport.createAccessToken(user);
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 2);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.storeProductId").value(storeProduct.getId()))
                    .andExpect(jsonPath("$.productName").value("Burger"))
                    .andExpect(jsonPath("$.quantity").value(2));

            CartList cartList = cartSessionStorage.load(cartTestSupport.userSessionKey(store.getId(), user.getEmail()))
                    .orElseThrow();
            assertThat(cartList.getCarts()).hasSize(1);
            assertThat(cartList.getCarts().get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        void existingSession_addsProductToSameSession() throws Exception {
            UUID guestId = UUID.randomUUID();
            String sessionKey = cartTestSupport.guestSessionKey(store.getId(), guestId);
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            UUID firstSessionId = cartSessionStorage.load(sessionKey)
                    .orElseThrow()
                    .getSessionId();

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            CartList cartList = cartSessionStorage.load(sessionKey).orElseThrow();
            assertThat(cartList.getSessionId()).isEqualTo(firstSessionId);
            assertThat(cartList.getCarts()).hasSize(2);
        }

        @Test
        void noExistingSession_createsNewSession() throws Exception {
            UUID guestId = UUID.randomUUID();
            String sessionKey = cartTestSupport.guestSessionKey(store.getId(), guestId);
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            assertThat(cartSessionStorage.load(sessionKey)).isEmpty();

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            CartList cartList = cartSessionStorage.load(sessionKey).orElseThrow();
            assertThat(cartList.getSessionId()).isNotNull();
            assertThat(cartList.getCarts()).hasSize(1);
        }
    }

    @Nested
    class AddToCartErrors {

        @Test
        void whenStoreNotFound_returns404() throws Exception {
            UUID guestId = UUID.randomUUID();
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", 999_999L)
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void whenStoreIdMismatchStoreIdFromCartItem_returns400() throws Exception {
            Store otherStore = catalog.saveStore("Branch 2");
            UUID guestId = UUID.randomUUID();
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", otherStore.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void whenNoGuestAndNoUser_returns400() throws Exception {
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);
            String sessionKey = cartTestSupport.guestSessionKey(store.getId(), null);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            assertThat(cartSessionStorage.load(sessionKey)).isEmpty();
        }

        @Test
        void whenCartItemLimitExceeded_returns400() throws Exception {
            UUID guestId = UUID.randomUUID();
            String sessionKey = cartTestSupport.guestSessionKey(store.getId(), guestId);
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            for (int i = 0; i < CartValidator.MAX_CART_ITEMS; i++) {
                mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                                .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            CartList cartList = cartSessionStorage.load(sessionKey).orElseThrow();
            assertThat(cartList.getCarts()).hasSize(CartValidator.MAX_CART_ITEMS);
        }

        @Test
        void whenStoreProductNotFound_returns400() throws Exception {
            UUID guestId = UUID.randomUUID();
            String sessionKey = cartTestSupport.guestSessionKey(store.getId(), guestId);
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(999_999L, 1);

            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            assertThat(cartSessionStorage.load(sessionKey)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Merge Cart")
    class MergeCart {

        @Test
        void whenAuthenticated_mergesGuestCartIntoUserCart() throws Exception {
            StoreProduct guestProduct = storeProduct;
            StoreProduct userProduct = catalog.saveMinimalStoreProduct(store, "Fries");
            UUID guestId = UUID.randomUUID();
            User user = cartTestSupport.saveUser(Role.USER);
            String accessToken = cartTestSupport.createAccessToken(user);

            CartCreateRequestDto guestRequest = cartTestSupport.buildAddToCartRequest(guestProduct, 1);
            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(guestRequest)))
                    .andExpect(status().isCreated());

            CartCreateRequestDto userRequest = cartTestSupport.buildAddToCartRequest(userProduct, 1);
            mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(patch("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            CartList userCart = cartSessionStorage.load(
                            cartTestSupport.userSessionKey(store.getId(), user.getEmail()))
                    .orElseThrow();
            assertThat(userCart.getCarts()).hasSize(2);
            assertThat(userCart.getCarts())
                    .extracting(cart -> cart.getStoreProductId())
                    .containsExactlyInAnyOrder(guestProduct.getId(), userProduct.getId());

            CartList guestCart = cartSessionStorage.load(
                            cartTestSupport.guestSessionKey(store.getId(), guestId))
                    .orElseThrow();
            assertThat(guestCart.getCarts()).isEmpty();
        }

        @Test
        void whenMergedCartExceedsLimit_bothCartsUnchanged() throws Exception {
            UUID guestId = UUID.randomUUID();
            User user = cartTestSupport.saveUser(Role.USER);
            String accessToken = cartTestSupport.createAccessToken(user);
            String userSessionKey = cartTestSupport.userSessionKey(store.getId(), user.getEmail());
            String guestSessionKey = cartTestSupport.guestSessionKey(store.getId(), guestId);
            CartCreateRequestDto request = cartTestSupport.buildAddToCartRequest(storeProduct, 1);

            for (int i = 0; i < 15; i++) {
                mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }

            for (int i = 0; i < 6; i++) {
                mockMvc.perform(post("/api/v1/store/{storeId}/cart", store.getId())
                                .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }

            mockMvc.perform(patch("/api/v1/store/{storeId}/cart", store.getId())
                            .cookie(new jakarta.servlet.http.Cookie("guestId", guestId.toString()))
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            CartList userCart = cartSessionStorage.load(userSessionKey).orElseThrow();
            assertThat(userCart.getCarts()).hasSize(15);

            CartList guestCart = cartSessionStorage.load(guestSessionKey).orElseThrow();
            assertThat(guestCart.getCarts()).hasSize(6);
        }
    }
}
