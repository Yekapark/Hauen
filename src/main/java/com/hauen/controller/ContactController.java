package com.hauen.controller;

import com.hauen.domain.ContactRequest;
import com.hauen.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody ContactRequest req, HttpServletRequest httpReq) {
        // 필수 필드 검증
        if (isBlank(req.getName()) || isBlank(req.getPhone()) || isBlank(req.getLocation())) {
            return ResponseEntity.badRequest().body(Map.of("message", "필수 항목을 입력해 주세요."));
        }

        String clientIp = extractIp(httpReq);

        try {
            contactService.submit(req, clientIp);
            return ResponseEntity.ok(Map.of("message", "상담 신청이 완료되었습니다."));
        } catch (ContactService.BlockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (ContactService.RateLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /** Railway/Nginx 프록시 뒤에서도 실제 클라이언트 IP 추출 */
    private String extractIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
