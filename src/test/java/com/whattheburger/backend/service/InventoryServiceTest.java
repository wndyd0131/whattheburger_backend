package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.repository.ProductOptionOptionQuantityRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    private static final Long STORE_ID = 1L;
    private static final Long INGREDIENT_ID = 300L;
    private static final Long INGREDIENT_ID_2 = 301L;
    private static final Long PRODUCT_OPTION_ID = 100L;
    private static final Long PRODUCT_OPTION_ID_2 = 101L;

    @Mock
    private StoreInventoryRepository storeInventoryRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.builder().id(STORE_ID).build();
    }

    @Test
    void givenNullOrder_whenDeductStock_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.deductStock(null));

        verifyNoInteractions(storeInventoryRepository, productOptionRepository, productOptionOptionQuantityRepository);
    }

    @Test
    void givenUnpaidOrder_whenDeductStock_thenThrowsIllegalArgumentException() {
        Order order = buildUnpaidOrder(List.of());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.deductStock(order));

        verifyNoInteractions(storeInventoryRepository, productOptionRepository, productOptionOptionQuantityRepository);
    }

    @Test
    void givenPaidOrderAndInsufficientStock_whenDeductStock_thenThrowsInsufficientOptionStockException() {
        int amount = 6;
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3)
        ));
        ProductOption productOption = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        StoreInventory storeInventory = buildStoreInventory(INGREDIENT_ID, 5);

        when(productOptionRepository.findAllWithOptionIngredientsByIdIn(anyCollection()))
                .thenReturn(List.of(productOption));
        when(storeInventoryRepository.findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID))
                .thenReturn(Optional.of(storeInventory));
        when(storeInventoryRepository.deductStockAtomic(STORE_ID, INGREDIENT_ID, amount))
                .thenReturn(0);

        assertThrows(InsufficientOptionStockException.class, () -> inventoryService.deductStock(order));

        verify(storeInventoryRepository, times(1))
                .deductStockAtomic(STORE_ID, INGREDIENT_ID, amount);
    }

    @Test
    void givenPaidOrderWithSameIngredientFromMultipleOptions_whenDeductStock_thenBatchLockFetchAndDeductSummedAmount() {
        int summedAmount = 10;
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3),
                countableOrderProduct(1, 1, PRODUCT_OPTION_ID_2, 4)
        ));
        ProductOption productOption1 = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        ProductOption productOption2 = buildProductOption(PRODUCT_OPTION_ID_2, INGREDIENT_ID, 4);
        StoreInventory storeInventory = buildStoreInventory(INGREDIENT_ID, 100);

        when(productOptionRepository.findAllWithOptionIngredientsByIdIn(anyCollection()))
                .thenReturn(List.of(productOption1, productOption2));
        when(storeInventoryRepository.findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID))
                .thenReturn(Optional.of(storeInventory));
        when(storeInventoryRepository.deductStockAtomic(STORE_ID, INGREDIENT_ID, summedAmount))
                .thenReturn(1);

        assertDoesNotThrow(() -> inventoryService.deductStock(order));

        verify(storeInventoryRepository, times(1))
                .deductStockAtomic(STORE_ID, INGREDIENT_ID, summedAmount);
    }

    @Test
    void givenPaidOrderWithEnoughStock_whenDeductStock_thenBatchLockFetchForAllIngredientsWithoutException() {
        int amount1 = 6;
        int amount2 = 8;
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3),
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID_2, 4)
        ));
        ProductOption productOption1 = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        ProductOption productOption2 = buildProductOption(PRODUCT_OPTION_ID_2, INGREDIENT_ID_2, 4);
        StoreInventory storeInventory1 = buildStoreInventory(INGREDIENT_ID, 100);
        StoreInventory storeInventory2 = buildStoreInventory(INGREDIENT_ID_2, 100);

        when(productOptionRepository.findAllWithOptionIngredientsByIdIn(anyCollection()))
                .thenReturn(List.of(productOption1, productOption2));
        when(storeInventoryRepository.findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID))
                .thenReturn(Optional.of(storeInventory1));
        when(storeInventoryRepository.findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID_2))
                .thenReturn(Optional.of(storeInventory2));
        when(storeInventoryRepository.deductStockAtomic(STORE_ID, INGREDIENT_ID, amount1))
                .thenReturn(1);
        when(storeInventoryRepository.deductStockAtomic(STORE_ID, INGREDIENT_ID_2, amount2))
                .thenReturn(1);

        assertDoesNotThrow(() -> inventoryService.deductStock(order));

        verify(storeInventoryRepository, times(1))
                .deductStockAtomic(STORE_ID, INGREDIENT_ID, amount1);
        verify(storeInventoryRepository, times(1))
                .deductStockAtomic(STORE_ID, INGREDIENT_ID_2, amount2);
    }

    private Order buildPaidOrder(List<OrderProduct> orderProducts) {
        return Order.builder()
                .store(store)
                .paymentStatus(PaymentStatus.PAID)
                .orderProducts(new ArrayList<>(orderProducts))
                .build();
    }

    private Order buildUnpaidOrder(List<OrderProduct> orderProducts) {
        return Order.builder()
                .store(store)
                .paymentStatus(PaymentStatus.UNPAID)
                .orderProducts(new ArrayList<>(orderProducts))
                .build();
    }

    private OrderProduct countableOrderProduct(
            int productQuantity,
            int optionQuantity,
            long productOptionId,
            int requiredQuantity
    ) {
        OrderProductOption orderProductOption = OrderProductOption.builder()
                .productOptionId(productOptionId)
                .countType(CountType.COUNTABLE)
                .quantity(optionQuantity)
                .build();

        OrderCustomRule orderCustomRule = OrderCustomRule.builder()
                .customRuleId(200L)
                .name("Custom Rule")
                .orderProductOptions(new ArrayList<>(List.of(orderProductOption)))
                .build();
        orderProductOption.assignOrderCustomRule(orderCustomRule);

        OrderProduct orderProduct = OrderProduct.builder()
                .storeProductId(42L)
                .quantity(productQuantity)
                .name("Burger")
                .orderCustomRules(new ArrayList<>(List.of(orderCustomRule)))
                .build();
        orderCustomRule.assignOrderProduct(orderProduct);

        return orderProduct;
    }

    private ProductOption buildProductOption(long productOptionId, long ingredientId, int requiredQuantity) {
        Ingredient ingredient = Ingredient.builder().id(ingredientId).build();
        OptionIngredient optionIngredient = OptionIngredient.builder()
                .id(1L)
                .ingredient(ingredient)
                .requiredQuantity(requiredQuantity)
                .build();
        Option option = Option.builder()
                .id(1L)
                .name("Cheese")
                .optionIngredients(new ArrayList<>(List.of(optionIngredient)))
                .build();
        return ProductOption.builder()
                .id(productOptionId)
                .countType(CountType.COUNTABLE)
                .option(option)
                .build();
    }

    private StoreInventory buildStoreInventory(long ingredientId, int currentStock) {
        Ingredient ingredient = Ingredient.builder().id(ingredientId).build();
        return StoreInventory.builder()
                .id(ingredientId)
                .currentStock(currentStock)
                .store(store)
                .ingredient(ingredient)
                .build();
    }
}
