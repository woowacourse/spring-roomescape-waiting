package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member reserver;
    @Embedded
    private ReservationDateTime reservationDatetime;
    @JoinColumn(name = "theme_id")
    @ManyToOne
    private Theme theme;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    private Reservation(
            Long id,
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme,
            ReservationStatus status
    ) {
        this.id = id;
        this.reserver = reserver;
        this.reservationDatetime = reservationDateTime;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation reserve(
            Member reserver,
            ReservationDateTime reservationDateTime,
            Theme theme
    ) {
        return new Reservation(null, reserver, reservationDateTime, theme, ReservationStatus.RESERVED);
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
