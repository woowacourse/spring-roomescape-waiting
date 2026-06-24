// successUrl 콜백: 토스가 붙여준 paymentKey·orderId·amount 를 서버 승인 API로 넘긴다.
document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const paymentKey = params.get('paymentKey');
    const orderId = params.get('orderId');
    const amount = Number(params.get('amount'));

    const statusEl = document.getElementById('status');
    const detailEl = document.getElementById('detail');

    try {
        await fetchJson(`/payments/${orderId}/confirmation`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ paymentKey, amount })
        });
        statusEl.textContent = '결제가 완료되어 예약이 확정되었습니다.';
        detailEl.textContent = `주문번호 ${orderId}`;
    } catch (error) {
        console.error('결제 승인 실패:', error);
        statusEl.textContent = '결제 승인에 실패했습니다.';
        detailEl.textContent = getErrorMessage(error, '잠시 후 다시 시도해주세요.');
    }
});
