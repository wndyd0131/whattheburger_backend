package com.whattheburger.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoreProductController {

    @PostMapping
    public void createProduct(@RequestBody StoreProductCreateRequestDto storeProductDto) {

    }
}
