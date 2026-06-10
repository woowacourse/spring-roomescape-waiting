package roomescape.service;

import roomescape.controller.dto.request.ReservationUpdateRequest;

import java.time.LocalDate;

public class ReservationUpdateCommand {
    private final String name;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public ReservationUpdateCommand(String name, LocalDate date, Long timeId, Long themeId) {
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public static ReservationUpdateCommand from(ReservationUpdateRequest request) {
        return new ReservationUpdateCommand(
                request.getName(),
                request.getDate(),
                request.getTimeId(),
                request.getThemeId()
        );
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
