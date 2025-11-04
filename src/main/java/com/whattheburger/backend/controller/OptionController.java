package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.option.OptionQuantityDto;
import com.whattheburger.backend.controller.dto.option.OptionReadResponseDto;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OptionController {
    @Value("${aws.s3.public-url}")
    private String s3PublicUrl;
    private final OptionService optionService;

    @GetMapping("/api/v1/options")
    public ResponseEntity<List<OptionReadResponseDto>> getAllOptions() {
        List<Option> options = optionService.getAllOptions();
        List<OptionReadResponseDto> optionReadResponseDtos = new ArrayList<>();
        for (Option option : options) {
            List<OptionQuantityDto> optionQuantityDtos = option.getOptionQuantities().stream().map(optionQuantity ->
                    OptionQuantityDto
                            .builder()
                            .id(optionQuantity.getId())
                            .quantityType(optionQuantity.getQuantity().getQuantityType())
                            .build()
            ).collect(Collectors.toList());

            String optionImageUrl = Optional.ofNullable(option.getImageSource())
                    .map(imageSource -> s3PublicUrl + "/" + imageSource)
                    .orElse(null);

            optionReadResponseDtos.add(
                    new OptionReadResponseDto(
                            option.getId(),
                            option.getName(),
                            optionImageUrl,
                            option.getCalories(),
                            optionQuantityDtos
                    )
            );
        }
        return ResponseEntity.ok(optionReadResponseDtos);
    }
}
