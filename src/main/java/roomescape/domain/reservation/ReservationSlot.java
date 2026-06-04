package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.theme.Theme;

@Getter
public class ReservationSlot {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationSlot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot create(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(null, date, time, theme);
    }

    public static ReservationSlot of(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(id, date, time, theme);
    }

    public void validateIsNotInPast(LocalDateTime now) {
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if(date.isBefore(nowDate)) {
            throw new BusinessException();
        }
        if(date.isEqual(nowDate) && time.isBefore(nowTime)) {
            throw new BusinessException();
        }
    }
}
