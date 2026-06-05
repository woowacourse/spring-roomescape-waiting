package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import roomescape.service.dto.MyReservationResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyReservationResponse {

    public enum Status {
        RESERVED, WAITING
    }

    private final Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final Status status;
    private final Integer waitingOrder; // RESERVED면 응답에서 제외

    public MyReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                 ThemeResponse theme, Status status, Integer waitingOrder) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
    }

    public static MyReservationResponse from(MyReservationResult r) {
        return new MyReservationResponse(
                r.getId(),
                r.getDate(),
                ReservationTimeResponse.from(r.getTime()),
                ThemeResponse.from(r.getTheme()),
                Status.valueOf(r.getStatus().name()),
                r.getWaitingOrder()
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public Integer getWaitingOrder() {
        return waitingOrder;
    }
}
