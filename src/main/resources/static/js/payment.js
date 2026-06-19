// 토스페이먼츠 결제 흐름 공통 헬퍼.
// 사용 전 토스 SDK(https://js.tosspayments.com/v2/standard)가 먼저 로드되어 있어야 한다.

async function fetchPaymentJson(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    let data;
    try {
      data = await response.json();
    } catch (error) {
      data = { message: '요청 처리에 실패했습니다.' };
    }
    const requestError = new Error(data?.message || '요청 처리에 실패했습니다.');
    requestError.data = data;
    throw requestError;
  }
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null;
  }
  return response.json();
}

// 토스 orderId 는 가맹점이 발급하는 6~64자 문자열이다. (토스가 발급하는 건 paymentKey)
function generateOrderId() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }
  return 'order-' + Date.now() + '-' + Math.random().toString(16).slice(2);
}

// ACTIVE + PENDING(미결제) 예약의 결제를 시작한다. 성공 시 successUrl 로 리다이렉트된다.
async function startReservationPayment(reservationId, reserverName = '') {
  const config = await fetchPaymentJson('/api/payments/config');
  const tossPayments = TossPayments(config.clientKey);
  const payment = tossPayments.payment({ customerKey: TossPayments.ANONYMOUS });

  const successUrl = new URL('/payments/success', window.location.origin);
  successUrl.searchParams.set('reservationId', reservationId);
  if (reserverName) {
    successUrl.searchParams.set('reservationName', reserverName);
  }

  await payment.requestPayment({
    method: 'CARD',
    amount: { currency: 'KRW', value: config.amount },
    orderId: generateOrderId(),
    orderName: '방탈출 예약',
    successUrl: successUrl.toString(),
    failUrl: window.location.origin + '/payments/fail'
  });
}
