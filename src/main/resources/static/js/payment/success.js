// 2-3에서 결제 승인(confirm) 로직 구현 예정
// Toss가 리다이렉트 시 전달하는 파라미터: paymentKey, orderId, amount
const params = new URLSearchParams(window.location.search);
const paymentKey = params.get("paymentKey");
const orderId = params.get("orderId");
const amount = params.get("amount");

console.log("결제 완료 파라미터:", {paymentKey, orderId, amount});
