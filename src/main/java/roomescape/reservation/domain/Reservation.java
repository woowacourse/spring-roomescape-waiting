package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.*;
import roomescape.member.domain.Member;

@Entity
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Column(nullable = false)
    private LocalDate reservationDate;
    @ManyToOne @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime reservationTime;
    @ManyToOne @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(
            final Long id,
            final Member member,
            final LocalDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        this.id = id;
        this.member = member;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public Reservation(Member member, LocalDate reservationDate, ReservationTime reservationTime, Theme theme) {
        this(null, member, reservationDate, reservationTime, theme);
    }

    public Long getId() {
        return id;
    }

    public Long getReservationTimeId() {
        return reservationTime.getId();
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public LocalTime getReservationStartTime() {
        return reservationTime.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
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
