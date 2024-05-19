package roomescape.domain;

import static roomescape.exception.ExceptionType.EMPTY_DATE;
import static roomescape.exception.ExceptionType.EMPTY_MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_THEME;
import static roomescape.exception.ExceptionType.EMPTY_TIME;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.RoomescapeException;

@Entity
public class Reservation implements Comparable<Reservation> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member reservationMember;
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(Member reservationMember, LocalDate date, ReservationTime time, Theme theme) {
        this(null, reservationMember, date, time, theme);
    }

    public Reservation(Long id, Member reservationMember, LocalDate date, ReservationTime time, Theme theme) {
        validateMember(reservationMember);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.id = id;
        this.reservationMember = reservationMember;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateMember(Member reservationMember) {
        if (reservationMember == null) {
            throw new RoomescapeException(EMPTY_MEMBER);
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomescapeException(EMPTY_THEME);
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new RoomescapeException(EMPTY_TIME);
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new RoomescapeException(EMPTY_DATE);
        }
    }

    public Reservation(long id, Reservation reservationBeforeSave) {
        this(id, reservationBeforeSave.reservationMember, reservationBeforeSave.date, reservationBeforeSave.time,
                reservationBeforeSave.theme);
    }

    @Override
    public int compareTo(Reservation other) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        LocalDateTime otherDateTime = LocalDateTime.of(other.date, other.time.getStartAt());
        return dateTime.compareTo(otherDateTime);
    }

    public boolean isBefore(LocalDateTime base) {
        return LocalDateTime.of(date, getTime()).isBefore(base);
    }

    public LocalTime getTime() {
        return time.getStartAt();
    }

    public boolean hasIdOf(long id) {
        return this.id == id;
    }

    public long getId() {
        return id;
    }

    public Member getReservationMember() {
        return reservationMember;
    }

    public String getName() {
        return reservationMember.getName();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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

        return Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationMember=" + reservationMember +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                '}';
    }
}
