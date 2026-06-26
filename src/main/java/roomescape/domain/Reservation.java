package roomescape.domain;

import java.time.LocalDate;
import roomescape.domain.exception.DomainValidationException;

public class Reservation {
    private static final int MAX_NAME_LENGTH = 30;

    private final Long id;
    private final String reserverName;
    private final LocalDate date;
    private final ReservationTime time;

    private final Theme theme;
    private final ReservationStatus status;

    public Reservation(Long id, String reserverName, LocalDate date, ReservationTime time, Theme theme,
                       ReservationStatus status) {
        validate(reserverName, date, time, theme, status);
        this.id = id;
        this.reserverName = reserverName;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private static void validate(String reserverName, LocalDate date, ReservationTime time, Theme theme,
                                 ReservationStatus status) {
        validateReserverName(reserverName);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateStatus(status);
    }

    private static void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new DomainValidationException("예약 상태는 비어 있을 수 없습니다.");
        }
    }

    private static void validateReserverName(String reserverName) {
        if (reserverName == null || reserverName.isBlank()) {
            throw new DomainValidationException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (reserverName.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(
                    "예약자 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new DomainValidationException("예약 날짜는 비어 있을 수 없습니다.");
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new DomainValidationException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new DomainValidationException("예약 테마는 비어 있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getReserverName() {
        return reserverName;
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

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public boolean isCanceled() {
        return status == ReservationStatus.CANCELED;
    }

    public boolean isActive() {
        return status != ReservationStatus.CANCELED;
    }
}
