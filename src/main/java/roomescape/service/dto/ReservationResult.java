package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Slot;

public class ReservationResult {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTimeResult time;
    private final ThemeResult theme;
    private final ReservationStatus status;

    public ReservationResult(
            Long id,
            String name,
            LocalDate date,
            ReservationTimeResult time,
            ThemeResult theme,
            ReservationStatus status
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static ReservationResult from(Reservation reservation) {
        return from(reservation.getId(), reservation.getName(), reservation.getSlot(), reservation.getStatus());
    }

    public static ReservationResult from(Long id, String name, Slot slot, ReservationStatus status) {
        return new ReservationResult(
                id,
                name,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
                status
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

    public ReservationStatus getStatus() {
        return status;
    }
}
