package roomescape.reservationtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.dto.ReservationTimeAvailability;
import roomescape.common.exception.DomainException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    private final Clock clock;

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        ReservationTime reservationTime = ReservationTime.create(startAt);
        validateNotDuplicated(reservationTime);

        return reservationTimeRepository.save(reservationTime);
    }

    private void validateNotDuplicated(ReservationTime reservationTime) {
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new DomainException(RESERVATION_TIME_ALREADY_EXISTS);
        }
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new DomainException(RESERVATION_TIME_HAS_RESERVATION);
        }

        if (!reservationTimeRepository.cancelById(id, LocalDateTime.now(clock))) {
            throw new DomainException(RESERVATION_TIME_NOT_FOUND);
        }
    }

    public List<ReservationTimeAvailability> findAvailableTimes(LocalDate date, Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new DomainException(THEME_NOT_FOUND);
        }

        return reservationTimeRepository.findAllByDateAndThemeIdWithAvailability(date, themeId);
    }
}
