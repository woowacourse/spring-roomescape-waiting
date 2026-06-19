package roomescape.payment.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderStatus;
import roomescape.reservation.application.ReservationManager;
import roomescape.reservation.application.ReservationReader;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationIntegrationInfo;
import roomescape.reservation.domain.PendingReservation;
import roomescape.reservation.application.PendingReservationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final Clock clock;
    private final PendingReservationService pendingReservationService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ReservationManager reservationManager;
    private final ReservationReader reservationReader;

    @Scheduled(cron = "0 0 0 * * *")
    public void processExpiredPendingRefunds() {
        LocalDate today = LocalDate.now(clock);
        log.info("만료된 대기 예약 자동 환불 배치를 시작합니다. 기준일: {}", today);

        List<PendingReservation> expiredPendingReservations = pendingReservationService.findExpiredReservations(today);

        for (PendingReservation pending : expiredPendingReservations) {
            pendingReservationRefund(pending);
        }
        log.info("만료된 대기 예약 자동 환불 배치가 종료되었습니다.");
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanupUnpaidReservations() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now(clock).minusMinutes(5);
        List<Order> abandonedOrders = orderService.findAbandonedOrders(fiveMinutesAgo);

        if (!abandonedOrders.isEmpty()) {
            log.info("미결제 유령 예약 청소를 시작합니다. 대상 건수: {}", abandonedOrders.size());
        }
        List<Long> reservationIds = abandonedOrders.stream()
                .map(Order::getReservationId)
                .toList();
        Map<Long, ReservationIntegrationInfo> reservationMap = reservationReader.readAll(
                reservationIds);
        for (Order order : abandonedOrders) {
            try {
                paymentService.fail(order.getOrderId());
                String reserverName = reservationMap.get(order.getReservationId()).name();
                ReservationCancelCommand cancelCommand = new ReservationCancelCommand(reserverName);

                reservationManager.cancelReservation(order.getReservationId(), cancelCommand);
                log.info("유령 예약 청소 완료: 주문ID={}, 예약ID={}", order.getOrderId(), order.getReservationId());
            } catch (Exception e) {
                log.error("유령 예약 청소 중 에러 발생: 주문ID={}", order.getOrderId(), e);
            }
        }
    }

    private void pendingReservationRefund(PendingReservation pending) {
        try {
            OrderInfo order = orderService.getOrder(pending.getId());

            if (order.status() == OrderStatus.COMPLETED) {
                paymentService.cancelBySystem(order.orderId(), "예약일 경과로 인한 대기 예약 자동 환불");
                log.info("환불 성공: 예약ID={}, 주문ID={}", pending.getId(), order.orderId());
            }
            pendingReservationService.cancel(pending.getId(), pending.getName());
        } catch (Exception e) {
            log.error("대기 예약 자동 환불 중 에러 발생! 예약ID={}", pending.getId(), e);
        }
    }
}
