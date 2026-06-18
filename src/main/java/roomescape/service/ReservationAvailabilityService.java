package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.service.result.TimeAvailabilityResult;

@Service
@Transactional(readOnly = true)
public class ReservationAvailabilityService {

    private final ReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public ReservationAvailabilityService(ReservationRepository reservationRepository,
                                          JpaReservationTimeRepository reservationTimeRepository,
                                          JpaThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<TimeAvailabilityResult> findAvailableTime(Long themeId, LocalDate date) {
        validateThemeExists(themeId);
        List<ReservationTime> times = reservationTimeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findReservationsByThemeAndDate(themeId, date);

        return times.stream()
                .map(time -> new TimeAvailabilityResult(
                        time.getId(),
                        time.getStartAt(),
                        isAvailable(time, reservations)
                ))
                .toList();
    }

    private boolean isAvailable(ReservationTime time, List<Reservation> reservations) {
        return reservations.stream()
                .noneMatch(reservation -> reservation.hasTime(time));
    }

    private void validateThemeExists(Long themeId) {
        if (themeRepository.findById(themeId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다.");
        }
    }
}
