package roomescape.application.reservation;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.ThemeSchedule;

@Component
public class ThemeScheduleReader {

    private final ReservationTimeRepository reservationTImeRepository;
    private final ThemeRepository themeRepository;

    public ThemeScheduleReader(ReservationTimeRepository reservationTImeRepository, ThemeRepository themeRepository) {
        this.reservationTImeRepository = reservationTImeRepository;
        this.themeRepository = themeRepository;
    }

    public ThemeSchedule getThemeSchedule(LocalDate date, Long themeId, Long timeId) {
        return new ThemeSchedule(date, getReservationTimeById(timeId), getThemeById(themeId));
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTImeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundEntityException(timeId + "에 해당하는 reservation_time 튜플이 없습니다."));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundEntityException(themeId + "에 해당하는 theme 튜플이 없습니다."));
    }
}

