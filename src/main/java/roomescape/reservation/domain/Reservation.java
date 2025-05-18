package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.InvalidReservationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public Reservation(final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme, final ReservationStatus reservationStatus) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = reservationStatus;
    }

    protected Reservation() {
    }

    public static Reservation createUpcomingReservationWithUnassignedId(final Member member, final LocalDate date,
                                                                        final ReservationTime time,
                                                                        final Theme theme, final LocalDateTime now,
                                                                        final ReservationStatus reservationStatus) {
        validateDateTime(date, time.getStartAt(), now);
        return new Reservation(member, date, time, theme, reservationStatus);
    }

    private static void validateDateTime(LocalDate date, LocalTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new InvalidReservationException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final Reservation that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMember(), that.getMember())
                && Objects.equals(getDate(), that.getDate()) && Objects.equals(getTime(),
                that.getTime()) && Objects.equals(getTheme(), that.getTheme());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMember(), getDate(), getTime(), getTheme());
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }
}
