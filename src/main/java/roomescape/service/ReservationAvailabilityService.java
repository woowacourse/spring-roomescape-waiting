package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.result.TimeAvailabilityResult;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationAvailabilityService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationAvailabilityService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
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
            throw new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다.");
        }
    }
}
