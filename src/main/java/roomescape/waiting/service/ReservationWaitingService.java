package roomescape.waiting.service;

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

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationWaitingResult save(ReservationWaitingCommand command) {
        ReservationWaiting newReservationWaiting = buildValidReservationWaiting(command);

        try {
            ReservationWaiting saved = reservationWaitingRepository.save(newReservationWaiting);
            return ReservationWaitingResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
        }
    }

    private ReservationWaiting buildValidReservationWaiting(ReservationWaitingCommand command) {
        Reservation targetReservation = findTargetReservationByDateAndTimeIdAndThemeId(command);

        validateNotAlreadyReservedOrWaiting(command);

        ReservationWaiting reservationWaiting = ReservationWaiting.of(
                command.name(),
                command.date(),
                targetReservation.getTime(),
                targetReservation.getTheme()
        );
        reservationWaiting.validateExpiry();
        return reservationWaiting;
    }

    private Reservation findTargetReservationByDateAndTimeIdAndThemeId(ReservationWaitingCommand command) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId()
        ).orElseThrow(
                () -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND.getMessage())
        );
    }

    private void validateNotAlreadyReservedOrWaiting(ReservationWaitingCommand command) {
        if (reservationRepository.existsByDateAndTimeIdAndName(command.date(), command.timeId(), command.name())) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
        }
        if (reservationWaitingRepository.existsByDateAndTimeIdAndName(command.date(), command.timeId(),
                command.name())) {
            throw new InvalidBusinessStateException(
                    ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage()); // Or another appropriate error message
        }
    }

    @Transactional
    public void delete(Long id, String userName) {
        ReservationWaiting reservationWaiting = findReservationWaitingById(id);
        reservationWaiting.validateExpiry();
        reservationWaiting.validateOwner(userName);

        int count = reservationWaitingRepository.deleteById(id);
        if (count == 0) {
            throw new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage());
        }
    }

    private ReservationWaiting findReservationWaitingById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage())
                );
    }
}
