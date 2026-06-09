package roomescape.reservationslot.domain;

import lombok.Getter;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ReservationSlot {

    private static final String DATE_REQUIRED_MESSAGE = "예약일을 입력해야 합니다.";
    private static final String TIME_REQUIRED_MESSAGE = "예약 시간을 선택해야 합니다.";
    private static final String THEME_REQUIRED_MESSAGE = "테마를 선택해야 합니다.";

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationSlot(final Long id, final LocalDate date, final ReservationTime time, final Theme theme) {
        validateRequiredValues(date, time, theme);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot create(final LocalDate date, final ReservationTime time, final Theme theme) {
        return new ReservationSlot(null, date, time, theme);
    }

    public static ReservationSlot of(
            final Long id,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        return new ReservationSlot(id, date, time, theme);
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public LocalDateTime dateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }

    private void validateRequiredValues(final LocalDate date, final ReservationTime time, final Theme theme) {
        if (date == null) {
            throw new IllegalArgumentException(DATE_REQUIRED_MESSAGE);
        }
        if (time == null) {
            throw new IllegalArgumentException(TIME_REQUIRED_MESSAGE);
        }
        if (theme == null) {
            throw new IllegalArgumentException(THEME_REQUIRED_MESSAGE);
        }
    }
}
