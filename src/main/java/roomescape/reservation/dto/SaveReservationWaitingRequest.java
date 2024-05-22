package roomescape.reservation.dto;

import java.time.LocalDate;

public record SaveReservationWaitingRequest(
        LocalDate date,
        Long memberId,
        Long time,
        Long theme
) {
    public SaveReservationWaitingRequest setMemberId(final Long memberId) {
        return new SaveReservationWaitingRequest(date, memberId, time, theme);
    }
}
