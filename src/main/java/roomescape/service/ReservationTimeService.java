package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.repository.dto.ReservationTimeAvailability;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        validateNotDuplicated(reservationTime);

        return reservationTimeRepository.save(reservationTime);
    }

    private void validateNotDuplicated(ReservationTime reservationTime) {
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new DomainException(ErrorCode.RESERVATION_TIME_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new DomainException(ErrorCode.RESERVATION_TIME_HAS_RESERVATION);
        }

        if (!reservationTimeRepository.deleteById(id)) {
            throw new DomainException(ErrorCode.RESERVATION_TIME_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeAvailability> findAvailableTimes(LocalDate date, Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new DomainException(ErrorCode.THEME_NOT_FOUND);
        }

        return reservationTimeRepository.findAllByDateAndThemeIdWithAvailability(date, themeId);
    }
}
