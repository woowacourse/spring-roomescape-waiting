package roomescape.domain;

import java.time.LocalDate;
import roomescape.domain.exception.DomainValidationException;

public class Reservation {

    private static final int MAX_NAME_LENGTH = 30;

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
        validateNameLength(name);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private static void validateNameLength(String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("예약자 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다.");
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

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }
}
