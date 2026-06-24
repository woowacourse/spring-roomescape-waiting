const API = '/reservations';
const WAITINGS_API = '/waitings';
const TIMES_API = '/times';
const THEMES_API = '/themes';
const TOSS_CLIENT_KEY = 'test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm';

let isEditing = false;
let cachedThemes = [];
let pendingOrder = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', startAdd);

    document.getElementById('payment-modal-close').addEventListener('click', closePaymentModal);
    document.getElementById('payment-modal').addEventListener('click', (event) => {
        if (event.target === event.currentTarget) closePaymentModal();
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !document.getElementById('payment-modal').hidden) {
            closePaymentModal();
        }
    });

    refresh();
});

async function refresh() {
    try {
        const [reservationData, themeData] = await Promise.all([
            fetchJson(API),
            fetchJson(THEMES_API)
        ]);

        cachedThemes = themeData.themes;
        render(reservationData.reservations);
    } catch (error) {
        console.error('예약 조회 실패:', error);
    }
}

function render(reservations) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';

    if (reservations.length === 0) {
        showEmptyState(tbody, 6, '등록된 예약이 없습니다. "예약 추가" 버튼으로 시작해보세요.');
        return;
    }

    reservations.forEach((reservation, index) => {
        const row = tbody.insertRow();

        row.insertCell().textContent = index + 1;
        row.insertCell().textContent = reservation.name;
        row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
        row.insertCell().textContent = reservation.date;
        row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';

        const actions = row.insertCell();
        actions.className = 'actions';
        const startAt = reservation.time ? reservation.time.startAt : null;
        if (isPastSlot(reservation.date, startAt)) {
            actions.appendChild(createDisabledButton('삭제', 'btn-danger', '지난 예약은 삭제할 수 없습니다.'));
        } else {
            actions.appendChild(createButton('삭제', 'btn-danger', () => deleteRow(reservation.id)));
        }
    });
}

function startAdd() {
    if (isEditing) return;

    if (cachedThemes.length === 0) {
        alert('등록된 테마가 없습니다. "테마 관리" 페이지에서 먼저 등록해주세요.');
        return;
    }

    isEditing = true;

    const tbody = document.getElementById('table-body');
    const emptyRow = tbody.querySelector('.empty-state');
    if (emptyRow) emptyRow.closest('tr').remove();

    const row = tbody.insertRow(0);
    row.insertCell().textContent = '';

    const nameInput = createInput('text', '예: 민욱');
    const themeSelect = createSelect();
    const dateInput = createInput('date');
    const timeSelect = createSelect();

    appendPlaceholder(themeSelect, '테마를 선택해주세요.');
    cachedThemes.forEach(theme => {
        const option = document.createElement('option');
        option.value = theme.id;
        option.textContent = theme.name;
        themeSelect.appendChild(option);
    });

    appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
    timeSelect.disabled = true;

    row.insertCell().appendChild(nameInput);
    row.insertCell().appendChild(themeSelect);
    row.insertCell().appendChild(dateInput);
    row.insertCell().appendChild(timeSelect);

    const actions = row.insertCell();
    actions.className = 'actions';

    const saveButton = createButton('예약 등록', 'btn-primary', async () => {
        await saveRow(
            nameInput.value,
            dateInput.value,
            themeSelect.value,
            timeSelect.value,
            timeSelect
        );
    });
    actions.appendChild(saveButton);

    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        row.remove();
        isEditing = false;
        refresh();
    }));

    const refreshTimesAndButton = async () => {
        await refreshAvailableTimes(dateInput.value, themeSelect.value, timeSelect);
        updateSaveButtonLabel(timeSelect, saveButton);
    };

    dateInput.addEventListener('change', refreshTimesAndButton);
    themeSelect.addEventListener('change', refreshTimesAndButton);
    timeSelect.addEventListener('change', () => updateSaveButtonLabel(timeSelect, saveButton));
}

async function refreshAvailableTimes(date, themeId, timeSelect) {
    clearSelect(timeSelect);

    if (!date || !themeId) {
        appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
        timeSelect.disabled = true;
        return;
    }

    try {
        const data = await fetchJson(
            `${TIMES_API}/availability?date=${encodeURIComponent(date)}&themeId=${encodeURIComponent(themeId)}`
        );
        const times = data.times;

        clearSelect(timeSelect);

        if (!times || times.length === 0) {
            appendPlaceholder(timeSelect, '등록된 시간이 없습니다.');
            timeSelect.disabled = true;
            return;
        }

        let hasSelectedTime = false;

        times.forEach(time => {
            const option = document.createElement('option');

            const reserved = isReserved(time);

            option.value = time.id;
            option.dataset.reserved = String(reserved);
            option.textContent = reserved ? `${time.startAt} (대기 가능)` : time.startAt;
            option.className = reserved ? 'time-option-unavailable' : 'time-option-available';

            if (!reserved && !hasSelectedTime) {
                option.selected = true;
                hasSelectedTime = true;
            }

            timeSelect.appendChild(option);
        });

        if (!hasSelectedTime) {
            timeSelect.options[0].selected = true;
        }

        timeSelect.disabled = false;
    } catch (error) {
        console.error('예약 가능 시간 조회 실패:', error);
        clearSelect(timeSelect);
        appendPlaceholder(timeSelect, '예약 가능 시간 조회 실패');
        timeSelect.disabled = true;
    }
}

