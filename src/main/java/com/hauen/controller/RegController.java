package com.hauen.controller;

import com.hauen.domain.Portfolio;
import com.hauen.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/reg")
@RequiredArgsConstructor
@Controller
public class RegController {

    private final PortFolioService portFolioService;

    @GetMapping("/portfolio")
    public String portfolioForm(Model model) {
        model.addAttribute("portfolio", new Portfolio());
        return "reg/portfolio-form";
    }

    @PostMapping("/portfolio")
    public String portfolioSave(@ModelAttribute Portfolio portfolio,
                                @RequestParam(value = "imageFiles", required = false) List<MultipartFile> images) throws Exception {
        if (images == null) images = List.of();
        portFolioService.save(portfolio, images);
        return "redirect:/portfolio";
    }
}
