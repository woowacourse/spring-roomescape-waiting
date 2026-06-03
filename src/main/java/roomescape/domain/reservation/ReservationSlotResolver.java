package roomescape.domain.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Component
@RequiredArgsConstructor
public class ReservationSlotResolver {

    private final ReservationDateRepository reservationDateRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationSlot resolve(Long dateId, Long timeId, Long themeId) {
        Theme theme = getTheme(themeId);
        return resolveWithTheme(dateId, timeId, theme);
    }

    public ReservationSlot resolveWithTheme(Long dateId, Long timeId, Theme theme) {
        ReservationDate date = getReservationDate(dateId);
        ReservationTime time = getReservationTime(timeId);
        return ReservationSlot.of(date, time, theme);
    }

    private ReservationDate getReservationDate(Long id) {
        return reservationDateRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_EXIST));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_EXIST));
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ThemeErrorCode.THEME_NOT_EXIST));
    }
}
