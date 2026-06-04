package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationUpdateCommand;
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
    private final WaitingRepository waitingRepository;

    public UserReservationService(
            AdminReservationService reservationService,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResult create(ReservationCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 시간으로 예약 생성 시도: timeId={}", command.timeId());
                    return new ReservationTimeNotFoundException("존재하지 않는 시간입니다: timeId=" + command.timeId());
                });
        validateNotPast(command.date(), time.getStartAt(), "과거 시점에는 예약할 수 없습니다");
        return reservationService.reserveOnSlot(command);
    }

    public List<ReservationResult> findByName(String name) {
        return reservationRepository.findByName(name).stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public void cancel(Long id, String name) {
        Reservation target = findByIdAndNameFromEither(id, name);

        validateNotPast(target.getDate(), target.getTime().getStartAt(), "과거 예약은 취소할 수 없습니다");

        executeDeletion(id, name, target.getDate(), target.getTime().getId(), target.getTheme().getId());
    }

    private Reservation findByIdAndNameFromEither(Long id, String name) {
        return reservationRepository.findById(id)
                .filter(r -> r.isOwnedBy(name))
                .or(() -> waitingRepository.findById(id).filter(w -> w.isOwnedBy(name)))
                .orElseThrow(() -> diagnoseError(id));
    }

    private void executeDeletion(Long id, String name, LocalDate date, Long timeId, Long themeId) {
        boolean isConfirmed = reservationRepository.findById(id)
                .filter(r -> r.isOwnedBy(name))
                .isPresent();

        if (isConfirmed) {
            reservationRepository.deleteById(id);
            waitingRepository.findFirstWaiting(date, timeId, themeId)
                    .ifPresent(this::promoteWaitingToReservation);
            return;
        }

        cancelWaiting(id);
        log.info("사용자 대기 취소 완료: waitingId={}, name={}", id, name);
    }

    private void promoteWaitingToReservation(Reservation waiting) {
        reservationRepository.save(waiting);
        cancelWaiting(waiting.getId());
    }

    private void cancelWaiting(Long waitingId) {
        waitingRepository.deleteById(waitingId);
    }

    private RuntimeException diagnoseError(Long id) {
        if (reservationRepository.existsById(id) || waitingRepository.findById(id).isPresent()) {
            return new UnauthorizedReservationException("본인의 예약이 아닙니다");
        }
        return new ReservationNotFoundException("존재하지 않는 예약입니다: id=" + id);
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
                command.date(),
                newTime,
                reservation.getTheme()
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
        if (!reservation.isOwnedBy(name)) {
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
