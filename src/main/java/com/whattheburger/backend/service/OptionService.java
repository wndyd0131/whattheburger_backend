package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionService {

    private final OptionRepository optionRepository;

    public List<Option> getAllOptions() {
        return optionRepository.findAll();
    }
}
