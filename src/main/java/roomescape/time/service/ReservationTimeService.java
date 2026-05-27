package roomescape.time.service;

import roomescape.global.exception.InvalidRequestValueException;

import java.time.Clock;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.BadRequestException;
import roomescape.time.exception.TimeErrorCode;
import roomescape.global.exception.DuplicateException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.AvailableTimesResult;
import roomescape.time.domain.ReservationTime;



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

    @Transactional
    public ReservationTime save(ReservationTimeCommand command) {
        if (reservationTimeRepository.existsByStartAt(command.startAt())) {
            throw new DuplicateException(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }

        ReservationTime reservationTime = ReservationTime.of(command.startAt());

        try {
            return reservationTimeRepository.save(reservationTime);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public AvailableTimesResult findAvailableTimes(Long themeId, LocalDate date) {
        if (LocalDate.now(clock).isAfter(date)) {
            throw new InvalidRequestValueException(ReservationErrorCode.INVALID_DATE.getMessage());
        }

        if (themeRepository.findById(themeId).isEmpty()) {
            throw new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
        }

        return new AvailableTimesResult(reservationTimeRepository.findAvailableTimes(themeId, date));
    }

    @Transactional
    public void deleteById(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage());
        }

        try {
            int affectedRow = reservationTimeRepository.deleteById(id);
            int nonAffected = 0;

            if (affectedRow == nonAffected) {
                throw new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage());
            }
        } catch (DataIntegrityViolationException e) {
            throw new roomescape.global.exception.DeleteFailedException(TimeErrorCode.TIME_IN_USE.getMessage());
        }

    }
}
