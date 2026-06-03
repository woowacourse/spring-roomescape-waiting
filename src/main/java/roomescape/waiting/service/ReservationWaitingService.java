package roomescape.waiting.service;

import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;
import roomescape.waiting.exception.ReservationWaitingErrorCode;
import roomescape.waiting.service.dto.ReservationWaitingCommand;
import roomescape.waiting.service.dto.ReservationWaitingResult;
import roomescape.time.service.ReservationTimeService;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;

    public ReservationWaitingService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
    }

    @Transactional
    public ReservationWaitingResult save(ReservationWaitingCommand command) {
        ReservationWaiting newReservationWaiting = buildValidReservationWaiting(command);

        try {
            ReservationWaiting saved = reservationWaitingRepository.save(newReservationWaiting);
            return ReservationWaitingResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationWaitingErrorCode.DUPLICATE_WAITING);
        }
    }

    @Transactional
    public void deleteById(Long id, String name) {
        ReservationWaiting reservationWaiting = getById(id);
        reservationWaiting.validateExpiry();
        reservationWaiting.validateOwner(name);

        reservationWaitingRepository.delete(reservationWaiting);
    }

    private ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND)
                );
    }

    private ReservationWaiting buildValidReservationWaiting(ReservationWaitingCommand command) {
        reservationTimeService.getByIdForUpdate(command.timeId());
        Reservation targetReservation = getReservationByDateAndTimeIdAndThemeId(command);

        validateNoDoubleBooking(command.date(), command.timeId(), command.name());

        ReservationWaiting reservationWaiting = ReservationWaiting.of(
                command.name(),
                command.date(),
                targetReservation.getTime(),
                targetReservation.getTheme()
        );
        reservationWaiting.validateExpiry();
        return reservationWaiting;
    }

    private Reservation getReservationByDateAndTimeIdAndThemeId(ReservationWaitingCommand command) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId()
        ).orElseThrow(
                () -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND)
        );
    }

    private void validateNoDoubleBooking(LocalDate date, Long timeId, String name) {
        validateNoSameTimeBooking(date, timeId, name);
        validateNoSameTimeWaiting(date, timeId, name);
    }

    private void validateNoSameTimeBooking(LocalDate date, Long timeId, String name) {
        if (reservationRepository.existsByDateAndTimeIdAndName(date, timeId, name)) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private void validateNoSameTimeWaiting(LocalDate date, Long timeId, String name) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndName(date, timeId, name)) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }
}
