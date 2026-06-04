package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private String name;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private ReservationStatus status;

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, name, date, time, theme, status);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
                       ReservationStatus status) {
        this.id = id;
        this.status = status;
        validateName(name);
        validateDate(date);
        validateTime(time);
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }


    public boolean isSameDateTime(LocalDate date, Long timeId) {
        return this.date.equals(date) && this.time.getId().equals(timeId);
    }

    public void validateNotPast(LocalDate date, ReservationTime time) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, time.getStartAt());
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 지난 시간/날짜는 예약할 수 없습니다.");
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

    public ReservationStatus getStatus() {
        return status;
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
}
