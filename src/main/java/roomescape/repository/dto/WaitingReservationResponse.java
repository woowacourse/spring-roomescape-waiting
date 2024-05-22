package roomescape.repository.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import java.time.LocalDate;
import roomescape.domain.reservation.ReservationTime;

public class WaitingReservationResponse {

    private final long id;
    private final String memberName;
    private final String theme;
    private final String date;
    private final String startAt;

    @JsonCreator(mode = Mode.PROPERTIES)
    public WaitingReservationResponse(long id, String memberName, String theme, String date, String startAt) {
        this.id = id;
        this.memberName = memberName;
        this.theme = theme;
        this.date = date;
        this.startAt = startAt;
    }

    public WaitingReservationResponse(long id,
                                      String memberName,
                                      String theme,
                                      LocalDate date,
                                      ReservationTime startAt) {
        this(id, memberName, theme, date.toString(), startAt.getStartAt().toString());
    }

    public long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
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
