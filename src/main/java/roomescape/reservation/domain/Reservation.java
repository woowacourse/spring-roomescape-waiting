package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Date date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private Time time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus reservationStatus;

    protected Reservation() {
    }

    private Reservation(long id, Date date, Time time, Theme theme, Member member,
                        ReservationStatus reservationStatus) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.reservationStatus = reservationStatus;
    }

    public static Reservation of(LocalDate date, Time time, Theme theme, Member member,
                                 ReservationStatus reservationStatus) {
        validateAtSaveDateAndTime(date, time);
        return new Reservation(0, Date.saveFrom(date), time, theme, member, reservationStatus);
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date.getDate();
    }

    public Time getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public String getReservationStatus() {
        return reservationStatus.getStatus();
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public boolean isAfterCancelDate(LocalDate now) {
        return date.isAfterCancelDate(now);
    }

    private static void validateAtSaveDateAndTime(LocalDate date, Time time) {
        if (date.equals(LocalDate.now())) {
            validateTime(time);
        }
    }

    private static void validateTime(Time time) {
        if (time.isBeforeTime(LocalTime.now())) {
            throw new RoomEscapeException(ReservationExceptionCode.RESERVATION_TIME_IS_PAST_EXCEPTION);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
