package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.TimeAvailability;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationAvailabilityService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationAvailabilityService(
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<TimeAvailability> findAvailableTimes(Long themeId, LocalDate date) {
        validateThemeExists(themeId);
        return reservationTimeRepository.findAvailabilitiesByThemeIdAndDate(themeId, date);
    }

    private void validateThemeExists(Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다.");
        }
    }
}
