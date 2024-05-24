package roomescape.service.dto;

import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;

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

    public Reservation toReservation(ReservationTime time, Theme theme, Member member) {
        return new Reservation(this.date, time, theme, member);
    }

    public Waiting toWaiting(Member member, ReservationTime time, Theme theme) {
        return new Waiting(this.date, time, theme, member);
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
