package roomescape.repository.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationTime;

public class ReservationWaitingResponse {

    private final long id;
    private final String name;
    private final String theme;
    private final String date;
    private final String startAt;

    public ReservationWaitingResponse(long id, String name, String theme, LocalDate date, ReservationTime startAt) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.date = date.toString();
        this.startAt = startAt.getStartAt().toString();
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
