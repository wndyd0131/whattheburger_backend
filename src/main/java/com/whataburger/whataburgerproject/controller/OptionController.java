package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.OptionReadResponseDto;
import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @GetMapping("/api/v1/options")
    public ResponseEntity<List<OptionReadResponseDto>> getAllOptions() {
        List<Option> options = optionService.getAllOptions();
        List<OptionReadResponseDto> optionReadResponseDtos = new ArrayList<>();
        for (Option option : options) {
            optionReadResponseDtos.add(
                    new OptionReadResponseDto(
                            option.getId(),
                            option.getName(),
                            option.getImageSource(),
                            option.getCalories()
                    )
            );
        }
        return ResponseEntity.ok(optionReadResponseDtos);
    }
}
