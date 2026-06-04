package roomescape.reservationWaiting.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.auth.exception.AuthorizationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.AlreadyReservedSameSlotException;
import roomescape.reservationWaiting.exception.DuplicateReservationWaitingException;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.exception.ReservationWaitingTargetNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.InvalidTimeStartAtValueException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     ReservationTimeRepository reservationTimeRepository,
                                     ThemeRepository themeRepository,
                                     Clock clock) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public ReservationWaiting makeReservationWaiting(ReservationWaitingCommand command) {
        if (reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(
                command.date(), command.timeId(), command.themeId(), command.name()
        )) {
            throw new DuplicateReservationWaitingException();
        }

        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(TimeNotFoundException::new);

        validateExpiry(command.date(), time.getStartAt());

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(ThemeNotFoundException::new);

        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId()
        ).orElseThrow(ReservationWaitingTargetNotFoundException::new);

        if (reservation.hasSameName(command.name())) {
            throw new AlreadyReservedSameSlotException();
        }

        try {
            return reservationWaitingRepository.save(
                    ReservationWaiting.of(
                            command.name(),
                            command.date(),
                            time,
                            theme
                    )
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationWaitingException();
        }
    }

    private void validateExpiry(LocalDate date, LocalTime startAt) {
        LocalDate nowDate = LocalDate.now(clock);

        if (nowDate.isAfter(date)) {
            throw new InvalidReservationDateValueException();
        }

        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(startAt)) {
            throw new InvalidTimeStartAtValueException();
        }
    }

    public void deleteReservationWaitingById(Long id, String name) {
        ReservationWaiting reservationWaiting = getReservationWaiting(id);

        if (!reservationWaiting.hasSameName(name)) {
            throw new AuthorizationException();
        }

        validateExpiry(
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt()
        );

        int affectedRow = reservationWaitingRepository.deleteById(id);
        int nonAffected = 0;

        if (affectedRow == nonAffected) {
            throw new ReservationWaitingNotFoundException();
        }
    }

    private ReservationWaiting getReservationWaiting(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(ReservationWaitingNotFoundException::new);
    }
}
