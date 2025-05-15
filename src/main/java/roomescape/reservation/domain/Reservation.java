package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;
    @ManyToOne @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(
            final Long id,
            final Member member,
            final ReservationStatus status,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        this.id = id;
        this.member = member;
        this.status = status;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, ReservationStatus.CONFIRMED, date, time, theme);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
