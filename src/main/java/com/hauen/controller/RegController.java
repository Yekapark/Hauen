package com.hauen.controller;

import com.hauen.domain.Portfolio;
import com.hauen.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    public String portfolioSave(
            @ModelAttribute Portfolio portfolio,
            @RequestParam(value = "thumbnail", required = false) List<MultipartFile> thumbnail,
            @RequestParam(value = "before", required = false) List<MultipartFile> before,
            @RequestParam(value = "entrance", required = false) List<MultipartFile> entrance,
            @RequestParam(value = "living", required = false) List<MultipartFile> living,
            @RequestParam(value = "kitchen", required = false) List<MultipartFile> kitchen,
            @RequestParam(value = "room", required = false) List<MultipartFile> room,
            @RequestParam(value = "bathroom", required = false) List<MultipartFile> bathroom
    ) throws Exception {

        Map<String, List<MultipartFile>> categoryFiles = Map.of(
                "thumbnail", orEmpty(thumbnail),
                "before",    orEmpty(before),
                "entrance",  orEmpty(entrance),
                "living",    orEmpty(living),
                "kitchen",   orEmpty(kitchen),
                "room",      orEmpty(room),
                "bathroom",  orEmpty(bathroom)
        );

        portFolioService.save(portfolio, categoryFiles);
        return "redirect:/portfolio";
    }

    private List<MultipartFile> orEmpty(List<MultipartFile> list) {
        return list == null ? List.of() : list;
    }
}
