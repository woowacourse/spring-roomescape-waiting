package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.WaitingWithRank;

public class MemberWaitingResponse {

    private long reservationId;
    private String theme;
    private LocalDate date;
    private LocalTime time;
    private String status;

    private MemberWaitingResponse(long reservationId, String theme, LocalDate date, LocalTime time, String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public MemberWaitingResponse(WaitingWithRank waiting) {
        this(waiting.getId(), waiting.getTheme().getName(),
                waiting.getDate(), waiting.getTime().getStartAt(), waiting.getRank() + "번째 예약대기");
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
