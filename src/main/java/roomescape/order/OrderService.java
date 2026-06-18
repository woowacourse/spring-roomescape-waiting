package roomescape.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;

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

    /**
     * 결제 시작용 주문을 확보한다. 같은 예약에 아직 처리되지 않은(PENDING) 주문이 있으면 그걸 재사용하고,
     * 없으면 새로 만든다 — 결제창 새로고침 등으로 한 예약에 미결제 주문이 중복으로 쌓이지 않게 한다(멱등).
     */
    public Order getOrCreate(Long reservationId, long amount) {
        return orderDao.findPendingByReservationId(reservationId)
                .orElseGet(() -> createPending(reservationId, amount));
    }

    private Order createPending(Long reservationId, long amount) {
        try {
            return create(reservationId, amount);
        } catch (DuplicateKeyException e) {
            // UNIQUE(reservation_id) 백스톱. 동시 결제 준비 경합이면 먼저 만들어진 PENDING 주문을 재사용하고,
            // 이미 확정/취소된 예약이면(=PENDING 없음) 결제를 새로 시작할 수 없다.
            return orderDao.findPendingByReservationId(reservationId)
                    .orElseThrow(() -> new BusinessRuleViolationException("이미 처리된 예약입니다."));
        }
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

    public void markNeedsCheck(Order order) {
        order.markNeedsCheck();
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
