package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.OptionTrait;
import com.whattheburger.backend.repository.OptionTraitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionTraitService {

    private final OptionTraitRepository optionTraitRepository;

    public List<OptionTrait> getAllOptionTraits() {
        return optionTraitRepository.findAll();
    }
}
