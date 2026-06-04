package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;

public class WaitingResult {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTimeResult time;
    private final ThemeResult theme;
    private final int orderIndex;

    public WaitingResult(Long id, String name, LocalDate date,
                         ReservationTimeResult time, ThemeResult theme, int orderIndex) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.orderIndex = orderIndex;
    }

    public static WaitingResult from(Waiting w) {
        return from(w.getId(), w.getName(), w.getSlot(), w.getOrderIndex());
    }

    public static WaitingResult from(Long id, String name, Slot slot, int orderIndex) {
        return new WaitingResult(
                id,
                name,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
                orderIndex
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

    public int getOrderIndex() {
        return orderIndex;
    }
}
