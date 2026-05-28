package roomescape.reservationWaiting.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.ReservationWaitingErrorCode;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.reservationWaiting.service.dto.ReservationWaitingResult;

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
        ReservationWaiting newReservationWaiting = consistValidReservationWaiting(command);

        try {
            ReservationWaiting saved = reservationWaitingRepository.save(newReservationWaiting);
            return ReservationWaitingResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
        }
    }

    private ReservationWaiting consistValidReservationWaiting(ReservationWaitingCommand command) {
        validateReservationWaitingUniqueness(command);
        Reservation targetReservation = findTargetReservationByDateAndTimeIdAndThemeId(command);

        if (targetReservation.hasSameName(command.name())) {
            throw new BadRequestException(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
        }

        ReservationWaiting reservationWaiting = ReservationWaiting.of(
                command.name(),
                command.date(),
                targetReservation.time(),
                targetReservation.theme()
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

    private void validateReservationWaitingUniqueness(ReservationWaitingCommand command) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                command.date(), command.timeId(), command.themeId(), command.name())
        ) {
            throw new DuplicateException(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
        }
    }

    @Transactional
    public void delete(Long id, String userName) {
        ReservationWaiting reservationWaiting = findReservationWatingById(id);
        reservationWaiting.validateExpiry();
        reservationWaiting.validateOwner(userName);

        int count = reservationWaitingRepository.deleteById(id);
        if (count == 0) {
            throw new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage());
        }
    }

    @NonNull
    private ReservationWaiting findReservationWatingById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage())
                );
    }
}
