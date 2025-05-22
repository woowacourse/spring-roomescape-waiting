package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.service.result.MemberBookingResult;

public record MemberBookingResponse(
        Long reservationId,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MemberBookingResponse from(MemberBookingResult result) {
        return new MemberBookingResponse(
                result.id(),
                result.theme().name(),
                result.date(),
                result.time().startAt(),
                result.status()
        );
    }

    public static List<MemberBookingResponse> from(List<MemberBookingResult> results) {
        return results.stream()
                .map(MemberBookingResponse::from)
                .toList();
    }
}
