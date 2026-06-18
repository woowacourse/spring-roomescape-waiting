package roomescape.reservation.domain;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_CANCELED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_PAST;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_WAITING;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_DATE_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ID_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_MEMBER_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_OWNER;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_RESERVED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_PAST_DATETIME_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_THEME_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_TIME_IS_NULL;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.date.domain.ReservationDate;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity(name = "reservation")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_id", nullable = false)
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(nullable = false)
    private Long waitingOrder;

    public static Reservation reserved(Member member, ReservationDate reservationDate,
        ReservationTime time, Theme theme) {
        return of(member, reservationDate, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation wait(Member member, ReservationDate reservationDate,
        ReservationTime time, Theme theme, Long waitingOrder) {
        return of(member, reservationDate, time, theme, ReservationStatus.WAITING, waitingOrder);
    }

    private static Reservation of(Member member, ReservationDate reservationDate,
        ReservationTime time, Theme theme, ReservationStatus status) {
        validate(member, reservationDate, time, theme);
        validatePast(reservationDate.getDate(), time.getStartAt());

        return new Reservation(null, member, reservationDate, time, theme, status, 0L);
    }

    private static Reservation of(Member member, ReservationDate reservationDate,
        ReservationTime time, Theme theme, ReservationStatus status, Long waitingOrder) {
        validate(member, reservationDate, time, theme);
        validatePast(reservationDate.getDate(), time.getStartAt());
        validateWaitingOrder(waitingOrder);

        return new Reservation(null, member, reservationDate, time, theme, status, waitingOrder);
    }

    public static Reservation load(Long id, Member member, ReservationDate reservationDate,
        ReservationTime time, Theme theme, ReservationStatus status, Long waitingOrder) {
        validate(member, reservationDate, time, theme);
        validateId(id);

        return new Reservation(id, member, reservationDate, time, theme, status, waitingOrder);
    }

    public void cancel(Member requester) {
        validateOwner(requester);
        validateNotCanceled();
        validateNotPast(date.getDate(), time.getStartAt());

        this.status = ReservationStatus.CANCELED;
    }

    public void cancelByManager() {
        validateNotCanceled();
        validateNotPast(date.getDate(), time.getStartAt());

        this.status = ReservationStatus.CANCELED;
    }

    public void changeSchedule(Member requester, ReservationDate newDate,
        ReservationTime newTime) {
        validateOwner(requester);
        validateNotCanceled();
        validateNotWaiting();
        validateReserved();
        validateNotPast(date.getDate(), time.getStartAt());
        validateNewScheduleIsPast(newDate.getDate(), newTime.getStartAt());

        this.date = newDate;
        this.time = newTime;
    }

    public void changeScheduleByManager(ReservationDate newDate, ReservationTime newTime) {
        validateNotCanceled();
        validateNotWaiting();
        validateReserved();
        validateNotPast(date.getDate(), time.getStartAt());
        validateNewScheduleIsPast(newDate.getDate(), newTime.getStartAt());

        this.date = newDate;
        this.time = newTime;
    }

    private static void validate(Member member, ReservationDate reservationDate, ReservationTime time,
        Theme theme) {
        validateMember(member);
        validateDate(reservationDate);
        validateTime(time);
        validateTheme(theme);
    }

    private static void validateMember(Member member) {
        if (member == null) {
            throw new ReservationException(RESERVATION_MEMBER_IS_NULL);
        }
    }

    private static void validateDate(ReservationDate date) {
        if (date == null) {
            throw new ReservationException(RESERVATION_DATE_IS_NULL);
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new ReservationException(RESERVATION_TIME_IS_NULL);
        }
    }

    private static void validatePast(LocalDate date, LocalTime time) {
        if (isPast(date, time)) {
            throw new ReservationException(RESERVATION_PAST_DATETIME_NOT_ALLOWED);
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new ReservationException(RESERVATION_THEME_IS_NULL);
        }
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new ReservationException(RESERVATION_ID_IS_NULL);
        }
    }

    private static void validateWaitingOrder(Long waitingOrder) {
        if (waitingOrder != null && waitingOrder < 0) {
            throw new IllegalStateException("대기 순서는 음수일 수 없습니다");
        }
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    public void changeToReserved() {
        this.status = ReservationStatus.RESERVED;
        this.waitingOrder = 0L;
    }

    public void changeToWaitingWithOrder(Long waitingOrder) {
        validateWaitingOrder(waitingOrder);
        this.status = ReservationStatus.WAITING;
        this.waitingOrder = waitingOrder;
    }

    private void validateOwner(Member requester) {
        if (!isOwner(requester)) {
            throw new ReservationException(RESERVATION_NOT_OWNER);
        }
    }

    public boolean isOwner(Member requester) {
        return member.hasSameId(requester);
    }

    private void validateReserved() {
        if (status != ReservationStatus.RESERVED) {
            throw new ReservationException(RESERVATION_NOT_RESERVED);
        }
    }

    private void validateNotCanceled() {
        if (status == ReservationStatus.CANCELED) {
            throw new ReservationException(RESERVATION_ALREADY_CANCELED);
        }
    }

    private void validateNotWaiting() {
        if (status == ReservationStatus.WAITING) {
            throw new ReservationException(RESERVATION_ALREADY_WAITING);
        }
    }

    private void validateNotPast(LocalDate date, LocalTime time) {
        if (isPast(date, time)) {
            throw new ReservationException(RESERVATION_ALREADY_PAST);
        }
    }

    private void validateNewScheduleIsPast(LocalDate date, LocalTime time) {
        if (isPast(date, time)) {
            throw new ReservationException(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED);
        }
    }

    private static boolean isPast(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time).isBefore(LocalDateTime.now());
    }

}
