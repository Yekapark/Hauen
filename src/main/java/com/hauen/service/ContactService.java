package com.hauen.service;

import com.hauen.domain.BlockedPhone;
import com.hauen.domain.ContactRequest;
import com.hauen.repository.BlockedPhoneRepository;
import com.hauen.repository.ContactRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final int IP_LIMIT        = 5;   // 1시간 내 IP당 최대 요청 수
    private static final int PHONE_LIMIT      = 2;   // 24시간 내 전화번호당 최대 요청 수

    private final ContactRequestRepository contactRepo;
    private final BlockedPhoneRepository   blockedPhoneRepo;
    private final BizppurioService         bizppurioService;

    @Transactional
    public void submit(ContactRequest req, String clientIp) {
        // 1. 수동 차단 번호 확인
        if (blockedPhoneRepo.existsByPhone(normalize(req.getPhone()))) {
            throw new BlockedException("차단된 전화번호입니다.");
        }

        // 2. IP 기반 rate limit (1시간 내 3회)
        long ipCount = contactRepo.countByClientIpSince(clientIp, LocalDateTime.now().minusHours(1));
        if (ipCount >= IP_LIMIT) {
            throw new RateLimitException("잠시 후 다시 시도해 주세요.");
        }

        // 3. 전화번호 기반 rate limit (24시간 내 2회)
        long phoneCount = contactRepo.countByPhoneSince(normalize(req.getPhone()), LocalDateTime.now().minusDays(1));
        if (phoneCount >= PHONE_LIMIT) {
            throw new RateLimitException("해당 번호로는 이미 신청이 접수되었습니다.");
        }

        // 4. DB 저장
        req.setPhone(normalize(req.getPhone()));
        req.setClientIp(clientIp);
        contactRepo.save(req);

        // 5. 알림톡 발송 (실패해도 저장은 유지)
        bizppurioService.send(req);
    }

    // ── 관리자 기능 ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BlockedPhone> findAllBlocked() {
        return blockedPhoneRepo.findAll();
    }

    @Transactional
    public void block(String phone, String reason) {
        String normalized = normalize(phone);
        if (!blockedPhoneRepo.existsByPhone(normalized)) {
            BlockedPhone entry = new BlockedPhone();
            entry.setPhone(normalized);
            entry.setReason(reason);
            blockedPhoneRepo.save(entry);
        }
    }

    @Transactional
    public void unblock(String phone) {
        blockedPhoneRepo.findByPhone(normalize(phone))
                .ifPresent(blockedPhoneRepo::delete);
    }

    // ── 내부 전화번호 정규화 ────────────────────────────────────────────────

    private String normalize(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }

    // ── 예외 클래스 ─────────────────────────────────────────────────────────

    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) { super(message); }
    }

    public static class BlockedException extends RuntimeException {
        public BlockedException(String message) { super(message); }
    }
}
