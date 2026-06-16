(function () {
    "use strict";

    const state = {
        step: 1,
        themes: [],
        selectedTheme: null,
        selectedDate: null,
    };

    const $ = (selector) => document.querySelector(selector);
    const themeGrid = $('#theme-grid');
    const reserveForm = $('#reserve-form');
    const timeSelect = $('#time-select');
    const nameInput = $('#name-input');
    const reserveMessage = $('#reserve-message');

    let paymentWidget = null;
    let paymentMethodsWidget = null;

    async function init() {
        await Promise.all([loadThemes(), loadPopularThemes()]);
        checkUrlParams();
        lucide.createIcons();
    }

    async function loadThemes() {
        try {
            const res = await fetch('/themes');
            state.themes = await res.json();
            renderThemes(state.themes, '#theme-grid');
        } catch (e) {
            console.error('테마 로드 실패', e);
        }
    }

    async function loadPopularThemes() {
        try {
            const res = await fetch('/themes/popular?limit=3');
            const popularThemes = await res.json();
            const section = $('#popular-themes-section');
            if (!popularThemes || !Array.isArray(popularThemes) || popularThemes.length === 0) {
                section.style.display = 'none';
                return;
            }
            section.style.display = 'block';
            renderThemes(popularThemes, '#popular-theme-grid');
        } catch (e) {
            console.error('인기 테마 로드 실패', e);
            $('#popular-themes-section').style.display = 'none';
        }
    }

    function renderThemes(themes, targetSelector) {
        const container = $(targetSelector);
        if (!themes || !Array.isArray(themes) || themes.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1 / -1; text-align:center; padding:60px; background:var(--bg-card); border-radius:16px; border:1px solid var(--border); color:var(--text-muted);">
                    <i data-lucide="info" style="width:40px; height:40px; margin-bottom:12px; opacity:0.5;"></i>
                    <p>현재 등록된 테마가 없습니다.</p>
                </div>
            `;
            lucide.createIcons();
            return;
        }
        container.innerHTML = themes.map(t => `
            <div class="theme-card" onclick="onThemeSelect(${t.id})">
                <div class="theme-img-box">
                    <img src="${t.url || 'https://images.unsplash.com/photo-1519074063912-ad25b57b984a?q=80&w=1000&auto=format&fit=crop'}" class="theme-img" alt="${t.name}">
                </div>
                <div class="theme-info">
                    <h3 class="theme-name">${t.name}</h3>
                    <p class="theme-desc">${t.description}</p>
                </div>
            </div>
        `).join('');
    }

    $('#nav-my').addEventListener('click', (e) => {
        e.preventDefault();
        setStep(0);
        document.querySelectorAll('.flow-step').forEach(el => el.classList.remove('is-active'));
        $('#my-section').classList.remove('is-hidden');
    });

    $('#nav-booking').addEventListener('click', (e) => {
        if (state.step === 0) {
            e.preventDefault();
            setStep(1);
        }
    });

    window.onThemeSelect = (id) => {
        state.selectedTheme = state.themes.find(t => t.id === id);
        $('#summary-theme-name').textContent = state.selectedTheme.name;
        setStep(2);
        initCalendar();
    };

    function setStep(n) {
        state.step = n;
        window.scrollTo({ top: 0, behavior: 'smooth' });
        
        document.querySelectorAll('.step-content').forEach((el, i) => {
            el.classList.toggle('is-hidden', i + 1 !== n);
        });
        
        document.querySelectorAll('.flow-step').forEach((el, i) => {
            el.classList.toggle('is-active', i + 1 <= n);
        });

        if (n > 0) {
            $('#my-section').classList.add('is-hidden');
        } else {
            document.querySelectorAll('.step-content').forEach(el => el.classList.add('is-hidden'));
        }
    }
    window.setStep = setStep;

    async function initAndRenderPaymentWidget(methodsSelector, agreementSelector) {
        if (!window.TOSS_CLIENT_KEY) return;
        
        try {
            if (!paymentWidget) {
                paymentWidget = PaymentWidget(window.TOSS_CLIENT_KEY, PaymentWidget.ANONYMOUS);
            }
            
            paymentMethodsWidget = paymentWidget.renderPaymentMethods(methodsSelector, { value: 50000 });
            paymentWidget.renderAgreement(agreementSelector);
        } catch (e) {
            console.error('결제 위젯 로드 실패:', e);
        }
    }

    function initCalendar() {
        const grid = $('#calendar-grid');
        grid.innerHTML = '';
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        for (let i = 0; i < 14; i++) {
            const d = new Date();
            d.setDate(today.getDate() + i);
            const dateStr = d.toISOString().split('T')[0];
            const isToday = i === 0;
            
            const btn = document.createElement('button');
            btn.className = 'calendar-day-btn';
            btn.innerHTML = `
                <div style="font-size:0.75rem; opacity:0.6; font-weight:700; color: ${isToday ? 'var(--primary)' : 'inherit'}">
                    ${d.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                </div>
                <div style="font-size:1.5rem; font-weight:800; margin-top:4px;">${d.getDate()}</div>
            `;
            btn.onclick = () => onDateSelect(dateStr);
            grid.appendChild(btn);
        }
    }

    async function onDateSelect(date) {
        state.selectedDate = date;
        $('#summary-date').textContent = date;
        setStep(3);
        await loadTimes(date);
    }

    async function loadTimes(date) {
        try {
            const res = await fetch(`/times/available-times?themeId=${state.selectedTheme.id}&date=${date}`);
            const slots = await res.json();
            
            timeSelect.innerHTML = '<option value="">시간을 선택해주세요</option>' + slots.map(s => {
                const isReserved = s.status === 'RESERVED';
                return `
                    <option value="${s.id}">
                        ${s.startAt.slice(0, 5)} ${isReserved ? '(예약 대기)' : ''}
                    </option>
                `;
            }).join('');
        } catch (e) {
            console.error('시간 로드 실패', e);
        }
    }

    reserveForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if (!timeSelect.value) {
            alert('시간을 선택해주세요.');
            return;
        }

        reserveMessage.textContent = '예약 처리 중...';
        reserveMessage.style.color = 'var(--primary)';
        
        const body = {
            name: nameInput.value,
            date: state.selectedDate,
            timeId: timeSelect.value,
            themeId: state.selectedTheme.id
        };

        try {
            const res = await fetch('/reservations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            if (!res.ok) {
                const errorData = await res.json().catch(() => ({ message: '알 수 없는 오류가 발생했습니다.' }));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            const data = await res.json();

            if (data.status === 'PENDING_PAYMENT' && data.orderId) {
                await window.payReservation(data.orderId, state.selectedTheme.name);
                reserveMessage.textContent = '결제를 진행해 주세요.';
            } else if (data.status === 'WAITING') {
                window.location.href = '/?view=my&resStatus=WAITING';
            } else {
                window.location.href = '/?view=my&resStatus=SUCCESS';
            }
        } catch (err) {
            if (err.code === 'USER_CANCEL') {
                reserveMessage.textContent = '결제가 취소되었습니다.';
            } else {
                reserveMessage.textContent = '오류 발생: ' + err.message;
            }
            reserveMessage.style.color = 'var(--danger)';
        }
    });

    $('#my-search-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = $('#my-name-input').value;
        if (!name) {
            alert('성함을 입력해주세요.');
            return;
        }
        try {
            const res = await fetch(`/reservations/my-reservation?name=${encodeURIComponent(name)}`);
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({ message: '조회 중 오류가 발생했습니다.' }));
                throw new Error(errorData.message);
            }
            const list = await res.json();
            renderMyList(list);
        } catch (e) {
            console.error('예약 조회 실패', e);
            alert('조회 실패: ' + e.message);
        }
    });

    function getBadgeInfo(status) {
        if (status === 'CONFIRMED') return { text: '예약 확정', class: 'badge-confirmed' };
        if (status === 'WAITING') return { text: '예약 대기', class: 'badge-waiting' };
        return { text: '결제 대기', class: 'badge-pending' };
    }

    function renderMyList(reservations) {
        const container = $('#my-list');
        if (!reservations || !Array.isArray(reservations) || reservations.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:80px; background:var(--bg-card); border-radius:20px; border:1px solid var(--border); color:var(--text-muted);">
                    조회된 예약 내역이 없습니다.
                </div>
            `;
            return;
        }
        
        container.innerHTML = reservations.map(r => {
            const badge = getBadgeInfo(r.status);
            const orderInfo = (r.order && r.status === 'WAITING') ? ` (대기 순번: ${r.order}번)` : '';
            const paymentBtn = r.status === 'PENDING_PAYMENT' 
                ? `<button class="btn-primary-sm" style="margin-right:8px;" onclick="payReservation('${r.orderId}', '${r.themeResponse.name}')">결제하기</button>` 
                : '';
            
            return `
                <div class="mission-card">
                    <div class="mission-info">
                        <span class="badge ${badge.class}">${badge.text}${orderInfo}</span>
                        <h3 style="margin-top:12px; font-size:1.3rem;">${r.themeResponse.name}</h3>
                        <div class="mission-meta" style="color:var(--text-muted); font-size:0.95rem; margin-top:4px;">
                            ${r.date} • ${r.timeResponse.startAt.slice(0,5)}
                        </div>
                    </div>
                    <div style="display:flex;">
                        ${paymentBtn}
                        <button class="btn-danger-sm" onclick="cancelReservation(${r.id})">예약 취소</button>
                    </div>
                </div>
            `;
        }).join('');
    }

    window.payReservation = async (orderId, themeName) => {
        const modal = $('#payment-modal');
        modal.classList.add('is-active');
        $('#payment-modal-title').textContent = `${themeName} 결제`;

        await initAndRenderPaymentWidget('#modal-payment-widget-container', '#modal-agreement-widget-container');

        const payBtn = $('#modal-payment-btn');
        const newPayBtn = payBtn.cloneNode(true);
        payBtn.parentNode.replaceChild(newPayBtn, payBtn);

        newPayBtn.addEventListener('click', async () => {
            try {
                await paymentWidget.requestPayment({
                    orderId: orderId,
                    orderName: themeName,
                    successUrl: window.location.origin + "/payments/success",
                    failUrl: window.location.origin + "/payments/fail",
                });
            } catch (err) {
                if (err.code !== 'USER_CANCEL') {
                    alert('결제 요청 중 오류가 발생했습니다: ' + err.message);
                }
            }
        });
    };

    window.closePaymentModal = () => {
        $('#payment-modal').classList.remove('is-active');
    };

    window.cancelReservation = async (id) => {
        const confirmed = await confirm('정말로 예약을 취소하시겠습니까?');
        if (!confirmed) return;
        
        try {
            const res = await fetch(`/reservations/${id}`, { method: 'DELETE' });
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({ message: '알 수 없는 오류가 발생했습니다.' }));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            alert('예약이 취소되었습니다.');
            $('#my-search-form').dispatchEvent(new Event('submit'));
        } catch (e) {
            alert('취소 실패: ' + e.message);
        }
    };

    function checkUrlParams() {
        const params = new URLSearchParams(window.location.search);
        const payment = params.get('payment');
        const resStatus = params.get('resStatus');
        const view = params.get('view');

        if (payment === 'success') {
            alert('결제가 완료되어 예약이 확정되었습니다!');
        } else if (payment === 'fail') {
            alert('결제에 실패했습니다: ' + params.get('message'));
        }

        if (resStatus === 'WAITING') {
            alert('예약 대기로 신청되었습니다. 앞선 예약이 취소되어 본인 차례가 되면 \'내 예약 확인\' 메뉴에서 결제하실 수 있습니다.');
        } else if (resStatus === 'SUCCESS') {
            alert('예약이 성공적으로 완료되었습니다.');
        }

        if (view === 'my') {
            setStep(0);
            document.querySelectorAll('.flow-step').forEach(el => el.classList.remove('is-active'));
            $('#my-section').classList.remove('is-hidden');
        }

        if (payment || resStatus || view) {
            window.history.replaceState({}, document.title, "/");
        }
    }

    init();
})();
