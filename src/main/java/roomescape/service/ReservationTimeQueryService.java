package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.InvalidReferenceException;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationTimeQueryService {

    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAll();
    }

    public List<ReservationTime> findAvailableReservationTimes(LocalDate date, long themeId) {
        validateThemeExists(themeId);
        LocalDateTime now = LocalDateTime.now(clock);
        return reservationTimeDao.findAvailable(date, themeId).stream()
                .filter(time -> !time.isPast(date, now))
                .toList();
    }

    private void validateThemeExists(long themeId) {
        themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }
}
