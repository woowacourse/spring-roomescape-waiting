package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public class ReservationSaveRequest {

    private Long memberId;

    @NotNull
    private LocalDate date;

    @NotNull
    private Long themeId;

    @NotNull
    private Long timeId;

    public ReservationSaveRequest() {
    }

    public ReservationSaveRequest(LocalDate date, Long themeId, Long timeId) {
        this.date = date;
        this.themeId = themeId;
        this.timeId = timeId;
    }

    public ReservationSaveRequest(Long memberId, LocalDate date, Long themeId, Long timeId) {
        this.memberId = memberId;
        this.date = date;
        this.themeId = themeId;
        this.timeId = timeId;
    }

    public Reservation toReservation(Member member, Theme theme, ReservationTime reservationTime) {
        return new Reservation(member, date, theme, reservationTime);
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getTimeId() {
        return timeId;
    }
}
