package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.CategorizedStoreProductsReadDto;
import com.whattheburger.backend.controller.dto.store.StoreCustomRuleModifyRequest;
import com.whattheburger.backend.controller.dto.store.StoreOptionModifyRequest;
import com.whattheburger.backend.controller.dto.store.StoreProductModifyRequestDto;
import com.whattheburger.backend.controller.dto.store.StoreTraitModifyRequest;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.DeltaType;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.dto.OptionModificationDto;
import com.whattheburger.backend.service.dto.ProductReadByProductIdDto;
import com.whattheburger.backend.service.dto.StoreProductReadByProductIdDto;
import com.whattheburger.backend.service.dto.product.CustomRuleRequest;
import com.whattheburger.backend.service.dto.product.OptionTraitRequest;
import com.whattheburger.backend.service.dto.store.StoreProductsReadDto;
import com.whattheburger.backend.service.exception.*;
import com.whattheburger.backend.service.store_product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.whattheburger.backend.service.dto.store.StoreProductsReadDto.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreProductService {

    @Value("${aws.s3.public-url}")
    private String s3PublicUrl;

    private final ProductRepository productRepository;
    private final CustomRuleRepository customRuleRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;
    private final StoreOptionDeltaRepository storeOptionDeltaRepository;
    private final StoreTraitDeltaRepository storeTraitDeltaRepository;
    private final StoreRepository storeRepository;
    private final StoreProductRepository storeProductRepository;
    private final CategoryStoreProductRepository categoryStoreProductRepository;
    private final StoreProductModificationHandler storeProductModificationHandler;
    private final ModificationStrategyResolver modificationStrategyResolver;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;

    @Transactional
    public void registerProductToStores(Long productId, List<Long> storeIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        Set<Long> storeIdSet = storeIds.stream()
                .collect(Collectors.toSet());
        Map<Long, Store> storeIdMap = storeRepository.findAllById(storeIdSet)
                .stream().collect(Collectors.toMap(Store::getId, Function.identity()));
        List<StoreProduct> storeProducts = storeProductRepository.findByProductIdAndStoreIdIn(productId, storeIds);

        Set<Long> existingStoreIdSet = storeProducts.stream()
                .map(storeProduct -> storeProduct.getStore().getId())
                .collect(Collectors.toSet());
        List<Long> duplicateIds = storeIdSet.stream()
                .filter(existingStoreIdSet::contains)
                .toList();
        if (!duplicateIds.isEmpty())
            throw new StoreProductAlreadyExistException(duplicateIds, productId);

        List<StoreProduct> newStoreProducts = storeIds.stream()
                .map(storeId ->
                        Optional.ofNullable(storeIdMap.get(storeId))
                                .orElseThrow(() -> new StoreNotFoundException(storeId)))
                .map(store -> new StoreProduct(store, product))
                .toList();

        storeProductRepository.saveAll(newStoreProducts);
        Set<Category> categorySet = product.getCategoryProducts().stream()
                .map(categoryProduct ->
                        categoryProduct.getCategory()
                ).collect(Collectors.toSet());
        List<CategoryStoreProduct> categoryStoreProducts = newStoreProducts.stream()
                .flatMap(storeProduct -> categorySet.stream()
                        .map(category -> new CategoryStoreProduct(
                                storeProduct,
                                category
                        ))
                ).toList();
        categoryStoreProductRepository.saveAll(categoryStoreProducts);
    }

    @Transactional
    public void modifyProduct(
            Long storeId,
            Long storeProductId,
            StoreProductModifyRequestDto storeProductDto
    ) {
        StoreProduct storeProduct = storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> new StoreProductNotFoundException(storeProductId));
        if (!storeProduct.getStore().getId().equals(storeId))
            throw new IllegalArgumentException();
        Product product = storeProduct.getProduct();
        List<ProductOption> productOptions = productOptionRepository.findByProductId(product.getId());
        Set<CustomRule> customRuleSet = new HashSet<>();
        Map<Long, ProductOption> productOptionMap = new HashMap<>();
        for (ProductOption productOption : productOptions) {
            customRuleSet.add(productOption.getCustomRule());
            productOptionMap.put(productOption.getId(), productOption);
        }
        List<ProductOptionTrait> productOptionTraits = productOptionTraitRepository.findByProductOptionProductId(product.getId());
        Map<Long, ProductOptionTrait> productOptionTraitMap = productOptionTraits.stream()
                .collect(Collectors.toMap(ProductOptionTrait::getId, Function.identity()));

        if (!checkProductDiff(storeProductDto, storeProduct)) {
            updateProduct(storeProductDto, storeProduct);
        }
        List<StoreCustomRuleModifyRequest> customRuleRequests = storeProductDto.getCustomRuleRequests();
        for (StoreCustomRuleModifyRequest customRuleRequest : customRuleRequests) {
//            CustomRuleModificationCommand customRuleModificationCommand = new CustomRuleModificationCommand();
//            storeProductModificationHandler.handle(customRuleModificationCommand);
            List<StoreOptionModifyRequest> optionRequests = customRuleRequest.getOptionRequests();
            for (StoreOptionModifyRequest optionRequest : optionRequests) {
                Long optionId = optionRequest.getOptionId();
                OptionModificationCommand optionModificationCommand = new OptionModificationCommand(
                        storeProductId,
                        optionRequest.getOptionId(),
                        storeProduct,
                        optionRequest.getIsDefault(),
                        optionRequest.getDefaultQuantity(),
                        optionRequest.getMaxQuantity(),
                        optionRequest.getExtraPrice(),
                        optionRequest.getOrderIndex(),
                        optionRequest.getModifyType()
                );

                storeProductModificationHandler.handle(optionModificationCommand);

                List<StoreTraitModifyRequest> traitRequests = optionRequest.getOptionTraitRequests();
                for (StoreTraitModifyRequest traitRequest : traitRequests) {
                    Long traitId = traitRequest.getTraitId();
//                    TraitModificationCommand traitModificationCommand = new TraitModificationCommand();
//                    storeProductModificationHandler.handle(traitModificationCommand);
//                    switch(traitRequest.getModifyType()) {
//                        case EDIT -> {
//                            StoreTraitDelta storeTraitDelta = Optional.ofNullable(productOptionTraitMap.get(traitId))
//                                    .map(productOptionTrait -> new StoreTraitDelta(
//                                            traitRequest.getExtraPrice(),
//                                            DeltaType.OVERRIDE,
//                                            productOptionTrait,
//                                            storeProduct
//                                    ))
//                                    .orElseThrow(() -> new ProductOptionTraitNotFoundException(traitId));
//                            storeTraitDeltaRepository.save(storeTraitDelta); // DB INSERT happens
//                        }
//                    }
                }
            }
        }
        // check if there's change
        // check by iterating
        // if then, create either deltaClass, addClass... (add optionId different, modify -> optionId same, delete optionId does not exist)
        // else, throw ModificationNotDetectedException
    }

    private Boolean checkProductDiff(StoreProductModifyRequestDto storeProductDto, StoreProduct storeProduct) {
        if (storeProductDto.getProductPrice().equals(storeProduct.getOverridePrice())) {
            return true;
        }
        return false;
    }

