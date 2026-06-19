package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.client.TossConfirmResultUnknownException;
import roomescape.client.TossConnectionException;
import roomescape.client.TossPaymentGateway;
import roomescape.client.dto.PaymentConfirmation;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.controller.client.dto.response.PreparePaymentResponse;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.EntityNotFoundException;
import roomescape.query.ReservationQueryRepository;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.command.ReservationCommand;
import roomescape.service.result.OrderHistoryResult;
import roomescape.service.result.PaymentConfirmResult;
import roomescape.service.result.ReservationResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final ReservationService reservationService;
    private final ReservationQueryRepository reservationQueryRepository;
    private final Clock clock;

    // 자기 자신의 트랜잭션 프록시를 통해 persistConfirmedPayment()를 호출하기 위한 self-injection.
    // (같은 클래스 내부 호출은 @Transactional AOP를 우회하므로 프록시를 거치게 한다.)
    @Autowired
    @Lazy
    private PaymentService self;

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
        TossPaymentResponse response;
        try {
            response = tossPaymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        } catch (TossConfirmResultUnknownException e) {
            // 응답을 받지 못해 승인 여부 불명 - 상태만 기록하고 entry는 PENDING으로 유지(재시도 가능). 예외는 그대로 전파.
            paymentOrderRepository.update(paymentOrder.resultUnknown());
            log.warn("[결제 승인 결과 확인 불가] orderId={} message={}", orderId, e.getMessage());
            throw e;
        } catch (TossConnectionException e) {
            // 요청이 토스에 도달하지 못해 미승인이 확실한 경우 - FAILED로 기록. 예외는 그대로 전파.
            paymentOrderRepository.update(paymentOrder.failed());
            log.warn("[결제 서버 연결 실패] orderId={} message={}", orderId, e.getMessage());
            throw e;
        }
        log.info("[결제 승인 완료] orderId={} status={} approvedAt={}", response.orderId(), response.status(),
                response.approvedAt());

        // PaymentOrder 확정 기록과 예약 엔트리 확정을 하나의 트랜잭션으로 묶어 원자적으로 반영한다.
        ReservationResult reservationResult = self.persistConfirmedPayment(paymentOrder, paymentKey);

        log.info("[예약 확정] entryId={} PENDING→RESERVED", paymentOrder.getEntryId());

        return new PaymentConfirmResult(
                response,
                reservationResult.theme().name(),
                reservationResult.theme().thumbnailImageUrl(),
                reservationResult.date(),
                reservationResult.time().startAt()
        );
    }

    public List<OrderHistoryResult> getOrderHistories(String name) {
        return reservationQueryRepository.getOrderHistories(name);
    }

    @Transactional
    public ReservationResult persistConfirmedPayment(PaymentOrder paymentOrder, String paymentKey) {
        paymentOrderRepository.update(paymentOrder.confirmed(paymentKey));
        return reservationService.confirmPendingEntry(paymentOrder.getEntryId());
    }

    @Transactional
    public void cancel(String orderId) {
        if (orderId == null) {
            return;
        }

        paymentOrderRepository.findByOrderId(orderId).ifPresent(paymentOrder -> {
            Reservation reservation = reservationRepository.getByEntryIdForUpdate(paymentOrder.getEntryId());
            if (!reservation.findActiveEntry(paymentOrder.getEntryId()).isPending()) {
                // 이미 confirm()으로 RESERVED까지 확정된 결제 - cancel이 뒤늦게 들어와도 상태를 덮어쓰지 않는다.
                log.info("[결제 실패 정리 스킵] orderId={} entryId={} 이미 PENDING이 아님", orderId, paymentOrder.getEntryId());
                return;
            }

            reservation.cancelPendingEntry(paymentOrder.getEntryId());
            reservationRepository.update(reservation);
            paymentOrderRepository.update(paymentOrder.failed());
            log.info("[결제 실패 정리] orderId={} entryId={} PENDING→DELETED", orderId, paymentOrder.getEntryId());
        });
    }

    @Transactional
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
