package com.hauen.controller;

import com.hauen.domain.Portfolio;
import com.hauen.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PortFolioService portfolioService;

    @Value("${kakao.map.api-key:}")
    private String kakaoMapApiKey;

    private final int SIZE = 6;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);
        return "about";
    }

    @GetMapping("/portfolio")
    public String portfolio() {
        return "portfolio";
    }

    // 포트폴리오 AJAX API (필터+페이징)
    @GetMapping("/api/portfolios")
    @ResponseBody
    public ResponseEntity<?> portfolioApi(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page) {

        Page<Portfolio> result = portfolioService.findByFilter(filter, PageRequest.of(page, SIZE));

        var items = result.getContent().stream().map(p -> java.util.Map.of(
                "id", p.getId(),
                "title", p.getTitle(),
                "areaPyeong", p.getAreaPyeong(),
                "thumbnailUrl", p.getThumbnailUrl() != null ? p.getThumbnailUrl() : ""
        )).toList();

        return ResponseEntity.ok(java.util.Map.of(
                "items", items,
                "currentPage", result.getNumber(),
                "totalPages", result.getTotalPages()
        ));
    }

    @GetMapping("/portfolio/{id}")
    public String portfolioDetail(@PathVariable int id, Model model) {
        Portfolio portfolio = portfolioService.findById(id);
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("groups", List.of("before","entrance","living","kitchen","room","bathroom"));
        return "portfolio-detail";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
