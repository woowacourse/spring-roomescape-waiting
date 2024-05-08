package roomescape.controller.response;

import java.time.LocalDate;

import roomescape.model.Reservation;
import roomescape.model.Theme;

public class ReservationResponse {

    private Long id;
    private String name;
    private LocalDate date;
    private ReservationTimeResponse time;
    private Theme theme;

    private ReservationResponse() {
    }

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.name = reservation.getMember().getName();
        this.date = reservation.getDate();
        this.time = ReservationTimeResponse.of(reservation.getTime());
        this.theme = reservation.getTheme();
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

    public ReservationTimeResponse getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

}
