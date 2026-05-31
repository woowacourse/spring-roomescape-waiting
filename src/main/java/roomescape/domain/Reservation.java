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

    public Reservation(Long id, String reserverName, LocalDate date, ReservationTime time, Theme theme) {
        validate(reserverName, date, time, theme);
        this.id = id;
        this.reserverName = reserverName;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private static void validate(String reserverName, LocalDate date, ReservationTime time, Theme theme) {
        validateReserverName(reserverName);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
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
}
