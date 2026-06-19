package roomescape.service;

import roomescape.controller.dto.request.ReservationCreateRequest;

import java.time.LocalDate;

public class ReservationCreateCommand {
    private final Long memberId;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    private ReservationCreateCommand(Long memberId, LocalDate date, Long timeId, Long themeId) {
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public static ReservationCreateCommand from(ReservationCreateRequest request) {
        return new ReservationCreateCommand(
                request.getMemberId(),
                request.getDate(),
                request.getTimeId(),
                request.getThemeId()
        );
    }

    public Long getMemberId() {
        return memberId;
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
