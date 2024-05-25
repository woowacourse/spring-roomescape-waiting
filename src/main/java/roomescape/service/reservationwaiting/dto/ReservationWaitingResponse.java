package roomescape.service.reservationwaiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public class ReservationWaitingResponse {
    private final Long id;
    private final String name;
    private final String theme;
    private final LocalDate date;
    private final LocalTime startAt;
    private final Long reservationId;

    public ReservationWaitingResponse(
            Long id, String name, String theme, LocalDate date, LocalTime startAt, Long reservationId) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.date = date;
        this.startAt = startAt;
        this.reservationId = reservationId;
    }

    public ReservationWaitingResponse(ReservationWaiting waiting) {
        this(waiting.getId(),
                waiting.getMember().getName().getName(),
                waiting.getReservation().getTheme().getName().getName(),
                waiting.getReservation().getDate(),
                waiting.getReservation().getTime().getStartAt(),
                waiting.getReservation().getId()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
