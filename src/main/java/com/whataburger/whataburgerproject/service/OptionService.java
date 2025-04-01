package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionService {

    private final OptionRepository optionRepository;

    public List<Option> findAllOptions() {
        return optionRepository.findAll();
    }
}
