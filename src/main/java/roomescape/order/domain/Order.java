package roomescape.order.domain;

public class Order {

  private final Long id;
  private final String orderId;
  private final Long amount;

  public Order(Long id, String orderId, Long amount) {
    this.id = id;
    this.orderId = orderId;
    this.amount = amount;
  }

  public Long getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public Long getAmount() {
    return amount;
  }
}
