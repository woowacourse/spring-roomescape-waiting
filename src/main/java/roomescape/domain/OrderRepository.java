package roomescape.domain;

public interface OrderRepository {

    void save(Order order);

    Order getByOrderId(String orderId);

}
