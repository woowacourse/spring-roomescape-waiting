package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Getter
public class Reservation {

    private final Long id;
    private final Member reserver;
    private final ReservationDateTime reservationDatetime;
    private final Theme theme;

    private Reservation(
            Long id,
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme
    ) {
        this.id = id;
        this.reserver = reserver;
        this.reservationDatetime = reservationDateTime;
        this.theme = theme;
    }

    public static Reservation reserve(
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme
    ) {
        return new Reservation(null, reserver, reservationDateTime, theme);
    }

    public static Reservation load(
            Long id,
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme
    ) {
        return new Reservation(id, reserver, reservationDateTime, theme);
    }

    public String getReserverName() {
        return reserver.getName();
    }

    public LocalDate getDate() {
        return reservationDatetime.getReservationDate().getDate();
    }

    public LocalTime getStartAt() {
        return reservationDatetime.getReservationTime().getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationDatetime.getReservationTime();
    }

    public Long getTimeId() {
        return reservationDatetime.getReservationTime().getId();
    }
}
