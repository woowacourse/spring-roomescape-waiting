package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public class MemberReservationResponse {

    private final long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private MemberReservationResponse(long reservationId, String theme, LocalDate date, LocalTime time, String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public MemberReservationResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getTime().getStartAt(), "예약");
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
