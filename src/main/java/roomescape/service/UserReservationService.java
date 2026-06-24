package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentService;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.PaymentConfirmCommand;
import roomescape.service.dto.PaymentOrderResult;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationUpdateCommand;
import roomescape.service.exception.PastReservationException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;
import roomescape.service.exception.UnauthorizedReservationException;

@Service
public class UserReservationService {

    private static final Logger log = LoggerFactory.getLogger(UserReservationService.class);

    private static final long RESERVATION_AMOUNT = 1000L;
    private static final String ORDER_NAME = "방탈출 예약";

    private final AdminReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final PaymentService paymentService;
    private final PaymentOrderRepository paymentOrderRepository;

    public UserReservationService(
            AdminReservationService reservationService,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            PaymentService paymentService,
            PaymentOrderRepository paymentOrderRepository
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.paymentService = paymentService;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    public PaymentOrderResult createOrder(ReservationCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 주문 생성 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException(
                            "존재하지 않는 시간입니다: timeId=" + command.timeId());
                });
        validateNotPast(command.date(), time.getStartAt(), "과거 시점에는 예약할 수 없습니다");

        if (!themeRepository.existsById(command.themeId())) {
            log.warn("존재하지 않는 테마로 주문 생성 시도: themeId={}", command.themeId());
            throw new ThemeNotFoundException("존재하지 않는 테마입니다: themeId=" + command.themeId());
        }

        String orderId = generateOrderId();
        paymentOrderRepository.save(PaymentOrder.pending(orderId, command, RESERVATION_AMOUNT));
        log.info("주문 생성 완료: orderId={}, reserverName={}, date={}, timeId={}, themeId={}, amount={}",
                orderId, command.reserverName(), command.date(), command.timeId(), command.themeId(),
                RESERVATION_AMOUNT);
        return new PaymentOrderResult(orderId, RESERVATION_AMOUNT, ORDER_NAME);
    }

    public ReservationResult confirm(PaymentConfirmCommand command) {
        PaymentResult payment = paymentService.confirm(
                command.paymentKey(), command.orderId(), command.amount());
        PaymentOrder order = paymentOrderRepository.getByOrderId(command.orderId());
        ReservationResult reservation = reservationService.create(order.toCommand());
        paymentOrderRepository.markConfirmed(command.orderId(), payment.paymentKey(), reservation.id());
        log.info("결제 승인 후 예약 생성 완료: orderId={}, reservationId={}, status={}",
                command.orderId(), reservation.id(), reservation.status());
        return reservation;
    }

    public void cancelOrder(String orderId) {
        paymentOrderRepository.markCanceled(orderId);
        log.info("결제 대기 주문 취소: orderId={}", orderId);
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }

    public List<ReservationResult> findByReserverName(String reserverName) {
        return reservationRepository.findByReserverName(reserverName).stream()
                .map(ReservationResult::from)
                .toList();
    }
    public void cancel(Long id, String reserverName) {
        Reservation reservation = findReservation(id);
        validateOwner(reservation, reserverName);
        validateNotPast(
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "과거 예약은 취소할 수 없습니다"
        );
        reservationService.cancel(id);
    }

    @Transactional
    public ReservationResult update(ReservationUpdateCommand command) {
        Reservation reservation = findReservation(command.id());
        validateOwner(reservation, command.reserverName());
        validateNotPast(
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "과거 예약은 변경할 수 없습니다"
        );

        ReservationTime newTime = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 예약 변경 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException(
                            "존재하지 않는 시간입니다: timeId=" + command.timeId());
                });
        validateNotPast(command.date(), newTime.getStartAt(), "과거 시점으로 변경할 수 없습니다");

        boolean slotChanged = !(reservation.getDate().equals(command.date())
                && reservation.getTime().getId().equals(command.timeId()));
        if (!slotChanged) {
            return ReservationResult.from(loadWithWaitingOrder(command.id()));
        }

        Long themeId = reservation.getTheme().getId();
        return reservationRepository.executeWithThemeLock(themeId, (lockedTheme, writer) -> {
            validateNoConflict(command, themeId);

            LocalDate oldDate = reservation.getDate();
            Long oldTimeId = reservation.getTime().getId();
            boolean wasConfirmed = reservation.isConfirmed();

            ReservationStatus newStatus;
            if (reservationRepository.existsActiveConfirmed(command.date(), command.timeId(), themeId)) {
                newStatus = ReservationStatus.WAITING;
            } else {
                newStatus = ReservationStatus.CONFIRMED;
            }

            Reservation updated = new Reservation(
                    reservation.getId(),
                    reservation.getReserverName(),
                    command.date(),
                    newTime,
                    reservation.getTheme(),
                    newStatus
            );
            ReservationWithWaitingOrder result = writer.updateAndRequeue(updated);

            if (wasConfirmed) {
                writer.promoteEarliestWaiting(oldDate, oldTimeId, themeId);
            }
            return ReservationResult.from(result);
        });
    }

    private ReservationWithWaitingOrder loadWithWaitingOrder(Long id) {
        return reservationRepository.findWithWaitingOrderById(id)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "존재하지 않는 예약입니다: reservationId=" + id));
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 예약 접근 시도: reservationId={}", id);
                    return new ReservationNotFoundException(
                            "존재하지 않는 예약입니다: reservationId=" + id);
                });
    }

    private void validateOwner(Reservation reservation, String reserverName) {
        if (!reservation.getReserverName().equals(reserverName)) {
            throw new UnauthorizedReservationException("본인의 예약이 아닙니다");
        }
    }

    private void validateNotPast(LocalDate date, LocalTime startAt, String message) {
        LocalDateTime reservationAt = LocalDateTime.of(date, startAt);
        if (reservationAt.isBefore(LocalDateTime.now())) {
            throw new PastReservationException(message);
        }
    }

    private void validateNoConflict(ReservationUpdateCommand command, Long themeId) {
        boolean conflict = reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                command.reserverName(), command.date(), command.timeId(), themeId, command.id());
        if (conflict) {
            throw new ReservationConflictException(
                    "이미 본인이 예약 또는 대기중인 시간입니다: %s, timeId=%d, themeId=%d"
                            .formatted(command.date(), command.timeId(), themeId)
            );
        }
    }
}
