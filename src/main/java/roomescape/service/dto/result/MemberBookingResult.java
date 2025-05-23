package roomescape.service.dto.result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.persistence.dto.MemberBookingProjection;

public record MemberBookingResult(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        BookingType bookingType,
        long rank
) {
    public static MemberBookingResult from(MemberBookingProjection projection) {
        return new MemberBookingResult(
                projection.getId(),
                projection.getThemeName(),
                projection.getDate(),
                projection.getTime(),
                BookingType.from(projection.getType()),
                projection.getRank()
        );
    }

    public static List<MemberBookingResult> from(List<MemberBookingProjection> projections) {
        return projections.stream()
                .map(MemberBookingResult::from)
                .toList();
    }
}
