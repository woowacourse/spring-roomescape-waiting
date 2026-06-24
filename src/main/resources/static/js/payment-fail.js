document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const orderId = params.get('orderId');
    const message = params.get('message');

    const statusEl = document.getElementById('status');
    const detailEl = document.getElementById('detail');

    if (!orderId) {
        statusEl.textContent = '결제가 완료되지 않았습니다.';
        detailEl.textContent = message || '정리할 주문번호가 없습니다.';
        return;
    }

    try {
        await fetchJson(`/payments/${encodeURIComponent(orderId)}`, { method: 'DELETE' });
        statusEl.textContent = '결제가 완료되지 않아 예약을 정리했습니다.';
        detailEl.textContent = message || `주문번호 ${orderId}`;
    } catch (error) {
        console.error('결제 대기 주문 정리 실패:', error);
        statusEl.textContent = '결제 대기 주문 정리에 실패했습니다.';
        detailEl.textContent = getErrorMessage(error, '잠시 후 다시 시도해주세요.');
    }
});
