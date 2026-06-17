package roomescape.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문(Order) 생명주기 애플리케이션 서비스. 생성·확정·실패·만료조회를 소유한다.
 * orderDao만 의존하고 예약·결제를 모른다 — reservation↔payment 사이클을 끊는 중립 지점.
 */
@Service
@Transactional
public class OrderService {
    private final OrderDao orderDao;

    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    /**
     * 결제 인증 전, 주문 정보(orderId·금액)를 먼저 저장한다. orderId는 서버가 UUID로 생성한다.
     */
    public Order create(Long reservationId, long amount) {
        String orderId = UUID.randomUUID().toString();
        return orderDao.insert(Order.create(orderId, reservationId, amount));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByOrderId(String orderId) {
        return orderDao.findByOrderId(orderId);
    }

    public void complete(Order order, String paymentKey) {
        order.complete(paymentKey);
        orderDao.update(order);
    }

    public void markFailed(Order order) {
        order.markFailed();
        orderDao.update(order);
    }

    /**
     * 결제 신호 없이 방치된(abandonment) 주문 후보. 기준 시각보다 이전에 생성된 PENDING만.
     */
    @Transactional(readOnly = true)
    public List<String> findExpiredPendingOrderIds(LocalDateTime threshold) {
        return orderDao.findExpiredPending(threshold).stream()
                .map(Order::getOrderId)
                .toList();
    }
}
