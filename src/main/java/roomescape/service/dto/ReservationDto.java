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

    public static ReservationDto of(ReservationRequest request, LoginMember member) {
        LocalDate date = request.getDate();
        long timeId = request.getTimeId();
        long themeId = request.getThemeId();
        long memberId = member.getId();
        return new ReservationDto(date, timeId, themeId, memberId);
    }

    public static ReservationDto from(AdminReservationRequest request) {
        LocalDate date = request.getDate();
        long timeId = request.getTimeId();
        long themeId = request.getThemeId();
        long memberId = request.getMemberId();
        return new ReservationDto(date, timeId, themeId, memberId);
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
