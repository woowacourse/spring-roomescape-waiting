package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime requestedAt;
    private final ReservationStatus status;

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme,
                       LocalDateTime requestedAt, ReservationStatus status) {
        this(null, name, date, time, theme, requestedAt, status);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime requestedAt,
                       ReservationStatus status) {
        this.id = id;
        validateName(name);
        validateDate(date);
        validateTime(time);
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.requestedAt = requestedAt;
        this.status = status;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자명이 유효하지 않습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜가 유효하지 않습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간이 유효하지 않습니다.");
        }
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
