package roomescape.controller.response;

import roomescape.model.Reservation;

import java.time.LocalDate;

public class ReservationResponse {

    private Long id;
    private String name;
    private LocalDate date;
    private ReservationTimeResponse time;
    private ReservationThemeResponse theme;

    private ReservationResponse() {
    }

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.name = reservation.getMember().getName();
        this.date = reservation.getDate();
        this.time = ReservationTimeResponse.of(reservation.getTime());
        this.theme = ReservationThemeResponse.of(reservation.getTheme());
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

    public ReservationThemeResponse getTheme() {
        return theme;
    }

}
