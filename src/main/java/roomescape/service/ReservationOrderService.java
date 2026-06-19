package roomescape.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationOrder.OrderStatus;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationOrder.ReservationOrderRepository;
import roomescape.exception.PaymentException.PaymentNotFoundException;

@Service
public class ReservationOrderService {

    private final ReservationOrderRepository reservationOrderRepository;

    public ReservationOrderService(ReservationOrderRepository reservationOrderRepository) {
        this.reservationOrderRepository = reservationOrderRepository;
    }

    public ReservationOrder insert(long reservationId) {
        ReservationOrder order = ReservationOrder.create(10000, reservationId);
        reservationOrderRepository.insert(order);
        return order;
    }

    public ReservationOrder getByReservationId(long reservationId) {
        return reservationOrderRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new PaymentNotFoundException("해당 예약에 대해서 존재하지 않는 주문입니다: " + reservationId));
    }

    public Optional<ReservationOrder> findByReservationId(long reservationId) {
        return reservationOrderRepository.findByReservationId(reservationId);
    }

    public ReservationOrder getByOrderId(String orderId) {
        return reservationOrderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("존재하지 않는 주문입니다: " + orderId));
    }

    public void completeOrder(ReservationOrder order, String paymentKey) {
        ReservationOrder confirmed = order.confirm(paymentKey);
        reservationOrderRepository.updatePaymentKey(confirmed.getId(), confirmed.getPaymentKey());
    }

    public void markUnknown(ReservationOrder order) {
        reservationOrderRepository.updateStatus(order.getId(), OrderStatus.UNKNOWN);
    }
}
