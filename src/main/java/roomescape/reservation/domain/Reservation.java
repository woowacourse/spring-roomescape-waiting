package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member reserver;
    @Embedded
    private ReservationDateTime reservationDatetime;
    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Builder
    private Reservation(
            final Member reserver,
            final ReservationDateTime reservationDateTime,
            final Theme theme
    ) {
        this.reserver = reserver;
        this.reservationDatetime = reservationDateTime;
        this.theme = theme;
    }

    public static Reservation reserve(
            final Member reserver,
            final ReservationDateTime reservationDateTime,
            final Theme theme
    ) {
        return Reservation.builder()
                .reserver(reserver)
                .reservationDateTime(reservationDateTime)
                .theme(theme)
                .build();
    }

    public String getReserverName() {
        return reserver.getName();
    }

    public LocalDate getDate() {
        return reservationDatetime.reservationDate().date();
    }

    public LocalTime getStartAt() {
        return reservationDatetime.reservationTime().getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationDatetime.reservationTime();
    }

    public Long getTimeId() {
        return reservationDatetime.reservationTime().getId();
    }
}
