package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;

public class ReservationResult {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTimeResult time;
    private final ThemeResult theme;

    public ReservationResult(
            Long id,
            String name,
            LocalDate date,
            ReservationTimeResult time,
            ThemeResult theme
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationResult from(Reservation reservation) {
        return from(reservation.getId(), reservation.getName(), reservation.getSlot());
    }

    public static ReservationResult from(Long id, String name, Slot slot) {
        return new ReservationResult(
                id,
                name,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme())
        );
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

    public ReservationTimeResult getTime() {
        return time;
    }

    public ThemeResult getTheme() {
        return theme;
    }
}