//    private Boolean checkOptionDiff(StoreOptionModifyRequest storeOptionDto, StoreProduct storeProduct) {
//        if (storeProductDto.getProductPrice().equals(storeProduct.getOverridePrice())) {
//            return true;
//        }
//        return false;
//    }


    private void updateProduct(StoreProductModifyRequestDto storeProductDto, StoreProduct storeProduct) {
        storeProduct.changePrice(storeProductDto.getProductPrice());
        storeProductRepository.save(storeProduct);
        return;
    }


    public List<StoreProductsReadDto> getStoreProducts(Long storeId) {
        return storeProductRepository.findByStoreId(storeId).stream()
                .filter(StoreProduct::getIsActive)
                .map(storeProduct -> {
                    Product product = storeProduct.getProduct();
                    List<CategoryDto> categoryDtos = storeProduct.getCategoryStoreProducts().stream()
                            .map(categoryStoreProduct -> CategoryDto
                                    .builder()
                                    .categoryId(categoryStoreProduct.getCategory().getId())
                                    .orderIndex(categoryStoreProduct.getOrderIndex())
                                    .build()
                            ).toList();
                    return builder()
                            .productId(product.getId())
                            .name(product.getName())
                            .productType(product.getProductType())
                            .imageSource(product.getImageSource())
                            .price(storeProduct.getOverridePrice())
                            .calories(product.getCalories())
                            .briefInfo(product.getBriefInfo())
                            .categories(categoryDtos)
                            .build();
                })
                .toList();
    }

    public List<CategorizedStoreProductsReadDto> getCategorizedStoreProducts(Long storeId) {
        List<Category> categories = categoryRepository.findAll();
        record ProductOrder(StoreProduct storeProduct, Integer orderIndex) {}
        Map<Long, List<ProductOrder>> categoryProductMap = new HashMap<>();
        List<StoreProduct> storeProducts = storeProductRepository.findByStoreId(storeId);
        log.info("Store Product Count {}", storeProducts.size());
        storeProducts.stream()
                .filter(StoreProduct::getIsActive)
                .forEach(storeProduct -> {
                    storeProduct.getCategoryStoreProducts().stream()
                            .forEach(categoryStoreProduct -> {
                                Integer orderIndex = categoryStoreProduct.getOrderIndex(); // how to ??
                                Long categoryId = categoryStoreProduct.getCategory().getId();
                                categoryProductMap.computeIfAbsent(
                                        categoryId,
                                        k -> new ArrayList<>()
                                ).add(new ProductOrder(storeProduct, orderIndex));
                            });
                });
        categoryProductMap.values().forEach(list -> {
            log.info("LIST SIZE {}", list.size());
            list.sort(Comparator.comparing(ProductOrder::orderIndex));
        });

        List<CategorizedStoreProductsReadDto> categorizedStoreProductsReadDtos = categories.stream()
                .map(category -> {
                    Long categoryId = category.getId();
                    log.info("Category Product Map {}: ", categoryProductMap.get(categoryId));
                    List<ProductOrder> productOrders = categoryProductMap.computeIfAbsent(
                            categoryId,
                            k -> new ArrayList<>()
                    );
                    List<CategorizedStoreProductsReadDto.StoreProductDto> storeProductDtos = productOrders.stream()
                            .map(productOrder -> {
                                StoreProduct storeProduct = productOrder.storeProduct;

                                String productImageUrl = Optional.ofNullable(storeProduct.getProduct().getImageSource())
                                        .map(imageSource -> s3PublicUrl + "/" + imageSource)
                                        .orElse(null);

                                Product product = storeProduct.getProduct();
                                BigDecimal productPrice = Optional.ofNullable(storeProduct.getOverridePrice())
                                        .orElse(product.getPrice());
                                return CategorizedStoreProductsReadDto.StoreProductDto
                                        .builder()
                                        .storeProductId(storeProduct.getId())
                                        .briefInfo(product.getBriefInfo())
                                        .name(product.getName())
                                        .imageSource(productImageUrl)
                                        .price(productPrice)
                                        .calories(product.getCalories())
                                        .productType(product.getProductType())
                                        .build();
                            }).toList();
                    return CategorizedStoreProductsReadDto
                            .builder()
                            .categoryId(categoryId)
                            .categoryName(category.getName())
                            .orderIndex(category.getOrderIndex())
                            .products(storeProductDtos)
                            .build();
                }).collect(Collectors.toList());
        log.info("DTO SIZE {}", categorizedStoreProductsReadDtos.size());
        if (!categorizedStoreProductsReadDtos.isEmpty()) {
            categorizedStoreProductsReadDtos.sort(Comparator.comparing(
                    CategorizedStoreProductsReadDto::getOrderIndex
            ));
        }

        return categorizedStoreProductsReadDtos;
    }

    public StoreProductReadByProductIdDto getProductById(Long storeId, Long storeProductId) {
        StoreProduct storeProduct = storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> new StoreProductNotFoundException(storeProductId));

        if (!storeProduct.getStore().getId().equals(storeId))
            throw new IllegalArgumentException();
        Map<Long, StoreOptionDelta> storeOptionDeltaMap = storeProduct.getStoreOptionDeltas().stream()
                .collect(Collectors.toMap(storeOptionDelta -> storeOptionDelta.getProductOption().getId(), Function.identity()));
        String productImageUrl = null;
        String productImageSource = storeProduct.getProduct().getImageSource();
        if (productImageSource != null) {
            productImageUrl = s3PublicUrl + "/" + productImageSource;
        }

        Product product = storeProduct.getProduct();
        BigDecimal productPrice = Optional.ofNullable(storeProduct.getOverridePrice())
                .orElse(product.getPrice());
        List<ProductOption> productOptions = product.getProductOptions();
        List<StoreProductReadByProductIdDto.OptionResponse> optionResponses = new ArrayList<>();

        for (ProductOption productOption : productOptions) {
            record OptionDelta(BigDecimal price) {}
            // Option conditioning
            OptionDelta optionDelta = Optional.ofNullable(storeOptionDeltaMap.get(productOption.getId()))
                    .map(storeOptionDelta -> {
                        if (storeOptionDelta.getDeltaType() == DeltaType.OVERRIDE) {
                            return new OptionDelta(
                                    storeOptionDelta.getOverridePrice()
                            );
                        } else {
                            return new OptionDelta(
                                    null
                            );
                        }
                    })
                    .orElse(
                            new OptionDelta(productOption.getExtraPrice())
                    );
            Option option = productOption.getOption();
            String optionImageUrl = null;
            String optionImageSource = option.getImageSource();
            if (optionImageSource != null) {
                optionImageUrl = s3PublicUrl + "/" + optionImageSource;
            }
            List<StoreProductReadByProductIdDto.QuantityDetailResponse> quantityDetailResponses = productOption.getProductOptionOptionQuantities()
                    .stream()
                    .map(productOptionQuantity -> StoreProductReadByProductIdDto.QuantityDetailResponse
                            .builder()
                            .id(productOptionQuantity.getId())
                            .quantityType(productOptionQuantity.getOptionQuantity().getQuantity().getQuantityType())
                            .labelCode(productOptionQuantity.getOptionQuantity().getQuantity().getLabelCode())
                            .extraPrice(productOptionQuantity.getExtraPrice())
                            .extraCalories(productOptionQuantity.getOptionQuantity().getExtraCalories())
                            .isDefault(productOptionQuantity.getIsDefault())
                            .build()
                    )
                    .collect(Collectors.toList());

            List<ProductOptionTrait> productOptionTraits = productOption.getProductOptionTraits();
            List<StoreProductReadByProductIdDto.OptionTraitResponse> optionTraitResponses = new ArrayList<>();
            CustomRule customRule = productOption.getCustomRule();
            StoreProductReadByProductIdDto.CustomRuleResponse customRuleResponse =
                    StoreProductReadByProductIdDto.CustomRuleResponse
                            .builder()
                            .customRuleId(customRule.getId())
                            .name(customRule.getName())
                            .customRuleType(customRule.getCustomRuleType())
                            .orderIndex(customRule.getOrderIndex())
                            .minSelection(customRule.getMinSelection())
                            .maxSelection(customRule.getMaxSelection())
                            .build();
            for (ProductOptionTrait productOptionTrait : productOptionTraits) {
                OptionTrait optionTrait = productOptionTrait.getOptionTrait();
                optionTraitResponses.add(
                        StoreProductReadByProductIdDto.OptionTraitResponse
                                .builder()
                                .productOptionTraitId(productOptionTrait.getId())
                                .name(optionTrait.getName())
                                .labelCode(optionTrait.getLabelCode())
                                .optionTraitType(optionTrait.getOptionTraitType())
                                .defaultSelection(productOptionTrait.getDefaultSelection())
                                .extraPrice(productOptionTrait.getExtraPrice())
                                .extraCalories(productOptionTrait.getExtraCalories())
                                .build()
                );
            }
            optionResponses.add(StoreProductReadByProductIdDto.OptionResponse
                    .builder()
                    .productOptionId(productOption.getId())
                    .name(option.getName())
                    .isDefault(productOption.getIsDefault())
                    .defaultQuantity(productOption.getDefaultQuantity())
                    .maxQuantity(productOption.getMaxQuantity())
                    .quantityDetailResponses(quantityDetailResponses)
                    .extraPrice(optionDelta.price)
                    .calories(option.getCalories())
                    .countType(productOption.getCountType())
                    .imageSource(optionImageUrl)
                    .orderIndex(productOption.getOrderIndex())
                    .customRuleResponse(customRuleResponse)
                    .optionTraitResponses(optionTraitResponses)
                    .build()
            );
        }
        return StoreProductReadByProductIdDto
                .builder()
                .storeProductId(storeProduct.getId())
                .productName(product.getName())
                .productPrice(productPrice)
                .productCalories(product.getCalories())
                .imageSource(productImageUrl)
                .briefInfo(product.getBriefInfo())
                .optionResponses(optionResponses)
                .build();
    }

//    public Object getStoreProduct(Long storeProductId) {
//        storeOptionDeltaRepository.findByStoreProductId()
    // if Add/Delta exists
    // if add, sort by orderIndex
    // if delta, override / hidden handle
//    }
}
