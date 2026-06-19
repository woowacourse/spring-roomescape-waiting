package roomescape.service;

import roomescape.controller.dto.request.ReservationUpdateRequest;

import java.time.LocalDate;

public class ReservationUpdateCommand {
    private final Long memberId;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public ReservationUpdateCommand(Long memberId, LocalDate date, Long timeId, Long themeId) {
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public static ReservationUpdateCommand from(ReservationUpdateRequest request) {
        return new ReservationUpdateCommand(
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
