package roomescape.waiting.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationRequestLockRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;
import roomescape.waiting.exception.ReservationWaitingErrorCode;
import roomescape.waiting.service.dto.ReservationWaitingCommand;
import roomescape.waiting.service.dto.ReservationWaitingResult;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationRequestLockRepository reservationRequestLockRepository;

    public ReservationWaitingService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            ReservationRequestLockRepository reservationRequestLockRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationRequestLockRepository = reservationRequestLockRepository;
    }

    @Transactional
    public ReservationWaitingResult save(ReservationWaitingCommand command, LocalDateTime requestTime) {
        reservationRequestLockRepository.lock(command.name(), command.date(), command.timeId());
        ReservationWaiting newReservationWaiting = createWaiting(command, requestTime);
        validateWaiting(newReservationWaiting);

        try {
            ReservationWaiting saved = reservationWaitingRepository.save(newReservationWaiting);
            return ReservationWaitingResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationWaitingErrorCode.DUPLICATE_WAITING);
        }
    }

    @Transactional
    public void deleteOwnedWaitingById(long id, String name, LocalDateTime requestTime) {
        ReservationWaiting reservationWaiting = getById(id);
        reservationWaiting.validateDeletable(name, requestTime);

        reservationWaitingRepository.delete(reservationWaiting);
    }

    private ReservationWaiting getById(long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND)
                );
    }

    private ReservationWaiting createWaiting(ReservationWaitingCommand command, LocalDateTime requestTime) {
        ReservationTime time = reservationTimeService.getById(command.timeId());
        Theme theme = themeService.findById(command.themeId());

        return new ReservationWaiting(
                command.name(),
                command.date(),
                time,
                theme,
                requestTime
        );
    }

    private void validateWaiting(ReservationWaiting reservationWaiting) {
        Reservation targetReservation = validateTargetReservationExists(reservationWaiting.getSlot());
        validateNoSameTimeReservation(reservationWaiting);
        boolean hasDuplicateWaiting = reservationWaitingRepository.hasWaitingAtSameTime(reservationWaiting);
        reservationWaiting.validateNoConflictWithReservation(targetReservation);
        reservationWaiting.validateNoDuplicateWaiting(hasDuplicateWaiting);
    }

    private void validateNoSameTimeReservation(ReservationWaiting reservationWaiting) {
        List<Reservation> reservations = reservationRepository.findAllByName(reservationWaiting.getName());
        boolean hasSameTimeReservation = reservations.stream()
                .anyMatch(reservation ->
                        reservation.getDate().equals(reservationWaiting.getDate())
                                && reservation.getTimeId().equals(reservationWaiting.getTime().getId())
                );

        if (hasSameTimeReservation) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private Reservation validateTargetReservationExists(ReservationSlot slot) {
        return reservationRepository.findBySlot(slot)
                .orElseThrow(() -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND));
    }
}
