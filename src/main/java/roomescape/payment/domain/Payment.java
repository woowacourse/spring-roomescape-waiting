package roomescape.payment.domain;

public class Payment {
  private Long id;
  private Long reservationId;
  private String paymentKey;
  private String orderId;
  private Long amount;

  public Payment(Long id, Long reservationId, String paymentKey, String orderId, Long amount) {
    this.id = id;
    this.reservationId = reservationId;
    this.paymentKey = paymentKey;
    this.orderId = orderId;
    this.amount = amount;
  }
}
