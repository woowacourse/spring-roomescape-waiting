package roomescape.reservation.domain;

public class Payment {

  private final Long id;
  private final Long reservationId;
  private final String paymentKey;
  private final Long orderId;
  private final Long amount;
  private final PaymentStatus paymentStatus;

  public Payment(Long id, Long reservationId, String paymentKey, Long orderId, Long amount,
      PaymentStatus paymentStatus) {
    this.id = id;
    this.reservationId = reservationId;
    this.paymentKey = paymentKey;
    this.orderId = orderId;
    this.amount = amount;
    this.paymentStatus = paymentStatus;
  }
}
