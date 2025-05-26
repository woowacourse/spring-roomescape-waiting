package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.ReservationInformation;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationInformation reservationInformation;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    protected Reservation() {
    }

    public Reservation(
            final Member member,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        this(member, new ReservationInformation(date, time, theme));
    }

    public Reservation(
            final Long id,
            final Member member,
            final ReservationInformation reservationInformation
    ) {
        this.id = id;
        this.member = member;
        this.reservationInformation = reservationInformation;
    }

    public Reservation(Member member, ReservationInformation reservationInformation) {
        this(null, member, reservationInformation);
    }

    public static Reservation of(Waiting waiting) {
        return new Reservation(
                waiting.getMember(),
                waiting.getReservationInformation()
        );
    }

    public boolean isBefore(LocalDateTime compare) {
        return reservationInformation.isBefore(compare);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return reservationInformation.getDate();
    }

    public ReservationTime getTime() {
        return reservationInformation.getTime();
    }

    public Theme getTheme() {
        return reservationInformation.getTheme();
    }
}
