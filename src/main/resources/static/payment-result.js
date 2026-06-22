const $ = id => document.getElementById(id);

async function extractErrorMessage(res) {
  try {
    const text = await res.text();
    if (!text) return `요청이 실패했습니다. (${res.status})`;
    try {
      const json = JSON.parse(text);
      return json.message || json.detail || json.error || json.title
        || text || `요청이 실패했습니다. (${res.status})`;
    } catch {
      return text || `요청이 실패했습니다. (${res.status})`;
    }
  } catch {
    return `요청이 실패했습니다. (${res.status})`;
  }
}

async function post(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(await extractErrorMessage(res));
  }

  const text = await res.text();
  return text ? JSON.parse(text) : {};
}

function updateResult(title, message) {
  $('payment-result-title').textContent = title;
  $('payment-result-message').textContent = message;
}

async function confirmPayment(params) {
  const paymentKey = params.get('paymentKey');
  const orderId = params.get('orderId');
  const amount = Number(params.get('amount'));

  if (!paymentKey || !orderId || !amount) {
    updateResult('결제 승인 실패', '결제 승인에 필요한 정보가 부족합니다.');
    return;
  }

  try {
    const result = await post('/payments/confirm', { paymentKey, orderId, amount });
    if (result.paymentStatus === 'REQUIRES_CONFIRMATION') {
      updateResult(
        '결제 확인이 필요합니다',
        result.message || '결제 승인 응답을 받지 못했습니다. 내 예약에서 상태를 확인해주세요.'
      );
      return;
    }

    updateResult('예약이 확정되었습니다', result.message || '결제가 승인되어 예약이 완료되었습니다.');
  } catch (e) {
    updateResult('결제 승인 실패', e.message);
  }
}

async function failPayment(params) {
  const code = params.get('code') || 'UNKNOWN';
  const message = params.get('message') || '결제가 완료되지 않았습니다.';
  const orderId = params.get('orderId');

  try {
    await post('/payments/fail', { code, message, orderId });
  } finally {
    updateResult('결제가 완료되지 않았습니다', message);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  const result = document.body.dataset.paymentResult;

  if (result === 'success') {
    confirmPayment(params);
    return;
  }

  failPayment(params);
});
