package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.UnauthorizedException;

public class Reservation {

    private static final String NOT_OWNER = "본인의 예약이 아닙니다.";
    private static final String PAST_RESERVATION_CREATE_REJECTED = "지난 시각에는 예약할 수 없습니다.";
    private static final String EXPIRED_RESERVATION_UPDATE_REJECTED = "이미 지난 예약은 변경할 수 없습니다.";
    private static final String PAST_RESERVATION_UPDATE_REJECTED = "지난 시각으로 예약을 변경할 수 없습니다.";
    private static final String PAST_RESERVATION_CANCEL_REJECTED = "이미 지난 예약은 취소할 수 없습니다.";

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(null, name, date, time, theme);
    }

    public static Reservation createWith(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            LocalDateTime now
    ) {
        validateCreatable(date, time, now);
        return new Reservation(name, date, time, theme);
    }

    public Reservation updateWith(
            String name,
            LocalDate date,
            ReservationTime time,
            LocalDateTime now
    ) {
        validateOwner(name);
        validatePast(now, EXPIRED_RESERVATION_UPDATE_REJECTED);
        validateTargetNotPast(date, time, now);

        return new Reservation(
                this.id,
                name,
                date,
                time,
                this.theme
        );
    }

    public void cancelBy(String name, LocalDateTime now) {
        validateOwner(name);
        validatePast(now, PAST_RESERVATION_CANCEL_REJECTED);
    }

    public boolean isOwnedBy(String name) {
        return name.equals(this.name);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    // TODO: slot? Theme이 없어도 slot이라 부를 수 있는가?
    public boolean isSameSlot(LocalDate date, ReservationTime time) {
        return this.date.equals(date) && this.time.equals(time);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void validateOwner(String name) {
        if (!name.equals(this.name)) {
            throw new UnauthorizedException(NOT_OWNER);
        }
    }

    private void validatePast(LocalDateTime now, String message) {
        if (isPast(now)) {
            throw new BusinessRuleViolationException(message);
        }
    }

    private void validateTargetNotPast(LocalDate date, ReservationTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_UPDATE_REJECTED);
        }
    }

    private static void validateCreatable(LocalDate date, ReservationTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_CREATE_REJECTED);
        }
    }
}
