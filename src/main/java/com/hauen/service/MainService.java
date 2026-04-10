package com.hauen.service;

import com.hauen.domain.Portfolio;
import com.hauen.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MainService {

    private final PortfolioRepository portfolioRepository;


}
