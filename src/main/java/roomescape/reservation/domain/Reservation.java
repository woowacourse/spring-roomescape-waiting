package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    private Reservation(final Member member, final LocalDate date, final ReservationTime time,
                        final Theme theme, final ReservationStatus reservationStatus) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = reservationStatus;
    }

    public Reservation() {
    }

    public static Reservation of(final LocalDate date, final Member member, final ReservationTime time,
                                 final Theme theme, final ReservationStatus reservationStatus) {
        return new Reservation(member, date, time, theme, reservationStatus);
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
