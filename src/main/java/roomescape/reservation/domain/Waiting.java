package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    private LocalDate date;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private ReservationTime reservationTime;

    protected Waiting() {
    }

    public Waiting(final Member member, final LocalDate date, final Theme theme,
            final ReservationTime reservationTime) {
        this(null, member, date, theme, reservationTime);
    }

    public Waiting(final Long id, final Member member, final LocalDate date, final Theme theme,
            final ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
