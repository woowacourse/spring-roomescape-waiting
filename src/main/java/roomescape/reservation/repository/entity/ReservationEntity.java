package roomescape.reservation.repository.entity;

import java.time.LocalDate;
import roomescape.theme.repository.entity.ThemeEntity;
import roomescape.time.repository.entity.ReservationTimeEntity;

public class ReservationEntity {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTimeEntity time;
    private final ThemeEntity theme;

    public ReservationEntity(Long id, String name, LocalDate date, ReservationTimeEntity time, ThemeEntity theme) {
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

    public ReservationTimeEntity getTime() {
        return time;
    }

    public ThemeEntity getTheme() {
        return theme;
    }
}
