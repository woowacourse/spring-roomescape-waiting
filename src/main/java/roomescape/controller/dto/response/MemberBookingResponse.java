package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.service.dto.result.BookingType;
import roomescape.service.dto.result.MemberBookingResult;

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
                result.themeName(),
                result.date(),
                result.time(),
                getDisplayName(result.bookingType(), result.rank())
        );
    }

    private static String getDisplayName(BookingType bookingType, long rank) {
        StringBuilder sb = new StringBuilder();

        if(rank > 0) {
            sb.append(rank + "번째 ");
        }
        sb.append(bookingType.getDisplayName());

        return sb.toString();
    }

    public static List<MemberBookingResponse> from(List<MemberBookingResult> results) {
        return results.stream()
                .map(MemberBookingResponse::from)
                .toList();
    }
}
