package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.OptionTraitReadResponseDto;
import com.whataburger.whataburgerproject.domain.OptionTrait;
import com.whataburger.whataburgerproject.service.OptionTraitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OptionTraitController {

    private final OptionTraitService optionTraitService;

    @GetMapping("/api/v1/optionTraits")
    public ResponseEntity<List<OptionTraitReadResponseDto>> getAllOptionTraits() {
        List<OptionTrait> allOptionTraits = optionTraitService.getAllOptionTraits();
        List<OptionTraitReadResponseDto> optionTraitReadResponseDtos = new ArrayList<>();
        for (OptionTrait optionTrait : allOptionTraits) {
            optionTraitReadResponseDtos.add(
                    new OptionTraitReadResponseDto(
                            optionTrait.getId(),
                            optionTrait.getName(),
                            optionTrait.getLabelCode(),
                            optionTrait.getOptionTraitType()
                    )
            );
        }

        return ResponseEntity.ok(optionTraitReadResponseDtos);
    }
}
