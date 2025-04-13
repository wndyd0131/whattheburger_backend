package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.OptionTrait;
import com.whataburger.whataburgerproject.repository.OptionTraitRepository;
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
