package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation create(String name, LocalDate date, ReservationTime time, Theme theme) {
        validateCreatableDateTime(date, time);
        return new Reservation(null, name, date, time, theme);
    }

    public Reservation update(String name, LocalDate date, ReservationTime time) {
        validateOwner(name);
        validateModifiable();
        validateModifiableDateTime(date, time);
        return new Reservation(id, this.name, date, time, theme);
    }

    public void cancel(String name) {
        validateOwner(name);
        validateModifiable();
    }

    public static Reservation createRow(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, name, date, time, theme);
    }

    public Reservation appendId(Long id) {
        return new Reservation(id, name, date, time, theme);
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

    private static void validateCreatableDateTime(LocalDate date, ReservationTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_CREATE_IN_PAST);
        }
    }

    private static void validateModifiableDateTime(LocalDate date, ReservationTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_MODIFY_IN_PAST);
        }
    }

    private void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_OWNER_MISMATCH);
        }
    }

    private void validateModifiable() {
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_MODIFY_IN_PAST);
        }
        if (date.isEqual(LocalDate.now()) && time.getStartAt().isBefore(LocalTime.now())) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_MODIFY_IN_PAST);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        if (this.id == null || that.id == null) {
            return false;
        }
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else {
            return System.identityHashCode(this);
        }
    }
}
