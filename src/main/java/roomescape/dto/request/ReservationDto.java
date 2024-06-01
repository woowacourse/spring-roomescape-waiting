package roomescape.dto.request;

import java.time.LocalDate;

import static roomescape.dto.request.exception.InputValidator.validateNotNull;

public record ReservationDto(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId) {

    public ReservationDto {
        validateNotNull(memberId, date, timeId, themeId);
    }

    public static ReservationDto mapToApplicationDto(MemberReservationRequest memberReservationRequest, Long memberId) {        return new ReservationDto(
                memberId,
                memberReservationRequest.date(),
                memberReservationRequest.timeId(),
                memberReservationRequest.themeId()
        );
    }

    public static ReservationDto mapToApplicationDto(AdminReservationRequest adminReservationRequest) {
        return new ReservationDto(
                adminReservationRequest.memberId(),
                adminReservationRequest.date(),
                adminReservationRequest.timeId(),
                adminReservationRequest.themeId()
        );
    }
}
