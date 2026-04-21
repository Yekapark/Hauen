/**
 * HAUEN 인테리어 - 공통 스크립트
 * main.js
 *
 * 포함 기능:
 *  1. 네비바 스크롤 전환 (hero-mode ↔ scrolled)
 *  2. 스크롤 진입 애니메이션 (IntersectionObserver)
 *  3. 포트폴리오 필터 탭
 *  4. 상담 폼 글자 수 카운터
 *  5. 상담 폼 제출 처리
 */

/* ─────────────────────────────────────────
   1. 네비바: 스크롤에 따라 배경 전환
───────────────────────────────────────── */
function initNavbar() {
    const navbar = document.getElementById('navbar');
    if (!navbar) return;

    const onScroll = () => {
        if (window.scrollY > 60) {
            navbar.classList.remove('hero-mode');
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.add('hero-mode');
            navbar.classList.remove('scrolled');
        }
    };

    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll(); // 초기 상태 반영
}

/* ─────────────────────────────────────────
   2. 스크롤 진입 애니메이션
   - 이미 뷰포트 안 요소: 즉시 표시
   - 밖 요소: IntersectionObserver로 감지
───────────────────────────────────────── */
function initScrollReveal() {
    const reveals = document.querySelectorAll('.reveal');
    if (!reveals.length) return;

    const observer = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    observer.unobserve(entry.target); // 한 번 표시 후 해제
                }
            });
        },
        { threshold: 0, rootMargin: '0px 0px -40px 0px' }
    );

    const checkAndObserve = () => {
        reveals.forEach((el) => {
            const rect = el.getBoundingClientRect();
            // 이미 뷰포트 안에 있으면 즉시 표시
            if (rect.top < window.innerHeight && rect.bottom > 0) {
                el.classList.add('visible');
            } else {
                observer.observe(el);
            }
        });
    };

    // DOMContentLoaded 또는 이미 로드된 경우 바로 실행
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', checkAndObserve);
    } else {
        checkAndObserve();
    }

    // 안전장치: 모든 리소스 로드 완료 후 한 번 더 체크
    window.addEventListener('load', checkAndObserve);
}

/* ─────────────────────────────────────────
   3. 포트폴리오 필터 탭
───────────────────────────────────────── */
function initPortfolioFilter() {
    const tabs = document.querySelectorAll('.filter-tab');
    const cards = document.querySelectorAll('.portfolio-card');
    if (!tabs.length || !cards.length) return;

    tabs.forEach((tab) => {
        tab.addEventListener('click', () => {
            // 활성 탭 전환
            tabs.forEach((t) => t.classList.remove('active'));
            tab.classList.add('active');

            const filter = tab.dataset.filter;

            cards.forEach((card) => {
                const match = filter === 'all' || card.dataset.category === filter;
                if (match) {
                    card.style.display = '';
                    // 짧은 딜레이로 opacity 트랜지션 적용
                    requestAnimationFrame(() => {
                        card.style.opacity = '1';
                    });
                } else {
                    card.style.opacity = '0';
                    setTimeout(() => { card.style.display = 'none'; }, 300);
                }
            });
        });
    });
}

/* ─────────────────────────────────────────
   4. 상담 폼 글자 수 카운터
───────────────────────────────────────── */
function initCharCounter() {
    const msgArea  = document.getElementById('msgArea');
    const charCount = document.getElementById('charCount');
    if (!msgArea || !charCount) return;

    const MAX = 500;
    msgArea.addEventListener('input', () => {
        const len = msgArea.value.length;
        charCount.textContent = `${len}/${MAX}`;
        // 한도 초과 시 빨간색 표시
        charCount.style.color = len > MAX ? '#ff6b6b' : 'rgba(255,255,255,0.3)';
    });
}

/* ─────────────────────────────────────────
   5. 상담 폼 제출 처리
───────────────────────────────────────── */
async function handleSubmit(btn) {
    const val = id => { const el = document.getElementById(id); return el ? el.value.trim() : ''; };
    const radio = name => { const el = document.querySelector(`input[name="${name}"]:checked`); return el ? el.value : ''; };

    // 필수 항목 검증
    if (!val('name') || !val('phone') || !val('location')) {
        alert('성함, 연락처, 현장주소는 필수 항목입니다.');
        return;
    }

    btn.disabled = true;
    btn.textContent = '전송 중...';

    const body = {
        name:         val('name'),
        phone:        val('phone'),
        location:     val('location'),
        buildingType: val('buildingType'),
        area:         val('area'),
        jungmun:      radio('jungmun'),
        expansion:    radio('expansion'),
        bathroom:     radio('bathroom'),
        sink:         radio('sink'),
        builtin:      radio('builtin'),
        budget:       val('budget'),
        startDate:    val('startDate'),
        moveInDate:   val('moveInDate'),
        message:      val('msgArea'),
    };

    try {
        const res = await fetch('/api/contact', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });

        if (res.ok) {
            btn.textContent = '✓ 신청이 완료되었습니다!';
            btn.style.background = '#2D3E2A';
        } else if (res.status === 429) {
            const data = await res.json();
            alert(data.message || '잠시 후 다시 시도해 주세요.');
            btn.textContent = '상담 신청하기 →';
            btn.disabled = false;
        } else if (res.status === 403) {
            btn.textContent = '신청이 제한된 번호입니다.';
            btn.style.background = '#999';
        } else {
            throw new Error('서버 오류');
        }
    } catch (e) {
        alert('오류가 발생했습니다. 다시 시도해 주세요.');
        btn.textContent = '상담 신청하기 →';
        btn.disabled = false;
    }
}

/* ─────────────────────────────────────────
   6. 햄버거 메뉴
───────────────────────────────────────── */
function initHamburger() {
    const hamburger = document.getElementById('hamburger');
    const mobileMenu = document.getElementById('mobileMenu');
    if (!hamburger || !mobileMenu) return;

    hamburger.addEventListener('click', () => {
        hamburger.classList.toggle('open');
        mobileMenu.classList.toggle('open');
    });

    // 메뉴 링크 클릭 시 닫기
    mobileMenu.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', () => {
            hamburger.classList.remove('open');
            mobileMenu.classList.remove('open');
        });
    });

    // 외부 클릭 시 닫기
    document.addEventListener('click', (e) => {
        if (!hamburger.contains(e.target) && !mobileMenu.contains(e.target)) {
            hamburger.classList.remove('open');
            mobileMenu.classList.remove('open');
        }
    });
}

/* ─────────────────────────────────────────
   초기화 진입점
───────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initScrollReveal();
    initPortfolioFilter();
    initCharCounter();
    initHamburger();
});