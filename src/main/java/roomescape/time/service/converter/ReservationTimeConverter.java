package roomescape.time.service.converter;

import java.util.List;
import roomescape.time.controller.dto.ReservationTimeWebResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.dto.CreateReservationTimeServiceRequest;

public class ReservationTimeConverter {

    public static ReservationTime toDomain(final CreateReservationTimeServiceRequest request) {
        return ReservationTime.withoutId(
                request.startAt());
    }

    public static ReservationTimeWebResponse toDto(final ReservationTime reservationTime) {
        return new ReservationTimeWebResponse(
                reservationTime.getId(),
                reservationTime.getStartAt());
    }

    public static List<ReservationTimeWebResponse> toDto(final List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(ReservationTimeConverter::toDto)
                .toList();
    }
}
