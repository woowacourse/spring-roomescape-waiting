package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public Waiting(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this(null, date, reservationTime, theme, member);
    }

    public boolean hasNoAuthToDeleteThis(Member member) {
        return !(member.isAdmin()) && !(Objects.equals(this.member, member));
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDate(DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
