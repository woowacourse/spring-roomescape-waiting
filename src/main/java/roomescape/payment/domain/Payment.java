package roomescape.payment.domain;

public class Payment {
  private Long id;
  private Long reservationId;
  private String paymentKey;
  private String orderId;
  private Long amount;
  private PaymentStatus status;

  public Payment(Long id, Long reservationId, String paymentKey, String orderId, Long amount,
      PaymentStatus status) {
    this.id = id;
    this.reservationId = reservationId;
    this.paymentKey = paymentKey;
    this.orderId = orderId;
    this.amount = amount;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public Long getReservationId() {
    return reservationId;
  }

  public String getPaymentKey() {
    return paymentKey;
  }

  public String getOrderId() {
    return orderId;
  }

  public Long getAmount() {
    return amount;
  }

  public PaymentStatus getStatus() {
    return status;
  }
}
