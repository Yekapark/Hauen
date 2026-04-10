package com.hauen.controller;

import com.hauen.domain.Portfolio;
import com.hauen.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/edit")
@RequiredArgsConstructor
@Controller
public class EditController {

    private final PortFolioService portFolioService;

    // 수정 화면
    @GetMapping("/portfolio")
    public String editForm(@RequestParam("pk") int pk, Model model) {
        Portfolio portfolio = portFolioService.findById(pk);
        model.addAttribute("portfolio", portfolio);
        return "edit/portfolio-edit";
    }

    // 수정 처리
    @PostMapping("/portfolio/edit")
    public String editSave(@RequestParam("pk") int pk,
                           @ModelAttribute Portfolio form,
                           @RequestParam(value = "imageFiles", required = false) List<MultipartFile> newImages,
                           @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds) throws Exception {
        portFolioService.update(pk, form, newImages, deleteImageIds);
        return "redirect:/portfolio";
    }

    // 삭제 처리
    @PostMapping("/portfolio/delete")
    public String delete(@RequestParam("pk") int pk) {
        portFolioService.delete(pk);
        return "redirect:/portfolio";
    }
}
