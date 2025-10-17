package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.service.store_product.OptionModificationCommand;
import com.whattheburger.backend.service.store_product.TraitModificationCommand;

public sealed interface StoreProductModificationCommand permits CustomRuleModificationCommand, OptionModificationCommand, TraitModificationCommand { }