function isReserved(time) {
    return time.reserved === true;
}

function updateSaveButtonLabel(timeSelect, saveButton) {
    const selectedOption = timeSelect.options[timeSelect.selectedIndex];
    const isWaiting = selectedOption && selectedOption.dataset.reserved === 'true';
    saveButton.textContent = isWaiting ? '대기 신청' : '예약 등록';
}

async function saveRow(name, date, themeId, timeId, timeSelect) {
    if (!name.trim() || !date || !themeId || !timeId) {
        alert('모든 항목을 입력해주세요.');
        return;
    }

    const selectedOption = timeSelect.options[timeSelect.selectedIndex];
    const isWaiting = selectedOption && selectedOption.dataset.reserved === 'true';
    const body = JSON.stringify({
        name,
        date,
        themeId: Number(themeId),
        timeId: Number(timeId)
    });

    if (isWaiting) {
        try {
            await fetchJson(WAITINGS_API, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body
            });
            isEditing = false;
            alert('대기 신청이 완료되었습니다. 내 예약에서 대기 순번을 확인할 수 있습니다.');
            refresh();
        } catch (error) {
            console.error('대기 신청 실패:', error);
            alert(getErrorMessage(error, '대기 신청에 실패했습니다.'));
        }
        return;
    }

    try {
        // 예약은 결제 대기(PENDING)로 생성되고 orderId·결제금액을 돌려받는다.
        const order = await fetchJson(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body
        });
        isEditing = false;
        await openPaymentModal(order);
    } catch (error) {
        console.error('예약 추가 실패:', error);
        alert(getErrorMessage(error, '예약 추가에 실패했습니다.'));
    }
}

async function openPaymentModal(order) {
    pendingOrder = order;

    const themeName = order.theme ? order.theme.name : '방탈출';
    const orderName = `방탈출 예약 - ${themeName}`;

    document.getElementById('order-name').textContent = orderName;
    document.getElementById('order-amount').textContent = `${order.amount.toLocaleString()}원`;

    // 이전 모달의 위젯 iframe이 남아 있지 않도록 비우고 새로 렌더한다.
    document.getElementById('payment-method').innerHTML = '';
    document.getElementById('agreement').innerHTML = '';

    const payButton = document.getElementById('payment-button');
    payButton.disabled = true;
    document.getElementById('payment-modal').hidden = false;

    try {
        const tossPayments = TossPayments(TOSS_CLIENT_KEY);
        const widgets = tossPayments.widgets({ customerKey: TossPayments.ANONYMOUS });
        await widgets.setAmount({ currency: 'KRW', value: order.amount });
        await Promise.all([
            widgets.renderPaymentMethods({ selector: '#payment-method', variantKey: 'DEFAULT' }),
            widgets.renderAgreement({ selector: '#agreement', variantKey: 'AGREEMENT' })
        ]);

        payButton.disabled = false;
        payButton.onclick = async () => {
            payButton.disabled = true;
            try {
                // 성공·실패 모두 토스가 successUrl·failUrl 로 페이지를 리다이렉트한다.
                await widgets.requestPayment({
                    orderId: order.orderId,
                    orderName,
                    successUrl: `${window.location.origin}/payment/success`,
                    failUrl: `${window.location.origin}/payment/fail`
                });
            } catch (error) {
                // 위젯 단계 오류(약관 미동의·검증 실패 등)는 모달을 유지해 재시도할 수 있게 둔다.
                console.error('결제 요청 실패:', error);
                payButton.disabled = false;
            }
        };
    } catch (error) {
        console.error('결제 위젯 초기화 실패:', error);
        alert('결제 위젯을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
        await closePaymentModal();
    }
}

async function closePaymentModal() {
    document.getElementById('payment-modal').hidden = true;

    const order = pendingOrder;
    pendingOrder = null;

    if (order) {
        // 결제를 마치지 않고 모달을 닫으면 결제 대기(PENDING) 주문을 정리한다.
        try {
            await fetchJson('/payments/fail', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    code: 'USER_CANCEL',
                    message: '사용자가 결제를 취소했습니다.',
                    orderId: order.orderId
                })
            });
        } catch (error) {
            console.error('결제 대기 주문 정리 실패:', error);
        }
    }

    refresh();
}

async function deleteRow(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await fetchJson(`${API}/${id}`, { method: 'DELETE' });
        refresh();
    } catch (error) {
        console.error('예약 삭제 실패:', error);
        alert(getErrorMessage(error, '예약 삭제에 실패했습니다.'));
    }
}

function appendPlaceholder(select, message) {
    const option = document.createElement('option');
    option.value = '';
    option.textContent = message;
    select.appendChild(option);
}

function clearSelect(select) {
    select.innerHTML = '';
}
