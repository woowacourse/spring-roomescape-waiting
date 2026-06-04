package roomescape.time.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.AvailableTimesResult;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.DuplicateTimeException;
import roomescape.time.exception.TimeInUseException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.dto.ReservationTimeCommand;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public ReservationTime registerReservationTime(ReservationTimeCommand command) {
        if (reservationTimeRepository.existByStartAt(command.startAt())) {
            throw new DuplicateTimeException();
        }

        ReservationTime reservationTime = ReservationTime.of(command.startAt());

        try {
            return reservationTimeRepository.save(reservationTime);
        } catch (DuplicateKeyException e) {
            throw new DuplicateTimeException();
        }
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public AvailableTimesResult findAvailableReservationTimes(Long themeId, LocalDate date) {
        if (LocalDate.now(clock).isAfter(date)) {
            throw new InvalidReservationDateValueException();
        }

        if (themeRepository.findById(themeId).isEmpty()) {
            throw new ThemeNotFoundException();
        }

        return new AvailableTimesResult(reservationTimeRepository.findAvailableTimes(themeId, date));
    }

    public void removeReservationTimeById(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new TimeNotFoundException();
        }

        try {
            int affectedRow = reservationTimeRepository.deleteById(id);
            int nonAffected = 0;

            if (affectedRow == nonAffected) {
                throw new TimeNotFoundException();
            }
        } catch (DataIntegrityViolationException e) {
            throw new TimeInUseException();
        }

    }
}
