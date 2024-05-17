package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.reservation.controller.dto.MemberReservationRequest;

public record MemberReservationCreate(long memberId, LocalDate date, long timeId, long themeId) {
    public static MemberReservationCreate from(MemberReservationRequest memberReservationRequest) {
        return new MemberReservationCreate(
                memberReservationRequest.memberId(),
                memberReservationRequest.date(),
                memberReservationRequest.timeId(),
                memberReservationRequest.themeId()
        );
    }
}
