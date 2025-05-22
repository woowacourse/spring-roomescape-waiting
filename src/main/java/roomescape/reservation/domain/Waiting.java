package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = false)
    private ReservationTime reservationTime;

    @ManyToOne(optional = false)
    private Theme theme;

    private LocalDate date;

    protected Waiting() {}

    public Waiting(Long id, Member member, ReservationTime reservationTime, Theme theme,
        LocalDate date) {
        this.id = id;
        this.member = member;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.date = date;
    }

    public Waiting(Member member, ReservationTime reservationTime, Theme theme, LocalDate date) {
        this(null, member, reservationTime, theme, date);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }
}
