package roomescape.domain;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Waiting {
    private final Long id;
    private final String name;
    private final LocalDate reservationDate;
    private final ReservationTime reservationTime;
    private final Theme reservationTheme;
    private final LocalDateTime createAt;

    public static Waiting create(long id, String name, LocalDate reservationDate, ReservationTime reservationTime, Theme reservationTheme, LocalDateTime createdAt) {
        return new Waiting(id, name, reservationDate, reservationTime, reservationTheme, createdAt);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LocalDate reservationDate() {
        return reservationDate;
    }

    public ReservationTime reservationTime() {
        return reservationTime;
    }

    public Theme reservationTheme() {
        return reservationTheme;
    }

    public LocalDateTime createAt() {
        return createAt;
    }
}
