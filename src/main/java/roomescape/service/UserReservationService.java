package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.payment.Payment;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationUpdateCommand;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.PastReservationException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.UnauthorizedReservationException;

@Transactional(readOnly = true)
@Service
public class UserReservationService {

    private static final Logger log = LoggerFactory.getLogger(UserReservationService.class);

    private final AdminReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final PaymentRepository paymentRepository;

    public UserReservationService(
            AdminReservationService reservationService,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            PaymentRepository paymentRepository
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public ReservationResult create(ReservationCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 예약 생성 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException("존재하지 않는 시간입니다: timeId=" + command.timeId());
                });
        validateNotPast(command.date(), time.getStartAt(), "과거 시점에는 예약할 수 없습니다");

        boolean slotTaken = reservationRepository.existsByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId());
        ReservationStatus status = slotTaken ? ReservationStatus.WAITING : ReservationStatus.PENDING;
        return reservationService.create(command, status);
    }

    public List<ReservationResult> findByName(String name) {
        List<ReservationWithWaitingOrder> reservations = reservationRepository.findByName(name);
        List<Long> reservationIds = reservations.stream()
                .map(ReservationWithWaitingOrder::id)
                .toList();
        Map<Long, Payment> paymentsByReservationId = paymentRepository.findByReservationIds(reservationIds).stream()
                .collect(Collectors.toMap(Payment::reservationId, Function.identity()));

        return reservations.stream()
                .map(reservation -> ReservationResult.from(
                        reservation, paymentsByReservationId.get(reservation.id())))
                .toList();
    }

    @Transactional
    public void cancel(Long id, String name) {
        Reservation reservation = findReservation(id);
        validateOwner(reservation, name);
        validateNotPast(
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "과거 예약은 취소할 수 없습니다"
        );
        reservationRepository.deleteById(id);
    }

    @Transactional
    public ReservationResult update(ReservationUpdateCommand command) {
        Reservation reservation = findReservation(command.id());
        validateOwner(reservation, command.name());
        validateNotPast(
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "이미 지난 예약은 변경할 수 없습니다"
        );

        ReservationTime newTime = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 예약 변경 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException("존재하지 않는 시간입니다: timeId=" + command.timeId());
                });

        validateNotPast(command.date(), newTime.getStartAt(), "과거 시점으로 변경할 수 없습니다");
        validateNoConflict(command, reservation.getTheme().getId());

        Reservation updated = new Reservation(
                reservation.getId(),
                reservation.getName(),
                new ReservationSlot(null, command.date(), newTime, reservation.getTheme()),
                reservation.getStatus()
        );
        return ReservationResult.from(reservationRepository.update(updated));
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 예약 접근 시도: reservationId={}", id);
                    return new ReservationNotFoundException(
                            "존재하지 않는 예약입니다: reservationId=" + id);
                });
    }

    private void validateOwner(Reservation reservation, String name) {
        if (!reservation.getName().equals(name)) {
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
        boolean conflict = reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                command.date(), command.timeId(), themeId, command.id());
        if (conflict) {
            throw new ReservationConflictException(
                    "이미 예약된 시간입니다: %s, timeId=%d, themeId=%d"
                            .formatted(command.date(), command.timeId(), themeId)
            );
        }
    }
}
