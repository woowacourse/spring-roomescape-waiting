package roomescape.reservationWaiting.service;

import java.time.Clock;
import org.springframework.dao.DataIntegrityViolationException;
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

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     Clock clock) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaiting save(ReservationWaitingCommand command) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                command.date(), command.timeId(), command.themeId(), command.name())
        ) {
            throw new DuplicateException(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
        }

        Reservation targetReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId()
        ).orElseThrow(
                () -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND.getMessage()));

        if (targetReservation.hasSameName(command.name())) {
            throw new BadRequestException(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
        }

        ReservationWaiting reservationWaiting = ReservationWaiting.of(
                command.name(),
                command.date(),
                targetReservation.getTime(),
                targetReservation.getTheme()
        );

        reservationWaiting.validateExpiry(clock);

        try {
            return reservationWaitingRepository.save(reservationWaiting);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
        }
    }

    @Transactional
    public void delete(Long id, String userName) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage())
        );

        reservationWaiting.validateExpiry(clock);
        reservationWaiting.validateOwner(userName);

        int count = reservationWaitingRepository.deleteById(id);
        if (count == 0) {
            throw new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage());
        }
    }
}
