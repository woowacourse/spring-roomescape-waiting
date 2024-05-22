package roomescape.repository.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import java.time.LocalDate;
import roomescape.domain.reservation.ReservationTime;

public class WaitingReservationResponse {

    private final long id;
    private final String name;
    private final String theme;
    private final String date;
    private final String startAt;

    @JsonCreator(mode = Mode.PROPERTIES)
    public WaitingReservationResponse(long id, String name, String theme, String date, String startAt) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.date = date;
        this.startAt = startAt;
    }

    public WaitingReservationResponse(long id, String name, String theme, LocalDate date, ReservationTime startAt) {
        this(id, name, theme, date.toString(), startAt.getStartAt().toString());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public String getStartAt() {
        return startAt;
    }
}
