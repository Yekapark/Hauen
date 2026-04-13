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

@RequestMapping("/edit")
@RequiredArgsConstructor
@Controller
public class EditController {

    private final PortFolioService portFolioService;

    @GetMapping("/portfolio")
    public String editForm(@RequestParam("pk") int pk, Model model) {
        Portfolio portfolio = portFolioService.findById(pk);
        model.addAttribute("portfolio", portfolio);
        return "edit/portfolio-edit";
    }

    @PostMapping("/portfolio/edit")
    public String editSave(
            @RequestParam("pk") int pk,
            @ModelAttribute Portfolio form,
            @RequestParam(value = "thumbnail", required = false) List<MultipartFile> thumbnail,
            @RequestParam(value = "before",    required = false) List<MultipartFile> before,
            @RequestParam(value = "entrance",  required = false) List<MultipartFile> entrance,
            @RequestParam(value = "living",    required = false) List<MultipartFile> living,
            @RequestParam(value = "kitchen",   required = false) List<MultipartFile> kitchen,
            @RequestParam(value = "room",      required = false) List<MultipartFile> room,
            @RequestParam(value = "bathroom",  required = false) List<MultipartFile> bathroom,
            @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds
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

        portFolioService.update(pk, form, categoryFiles, deleteImageIds);
        return "redirect:/portfolio";
    }

    @PostMapping("/portfolio/delete")
    public String delete(@RequestParam("pk") int pk) {
        portFolioService.delete(pk);
        return "redirect:/portfolio";
    }

    private List<MultipartFile> orEmpty(List<MultipartFile> list) {
        return list == null ? List.of() : list;
    }
}
