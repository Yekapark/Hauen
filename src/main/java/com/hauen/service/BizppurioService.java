package com.hauen.service;

import com.hauen.domain.ContactRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class BizppurioService {

    @Value("${bizppurio.user-id:}")
    private String userId;

    @Value("${bizppurio.password:}")
    private String password;

    @Value("${bizppurio.sender:}")
    private String sender;

    @Value("${bizppurio.template-code:}")
    private String templateCode;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.bizppurio.com")
            .build();

    /**
     * 비즈뿌리오 카카오 알림톡 발송
     * API 키가 없으면 로그만 출력하고 통과 (신청 저장을 막지 않음)
     */
    public void send(ContactRequest req) {
        if (userId.isBlank() || password.isBlank() || templateCode.isBlank()) {
            log.warn("[비즈뿌리오 미설정] 알림톡 미발송 - 신청자: {} / {}", req.getName(), req.getPhone());
            return;
        }

        try {
            String auth = Base64.getEncoder().encodeToString((userId + ":" + password).getBytes());

            // ── 알림톡 본문 (템플릿 변수 매핑) ──────────────────────────────
            String message = buildMessage(req);

            Map<String, Object> body = Map.of(
                    "type", "at",
                    "to", normalizePhone(req.getPhone()),
                    "from", sender,
                    "content", Map.of(
                            "templatecode", templateCode,
                            "message", message
                    )
            );

            restClient.post()
                    .uri("/v3/message")
                    .header("Authorization", "Basic " + auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[비즈뿌리오] 알림톡 발송 성공 - {}", req.getPhone());
        } catch (Exception e) {
            // 알림톡 실패해도 신청 저장은 유지
            log.error("[비즈뿌리오] 알림톡 발송 실패 - {}: {}", req.getPhone(), e.getMessage());
        }
    }

    private String buildMessage(ContactRequest req) {
        // TODO: 비즈뿌리오 템플릿 승인 후 템플릿 변수에 맞게 수정
        return String.format("""
                [하우엔 상담 신청]
                성함: %s
                연락처: %s
                현장주소: %s
                건물유형: %s
                면적: %s
                예산: %s
                공사시작: %s
                입주예정: %s
                요청사항: %s
                """,
                req.getName(),
                req.getPhone(),
                req.getLocation(),
                nullSafe(req.getBuildingType()),
                nullSafe(req.getArea()),
                nullSafe(req.getBudget()),
                nullSafe(req.getStartDate()),
                nullSafe(req.getMoveInDate()),
                nullSafe(req.getMessage())
        );
    }

    /** 010-1234-5678 → 01012345678 (비즈뿌리오는 하이픈 없는 형식) */
    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }

    private String nullSafe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
