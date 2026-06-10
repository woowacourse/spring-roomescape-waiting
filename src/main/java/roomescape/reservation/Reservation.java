package roomescape.reservation;

import java.time.LocalDate;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.time.ReservationTime;

import static roomescape.global.exception.ErrorCode.FORBIDDEN_RESERVATION_ACCESS;

public class Reservation {
    private Long id;
    private final String name;
    private final Long themeId;
    private final LocalDate date;
    private final ReservationTime time;

    public Reservation(String name, Long themeId, LocalDate date, ReservationTime time) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.themeId = themeId;
    }

    public Reservation(Long id, String name, Long themeId, LocalDate date, ReservationTime time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.themeId = themeId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void validateSameName(String name) {
        if (!this.name.equals(name)) {
            throw new RoomescapeException(FORBIDDEN_RESERVATION_ACCESS);
        }
    }
}
