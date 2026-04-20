package com.whattheburger.backend.integration.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.service.ProductService;
import com.whattheburger.backend.service.StoreProductService;
import com.whattheburger.backend.service.StoreService;
import com.whattheburger.backend.service.dto.product.ProductCreateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test-only helper that seeds products into the DB from {@code seeds/catalog.json}.
 *
 * <p>Mirrors the production seeding logic in {@code DataConfig} but is
 * callable on demand from integration tests, so each test class can choose
 * to seed the full catalog or just a subset.</p>
 *
 * <p>Requires the base reference data (categories, options, option_traits,
 * quantities, etc.) referenced by ID inside {@code catalog.json} to exist
 * first — typically loaded via {@code data.sql} or an {@code @Sql} script.</p>
 */
@Component
public class CatalogSeeder {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);
    private static final String CATALOG_RESOURCE = "seeds/catalog.json";

    private final ProductService productService;
    private final StoreService storeService;
    private final StoreProductService storeProductService;
    private final ObjectMapper objectMapper;

    public CatalogSeeder(
            ProductService productService,
            StoreService storeService,
            StoreProductService storeProductService,
            ObjectMapper objectMapper
    ) {
        this.productService = productService;
        this.storeService = storeService;
        this.storeProductService = storeProductService;
        this.objectMapper = objectMapper;
    }

    public List<Product> seedAll() throws IOException {
        return seed(Collections.emptySet());
    }

    public List<Product> seedByNames(String... productNames) throws IOException {
        if (productNames == null || productNames.length == 0) {
            return Collections.emptyList();
        }
        return seed(new HashSet<>(Arrays.asList(productNames)));
    }

    /**
     * Registers each product to every store returned by {@link StoreService#loadAllStores()}.
     *
     * <p>If no stores exist (e.g. {@code data.sql} was not loaded), logs a warning and
     * returns an empty list so callers get a clear signal instead of a silent no-op.</p>
     *
     * <p><b>Idempotency:</b> {@link StoreProductService#registerProductToStores} throws
     * {@code StoreProductAlreadyExistException} when a {@code (store, product)} row
     * already exists. Tests that invoke this more than once per persistent context
     * must either seed in {@code @BeforeAll} (with {@code @TestInstance(PER_CLASS)})
     * or truncate {@code store_product} between tests.</p>
     *
     * @return the store IDs that products were registered to (empty if no stores were found)
     */
    public List<Long> registerProductsToAllStores(List<Product> products) {
        List<Long> storeIds = storeService.loadAllStores().stream()
                .map(Store::getId)
                .toList();
        if (storeIds.isEmpty()) {
            log.warn("CatalogSeeder: no stores found; skipping store-product registration");
            return storeIds;
        }
        return registerProductsToStores(products, storeIds);
    }

    /**
     * Explicit variant of {@link #registerProductsToAllStores(List)} that registers the
     * given products to the given store IDs.
     *
     * <p><b>Idempotency:</b> See {@link #registerProductsToAllStores(List)} — calling this
     * twice for the same {@code (store, product)} pair throws
     * {@code StoreProductAlreadyExistException}.</p>
     */
    public List<Long> registerProductsToStores(List<Product> products, List<Long> storeIds) {
        if (products == null || products.isEmpty() || storeIds == null || storeIds.isEmpty()) {
            return storeIds == null ? Collections.emptyList() : storeIds;
        }
        products.forEach(p -> storeProductService.registerProductToStores(p.getId(), storeIds));
        log.info("CatalogSeeder: registered {} product(s) to {} store(s)", products.size(), storeIds.size());
        return storeIds;
    }

    /**
     * Convenience: {@link #seedAll()} followed by {@link #registerProductsToAllStores(List)}.
     *
     * <p>See {@link #registerProductsToAllStores(List)} for the duplicate-registration
     * constraint.</p>
     */
    public List<Product> seedAllAndRegisterToAllStores() throws IOException {
        List<Product> products = seedAll();
        registerProductsToAllStores(products);
        return products;
    }

    /**
     * Convenience: {@link #seedByNames(String...)} followed by
     * {@link #registerProductsToAllStores(List)}.
     *
     * <p>See {@link #registerProductsToAllStores(List)} for the duplicate-registration
     * constraint.</p>
     */
    public List<Product> seedByNamesAndRegisterToAllStores(String... productNames) throws IOException {
        List<Product> products = seedByNames(productNames);
        registerProductsToAllStores(products);
        return products;
    }

    private List<Product> seed(Set<String> nameFilter) throws IOException {
        List<ProductCreateDto> dtos = readCatalog();
        log.info("CatalogSeeder: loaded {} product(s) from {}", dtos.size(), CATALOG_RESOURCE);

        List<Product> created = new ArrayList<>();
        for (ProductCreateDto dto : dtos) {
            if (!nameFilter.isEmpty() && !nameFilter.contains(dto.getProductName())) {
                continue;
            }
            created.add(productService.createProduct(dto));
        }
        log.info("CatalogSeeder: created {} product(s)", created.size());
        return created;
    }

    private List<ProductCreateDto> readCatalog() throws IOException {
        ClassPathResource resource = new ClassPathResource(CATALOG_RESOURCE);
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, new TypeReference<List<ProductCreateDto>>() {});
        }
    }
}
