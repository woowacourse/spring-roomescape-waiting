package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyReservationWithRankResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private Long reservationId;
    private String theme;
    private LocalDate date;
    private String time;
    private String status;
    private Long rank;


    public MyReservationWithRankResponse(final Reservation reservation, final Long rank) {
        this.reservationId = reservation.getId();
        this.theme = reservation.getThemeName();
        this.date = reservation.getDate();
        this.time = reservation.getTime().getStartAt().format(FORMATTER);
        this.status = reservation.getStatus().getValue();
        this.rank = rank;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public Long getRank() {
        return rank;
    }
}
