package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.Waiting;

public class MemberWaitingResponse {

    private final long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private MemberWaitingResponse(long reservationId, String theme, LocalDate date, LocalTime time, String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public MemberWaitingResponse(Waiting waiting) {
        this(waiting.getId(), waiting.getTheme().getName(),
                waiting.getDate(), waiting.getTime().getStartAt(), "대기");
    }

    public long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    @JsonFormat(pattern = "HH:mm")
    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
