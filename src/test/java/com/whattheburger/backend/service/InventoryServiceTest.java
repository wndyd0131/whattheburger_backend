package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.inventory.InventoryRequirementCalculator;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.repository.ProductOptionOptionQuantityRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.StoreInventoryBatchRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private StoreInventoryBatchRepository storeInventoryBatchRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;

    private InventoryService inventoryService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.builder().id(STORE_ID).build();
        inventoryService = new InventoryService(
                storeInventoryRepository,
                storeInventoryBatchRepository,
                productOptionRepository,
                productOptionOptionQuantityRepository,
                new InventoryRequirementCalculator()
        );
    }

    @Test
    void givenNullOrder_whenDeductStock_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.deductStock(null));

        verifyNoInteractions(storeInventoryRepository, storeInventoryBatchRepository,
                productOptionRepository, productOptionOptionQuantityRepository);
    }

    @Test
    void givenUnpaidOrder_whenDeductStock_thenThrowsIllegalArgumentException() {
        Order order = buildUnpaidOrder(List.of());

        assertThrows(IllegalArgumentException.class, () -> inventoryService.deductStock(order));

        verifyNoInteractions(storeInventoryRepository, storeInventoryBatchRepository,
                productOptionRepository, productOptionOptionQuantityRepository);
    }

    @Test
    void givenPaidOrderWithEnoughStock_whenDeductStock_thenSendsSingleBatchWithSortedIds() {
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID_2, 4),
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3)
        ));
        ProductOption productOption1 = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        ProductOption productOption2 = buildProductOption(PRODUCT_OPTION_ID_2, INGREDIENT_ID_2, 4);

        when(productOptionRepository.findAllWithOptionByIdIn(anyCollection()))
                .thenReturn(List.of(productOption1, productOption2));
        when(storeInventoryBatchRepository.deductStockAtomicBatch(eq(STORE_ID), anyList(), anyMap()))
                .thenReturn(new int[]{1, 1});

        assertDoesNotThrow(() -> inventoryService.deductStock(order));

        verify(storeInventoryBatchRepository, times(1)).deductStockAtomicBatch(
                STORE_ID,
                List.of(INGREDIENT_ID, INGREDIENT_ID_2),
                Map.of(INGREDIENT_ID, 6, INGREDIENT_ID_2, 8)
        );
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    void givenSameIngredientFromMultipleOptions_whenDeductStock_thenBatchesSummedAmount() {
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3),
                countableOrderProduct(1, 1, PRODUCT_OPTION_ID_2, 4)
        ));
        ProductOption productOption1 = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        ProductOption productOption2 = buildProductOption(PRODUCT_OPTION_ID_2, INGREDIENT_ID, 4);

        when(productOptionRepository.findAllWithOptionByIdIn(anyCollection()))
                .thenReturn(List.of(productOption1, productOption2));
        when(storeInventoryBatchRepository.deductStockAtomicBatch(eq(STORE_ID), anyList(), anyMap()))
                .thenReturn(new int[]{1});

        assertDoesNotThrow(() -> inventoryService.deductStock(order));

        verify(storeInventoryBatchRepository).deductStockAtomicBatch(
                STORE_ID,
                List.of(INGREDIENT_ID),
                Map.of(INGREDIENT_ID, 10)
        );
    }

    @Test
    void givenSecondRowNotUpdated_whenDeductStock_thenThrowsInsufficientForThatIngredient() {
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3),
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID_2, 4)
        ));
        ProductOption productOption1 = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        ProductOption productOption2 = buildProductOption(PRODUCT_OPTION_ID_2, INGREDIENT_ID_2, 4);

        when(productOptionRepository.findAllWithOptionByIdIn(anyCollection()))
                .thenReturn(List.of(productOption1, productOption2));
        when(storeInventoryBatchRepository.deductStockAtomicBatch(eq(STORE_ID), anyList(), anyMap()))
                .thenReturn(new int[]{1, 0});
        when(storeInventoryRepository.findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID_2))
                .thenReturn(Optional.of(buildStoreInventory(INGREDIENT_ID_2, 5)));

        assertThrows(InsufficientOptionStockException.class, () -> inventoryService.deductStock(order));

        verify(storeInventoryRepository, never()).findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID);
    }

    @Test
    void givenRowNotUpdatedAndInventoryMissing_whenDeductStock_thenThrowsStoreInventoryNotFound() {
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3)
        ));
        ProductOption productOption = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);

        when(productOptionRepository.findAllWithOptionByIdIn(anyCollection()))
                .thenReturn(List.of(productOption));
        when(storeInventoryBatchRepository.deductStockAtomicBatch(eq(STORE_ID), anyList(), anyMap()))
                .thenReturn(new int[]{0});
        when(storeInventoryRepository.findByStoreIdAndIngredientId(STORE_ID, INGREDIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(StoreInventoryNotFoundException.class, () -> inventoryService.deductStock(order));
    }

    @Test
    void givenUnknownUpdateCount_whenDeductStock_thenThrowsIllegalState() {
        Order order = buildPaidOrder(List.of(
                countableOrderProduct(1, 2, PRODUCT_OPTION_ID, 3)
        ));
        ProductOption productOption = buildProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);

        when(productOptionRepository.findAllWithOptionByIdIn(anyCollection()))
                .thenReturn(List.of(productOption));
        when(storeInventoryBatchRepository.deductStockAtomicBatch(eq(STORE_ID), anyList(), anyMap()))
                .thenReturn(new int[]{Statement.SUCCESS_NO_INFO});

        assertThrows(IllegalStateException.class, () -> inventoryService.deductStock(order));

        verifyNoInteractions(storeInventoryRepository);
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
