package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate date;
    private LocalDateTime requestedAt;
    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    public Reservation() {
    }

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime requestedAt) {
        validateName(name);
        validateDate(date);
        validateTime(time);
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.requestedAt = requestedAt;
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

    public boolean isRequestedBefore(Reservation other) {
        return requestedAt.isBefore(other.requestedAt);
    }

    public void changeSchedule(LocalDate date, ReservationTime time) {
        validateDate(date);
        validateTime(time);
        this.date = date;
        this.time = time;
    }
}
