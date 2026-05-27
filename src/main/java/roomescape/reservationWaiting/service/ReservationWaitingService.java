package roomescape.reservationWaiting.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.AuthorizationException;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.AlreadyReservedException;
import roomescape.reservationWaiting.exception.DuplicateReservationWaitingException;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.exception.WaitingTargetReservationNotFoundException;
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

    public ReservationWaiting save(ReservationWaitingCommand command) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(
                command.date(), command.timeId(), command.themeId(), command.name())
        ) {
            throw new DuplicateReservationWaitingException();
        }

        ReservationTime time = getReservationTime(command.timeId());
        validateExpiry(command.date(), time.getStartAt());

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(ThemeNotFoundException::new);

        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId()
        ).orElseThrow(WaitingTargetReservationNotFoundException::new);

        if (reservation.getName().equals(command.name())) {
            throw new AlreadyReservedException();
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
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationWaitingException();
        }
    }

    @Transactional
    public void delete(Long id, String userName) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(id).orElseThrow(
                ReservationWaitingNotFoundException::new
        );

        validateExpiry(reservationWaiting.getDate(), reservationWaiting.getTime().getStartAt());
        if (!reservationWaiting.getName().equals(userName)) {
            throw new AuthorizationException();
        }

        int count = reservationWaitingRepository.deleteById(id);
        if (count == 0) {
            throw new ReservationWaitingNotFoundException();
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(TimeNotFoundException::new);
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

}
