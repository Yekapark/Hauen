package com.hauen.controller;

import com.hauen.domain.Portfolio;
import com.hauen.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PortFolioService portfolioService;

    private final int SIZE = 6;

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "all") String filter,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {

        Page<Portfolio> result = portfolioService.findByFilter(filter, PageRequest.of(page, SIZE));

        model.addAttribute("portfolios", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("filter", filter);

        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/portfolio")
    public String portfolio(@RequestParam(defaultValue = "all") String filter,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        Page<Portfolio> result = portfolioService.findByFilter(filter, PageRequest.of(page, SIZE));

        model.addAttribute("portfolios", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("filter", filter);

        return "portfolio";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
