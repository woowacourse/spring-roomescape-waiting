package roomescape.domain;

import java.time.LocalDate;

import org.springframework.lang.NonNull;

import roomescape.exception.Validator;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        Validator.validateNotNull(name);
        Validator.validateNotNull(date);
        Validator.validateNotNull(time);
        Validator.validateNotNull(theme);
        validateName(name);

        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
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

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("이름은 비거나 공백일 수 없습니다");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("이름은 255자를 초과할 수 없습니다.");
        }
    }
}
