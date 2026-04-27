package com.hauen.service;

import com.hauen.domain.ContactRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from:}")
    private String fromAddress;

    @Value("${notification.email:}")
    private String toEmail;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    /**
     * Resend HTTP API로 이메일 발송 (비동기 — HTTP 응답을 블로킹하지 않음)
     * 설정이 없으면 로그만 출력하고 통과 (신청 저장을 막지 않음)
     */
    @Async
    public void send(ContactRequest req) {
        if (apiKey.isBlank() || toEmail.isBlank()) {
            log.info("[Resend 미설정] 이메일 알림 미발송 - 신청자: {} / {}", req.getName(), req.getPhone());
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                    "from",    fromAddress.isBlank() ? "onboarding@resend.dev" : fromAddress,
                    "to",      List.of(toEmail),
                    "subject", "[하우엔] 상담 신청 - " + req.getName(),
                    "text",    buildBody(req)
            );

            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[Resend] 이메일 발송 성공 - {}", req.getPhone());
        } catch (Exception e) {
            log.error("[Resend] 이메일 발송 실패 - {}: {}", req.getPhone(), e.getMessage());
        }
    }

    private String buildBody(ContactRequest req) {
        return String.format("""
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📋 하우엔 인테리어 상담 신청
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                ▶ 성함       : %s
                ▶ 연락처     : %s
                ▶ 현장주소   : %s
                ▶ 건물유형   : %s
                ▶ 면적       : %s
                ▶ 예산       : %s
                ▶ 공사시작   : %s
                ▶ 입주예정   : %s
                ▶ 요청사항   :
                %s

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
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

    private String nullSafe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
