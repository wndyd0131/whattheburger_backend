package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.CustomRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidatedCustomRule {
    private CustomRule customRule;
    private List<ValidatedOption> validatedOptions;
}
