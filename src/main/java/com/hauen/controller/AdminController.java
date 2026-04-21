package com.hauen.controller;

import com.hauen.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/{secret}")
@RequiredArgsConstructor
public class AdminController {

    @Value("${admin.secret}")
    private String adminSecret;

    private final ContactService contactService;

    /** secret 불일치 시 404 반환 (존재 자체를 숨김) */
    private void verify(String secret) {
        if (!adminSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /** 차단 목록 페이지 */
    @GetMapping("/blocks")
    public String blocks(@PathVariable String secret, Model model) {
        verify(secret);
        model.addAttribute("blocked", contactService.findAllBlocked());
        model.addAttribute("secret", secret);
        return "admin/blocks";
    }

    /** 번호 차단 */
    @PostMapping("/block")
    public String block(@PathVariable String secret,
                        @RequestParam String phone,
                        @RequestParam(required = false) String reason) {
        verify(secret);
        contactService.block(phone, reason);
        return "redirect:/admin/" + secret + "/blocks";
    }

    /** 차단 해제 */
    @PostMapping("/unblock")
    public String unblock(@PathVariable String secret,
                          @RequestParam String phone) {
        verify(secret);
        contactService.unblock(phone);
        return "redirect:/admin/" + secret + "/blocks";
    }
}
