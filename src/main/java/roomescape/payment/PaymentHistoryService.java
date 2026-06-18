package roomescape.payment;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.order.OrderService;
import roomescape.payment.web.MyOrderResponse;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;

/**
 * 결제/주문 내역 조회 전용 서비스(읽기). 결제 흐름을 조율하는 PaymentService와 분리해 화면용 조합만 맡는다.
 * 한 회원의 예약들에 묶인 주문을 모아 reservationId로 돌려줘, 프론트가 예약 정보와 합쳐 보여줄 수 있게 한다.
 */
@Service
@Transactional(readOnly = true)
public class PaymentHistoryService {

    private final ReservationService reservationService;
    private final OrderService orderService;

    public PaymentHistoryService(ReservationService reservationService, OrderService orderService) {
        this.reservationService = reservationService;
        this.orderService = orderService;
    }

    public List<MyOrderResponse> findMyOrders(Long memberId) {
        List<Long> reservationIds = reservationService.findAllByMemberId(memberId).stream()
                .map(Reservation::getId)
                .toList();
        if (reservationIds.isEmpty()) {
            return List.of();
        }
        return orderService.findByReservationIds(reservationIds).stream()
                .map(MyOrderResponse::from)
                .toList();
    }
}
