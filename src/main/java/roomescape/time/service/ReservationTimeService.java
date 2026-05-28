package roomescape.time.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.AvailableTimesResult;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;

@Transactional(readOnly = true)
@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                                  Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationTimeResult save(ReservationTimeCommand command) {
        validateReservationTimeUniqueness(command.startAt());
        ReservationTime reservationTime = ReservationTime.of(command.startAt());

        try {
            ReservationTime saved = reservationTimeRepository.save(reservationTime);
            return ReservationTimeResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }
    }

    private void validateReservationTimeUniqueness(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new DuplicateException(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }
    }

    public List<ReservationTimeResult> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public ReservationTime findById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage()));
    }

    public AvailableTimesResult findAvailableTimes(Long themeId, LocalDate date) {
        ReservationDate.of(date, clock);

        if (themeRepository.findById(themeId).isEmpty()) {
            throw new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
        }

        return new AvailableTimesResult(reservationTimeRepository.findAvailableTimes(themeId, date));
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            int affectedRow = reservationTimeRepository.deleteById(id);
            if (affectedRow == 0) {
                throw new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage());
            }
        } catch (DataIntegrityViolationException e) {
            throw new roomescape.global.exception.DeleteFailedException(TimeErrorCode.TIME_IN_USE.getMessage());
        }
    }
}
