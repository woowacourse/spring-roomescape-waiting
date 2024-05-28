package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

@Entity
public class Reservation {
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

    protected Reservation() {
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

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_DATE);
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_TIME);
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomescapeException(ExceptionType.EMPTY_THEME);
        }
    }

    public static ReservationBuilder builder() {
        return new ReservationBuilder();
    }

    public boolean isAuthor(Member member) {
        return this.reservationMember == member;
    }

    public void approve() {
        status = ReservationStatus.APPROVED;
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
