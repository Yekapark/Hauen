package com.hauen.controller;

import com.hauen.domain.Portfolio;
import com.hauen.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/reg")
@RequiredArgsConstructor
@Controller
public class RegController {

    private final PortFolioService portFolioService;

    // 등록 화면
    @GetMapping("/portfolio")
    public String portfolioForm(Model model) {
        model.addAttribute("portfolio", new Portfolio());
        return "reg/portfolio-form";
    }

    // 등록 처리
    @PostMapping("/portfolio")
    public String portfolioSave(@ModelAttribute Portfolio portfolio) {
        portFolioService.save(portfolio);
        return "redirect:/portfolio";
    }
}
