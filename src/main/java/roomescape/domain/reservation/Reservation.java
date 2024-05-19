package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.user.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    public Reservation() {
    }

    public Reservation(final Long id, final ReservationDate date, final ReservationTime time, final Theme theme, final Member member, final ReservationStatus reservationStatus) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.reservationStatus = reservationStatus;
    }


    public static Reservation fromComplete(final Long id, final String date, final ReservationTime time, final Theme theme, final Member member) {
        return Reservation.from(id, date, time, theme, member, ReservationStatus.COMPLETE);
    }

    private static Reservation from(final Long id, final String date, final ReservationTime time, final Theme theme, final Member member, final ReservationStatus status) {
        return new Reservation(id, ReservationDate.from(date), time, theme, member, status);
    }

    public Long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public String getLocalDateTimeFormat() {
        return parseLocalDateTime().toString();
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public boolean isBefore(final LocalDate localDate, final LocalTime localTime) {
        return parseLocalDateTime().isBefore(LocalDateTime.of(localDate, localTime));
    }

    public LocalDateTime parseLocalDateTime() {
        return LocalDateTime.of(date.date(), this.time.getStartAt());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
