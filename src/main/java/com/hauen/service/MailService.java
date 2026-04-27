package com.hauen.service;

import com.hauen.domain.ContactRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${notification.email:}")
    private String toEmail;

    /**
     * 상담 신청 정보를 이메일로 발송 (비동기 — HTTP 응답을 블로킹하지 않음)
     * 설정이 없으면 로그만 출력하고 통과 (신청 저장을 막지 않음)
     */
    @Async
    public void send(ContactRequest req) {
        if (fromEmail.isBlank() || toEmail.isBlank()) {
            log.info("[메일 미설정] 이메일 알림 미발송 - 신청자: {} / {}", req.getName(), req.getPhone());
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("[하우엔] 상담 신청 - " + req.getName());
            msg.setText(buildBody(req));
            mailSender.send(msg);
            log.info("[메일] 상담 신청 이메일 발송 성공 - {}", req.getPhone());
        } catch (Exception e) {
            // 이메일 실패해도 신청 저장은 유지
            log.error("[메일] 이메일 발송 실패 - {}: {}", req.getPhone(), e.getMessage());
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
