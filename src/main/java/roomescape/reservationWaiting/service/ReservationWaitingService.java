package roomescape.reservationWaiting.service;

import java.time.Clock;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.reservationWaiting.exception.ReservationWaitingErrorCode;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.BadRequestException;
import roomescape.time.exception.TimeErrorCode;
import roomescape.global.exception.DuplicateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;




import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.theme.domain.Theme;

import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;

import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationTimeRepository reservationTimeRepository,
                                     ThemeRepository themeRepository, ReservationRepository reservationRepository,
                                     Clock clock) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
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

        ReservationTime time = getReservationTime(command.timeId());
        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));

        ReservationWaiting reservationWaiting = ReservationWaiting.of(
                command.name(),
                command.date(),
                time,
                theme
        );

        reservationWaiting.validateExpiry(clock);
        validateTargetReservation(command);

        try {
            return reservationWaitingRepository.save(reservationWaiting);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
        }
    }

    private void validateTargetReservation(ReservationWaitingCommand command) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId()
        ).orElseThrow(() -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND.getMessage()));

        if (reservation.getName().equals(command.name())) {
            throw new BadRequestException(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
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

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage()));
    }

}
