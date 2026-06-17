package roomescape.payment.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.application.exception.OrderUpdateException;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderRepository;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.reservation.application.ReservationReader;
import roomescape.reservation.application.dto.ReservationIntegrationInfo;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private static final Long AMOUNT = 50000L;

    private final Clock clock;
    private final OrderRepository orderRepository;
    private final ReservationReader reservationReader;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        Order failed = order.fail(clock);
        int affected = orderRepository.update(failed);
        if (affected == 0) {
            log.error("결제 실패 상태 갱신 중 에러 발생. OrderId: {}", orderId);
            throw new OrderUpdateException("결제 실패 상태 저장에 실패했습니다.");
        }
    }

    @Transactional
    public Order complete(String orderId, String paymentKey) {
        Order order = orderRepository.getByOrderId(orderId);
        Order completed = order.complete(paymentKey, clock);
        if (orderRepository.update(completed) == 0) {
            throw new OrderUpdateException("주문 갱신 실패 (affected row 0)");
        }
        return completed;
    }

    @Transactional
    public Order cancel(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        Order cancelled = order.cancel(clock);
        orderRepository.update(cancelled);
        return cancelled;
    }

    @Transactional
    public void createOrder(Long reservationId) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        ReservationIntegrationInfo reservation = reservationReader.read(reservationId);
        orderRepository.save(Order.createPending(orderId, AMOUNT, reservation.id(), clock));
    }

    public OrderInfo getOrder(Long reservationId) {
        Order order = orderRepository.getByReservationId(reservationId);
        ReservationIntegrationInfo reservation = reservationReader.read(reservationId);
        return OrderInfo.from(order, reservation);
    }

    public OrderInfo getOrder(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        ReservationIntegrationInfo reservation = reservationReader.read(order.getReservationId());
        return OrderInfo.from(order, reservation);
    }

    public List<OrderInfo> getOrdersByName(String name) {
        List<Order> orders = orderRepository.findAllByName(name);
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> reservationIds = orders.stream()
                .map(Order::getReservationId)
                .toList();
        Map<Long, ReservationIntegrationInfo> reservationMap = reservationReader.readAll(
                reservationIds);
        return orders.stream()
                .map(order -> OrderInfo.from(order, reservationMap.get(order.getReservationId())))
                .toList();
    }

    public List<Order> findAbandonedOrders(LocalDateTime thresholdTime) {
        return orderRepository.findPendingOrdersBefore(thresholdTime);
    }

    public boolean existsByReservationId(Long reservationId) {
        return orderRepository.existsByReservationId(reservationId);
    }
}
