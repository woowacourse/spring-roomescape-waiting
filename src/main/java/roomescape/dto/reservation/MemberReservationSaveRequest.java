package roomescape.dto.reservation;

import roomescape.dto.MemberResponse;

import java.time.LocalDate;

public record MemberReservationSaveRequest(
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ReservationSaveRequest generateReservationSaveRequest(MemberResponse memberResponse) {
        return new ReservationSaveRequest(memberResponse.id(), date, timeId, themeId);
    }
}
