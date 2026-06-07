package roomescape.service;

import roomescape.controller.dto.request.ReservationCreateRequest;

import java.time.LocalDate;

public class ReservationCreateCommand {
    private final String name;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    private ReservationCreateCommand(String name, LocalDate date, Long timeId, Long themeId) {
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;

    }

    public static ReservationCreateCommand from(ReservationCreateRequest request) {
        return new ReservationCreateCommand(
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
