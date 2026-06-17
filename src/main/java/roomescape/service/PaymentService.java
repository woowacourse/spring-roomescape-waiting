package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.client.TossPaymentGateway;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.controller.client.dto.response.PreparePaymentResponse;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.EntityNotFoundException;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.command.ReservationCommand;
import roomescape.service.result.PaymentConfirmResult;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final Clock clock;

    @Value("${toss.client-key}")
    private String clientKey;

    public PaymentConfirmResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

        if (!paymentOrder.getAmount().equals(amount)) {
            log.warn("[결제 금액 불일치] orderId={} 저장={} 요청={}", orderId, paymentOrder.getAmount(), amount);
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        log.info("[결제 승인 요청] orderId={} paymentKey={} amount={}", orderId, paymentKey, amount);
        TossPaymentResponse response = tossPaymentGateway.confirm(paymentKey, orderId, amount);
        log.info("[결제 승인 완료] orderId={} status={} approvedAt={}", response.orderId(), response.status(), response.approvedAt());

        Reservation reservation = reservationRepository.getByEntryIdForUpdate(paymentOrder.getEntryId());
        reservation.confirmPendingEntry(paymentOrder.getEntryId());
        reservationRepository.update(reservation);

        log.info("[예약 확정] entryId={} PENDING→RESERVED", paymentOrder.getEntryId());
        
        return new PaymentConfirmResult(
                response,
                reservation.getTheme().getName(),
                reservation.getTheme().getThumbnailImageUrl(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }

    public void cancel(String orderId) {
        if (orderId == null) {
            return;
        }

        paymentOrderRepository.findByOrderId(orderId).ifPresent(paymentOrder -> {
            Reservation reservation = reservationRepository.getByEntryIdForUpdate(paymentOrder.getEntryId());
            reservation.cancelEntry(paymentOrder.getEntryId());
            reservationRepository.update(reservation);
            log.info("[결제 실패 정리] orderId={} entryId={} PENDING→DELETED", orderId, paymentOrder.getEntryId());
        });
    }

    public PreparePaymentResponse prepare(ReservationCommand command) {
        Reservation reservation = findOrCreateSlotForUpdate(command);
        reservation.addPendingEntry(command.name(), command.amount(), LocalDateTime.now(clock));
        Reservation saved = reservationRepository.save(reservation);
        ReservationEntry savedEntry = saved.findEntryByNameAndStatus(command.name(), ReservationStatus.PENDING);

        String orderId = UUID.randomUUID().toString();
        PaymentOrder paymentOrder = PaymentOrder.create(orderId, command.amount(), savedEntry.getId(), LocalDateTime.now(clock));
        paymentOrderRepository.save(paymentOrder);

        String orderName = reservation.getTheme().getName()
                + " (" + reservation.getDate() + " " + reservation.getTime().getStartAt() + ")";
        return new PreparePaymentResponse(orderId, command.amount(), orderName, clientKey);
    }

    private Reservation findOrCreateSlotForUpdate(ReservationCommand command) {
        return reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition())
                .orElseGet(() -> {
                    Theme theme = themeRepository.findById(command.themeId())
                            .filter(Theme::isActive)
                            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마 정보입니다."));
                    ReservationTime time = reservationTimeRepository.findById(command.timeId())
                            .filter(ReservationTime::isActive)
                            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간 정보입니다."));
                    return Reservation.createSlot(command.date(), theme, time);
                });
    }
}
