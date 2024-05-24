package roomescape.service.dto;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.model.member.LoginMember;

import java.time.LocalDate;

public class ReservationDto {

    private final LocalDate date;
    private final long timeId;
    private final long themeId;
    private final long memberId;

    public ReservationDto(LocalDate date, Long timeId, Long themeId, Long memberId) {
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.memberId = memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }

    public long getThemeId() {
        return themeId;
    }

    public long getMemberId() {
        return memberId;
    }
}
