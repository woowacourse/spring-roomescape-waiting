package roomescape.reservationTime.fixture;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeRequestDto;
import roomescape.dto.response.ReservationTimeResponseDto;

public class ReservationTimeFixture {

    public static ReservationTimeRequestDto createRequestDto(LocalTime time) {
        return new ReservationTimeRequestDto(time);
    }

    public static ReservationTime create(LocalTime time) {
        ReservationTimeRequestDto requestDto = createRequestDto(time);
        return requestDto.toEntity();
    }

    public static ReservationTimeResponseDto createResponseDto(ReservationTime reservationTime) {
        return ReservationTimeResponseDto.of(reservationTime);
    }
}
