package roomescape.presentation.dto.response;

import roomescape.business.dto.ReservableReservationTimeDto;

import java.time.LocalTime;
import java.util.List;

public record BookedReservationTimeResponse(
        String id,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static BookedReservationTimeResponse from(ReservableReservationTimeDto dto) {
        return new BookedReservationTimeResponse(dto.id().value(), dto.startTime().value(), !dto.available());
    }

    public static List<BookedReservationTimeResponse> from(List<ReservableReservationTimeDto> dtos) {
        return dtos.stream()
                .map(BookedReservationTimeResponse::from)
                .toList();
    }
}
