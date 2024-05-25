package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // TODO: 생성자 오바
    protected Reservation() {
    }

    public Reservation(
            Member reservationMember,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(null, reservationMember, date, time, theme, null);
    }

    public Reservation(long id, Reservation reservationBeforeSave) {
        this(
                id,
                reservationBeforeSave.reservationMember,
                reservationBeforeSave.date,
                reservationBeforeSave.time,
                reservationBeforeSave.theme,
                reservationBeforeSave.status
        );
    }

    public Reservation(
            Long id,
            Member reservationMember,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(id, reservationMember, date, time, theme, null);
    }

    public Reservation(
            Member reservationMember,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        this(null, reservationMember, date, time, theme, status);
    }

    public Reservation(
            Long id,
            Member reservationMember,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        validateMember(reservationMember);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);

        this.id = id;
        this.reservationMember = reservationMember;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private void validateMember(Member reservationMember) {
        if (reservationMember == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_MEMBER);
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_THEME);
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_TIME);
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_DATE);
        }
    }

    public boolean isAuthor(Member member) {
        return this.reservationMember == member;
    }

    @Override
    public int compareTo(Reservation other) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        LocalDateTime otherDateTime = LocalDateTime.of(other.date, other.time.getStartAt());
        return dateTime.compareTo(otherDateTime);
    }

    public LocalTime getTime() {
        return time.getStartAt();
    }

    public long getId() {
        return id;
    }

    public Member getMember() {
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

    public ReservationStatus getStatus() {
        return status;
    }
}
