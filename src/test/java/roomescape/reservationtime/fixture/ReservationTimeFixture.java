package roomescape.reservationtime.fixture;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.dto.ReservationTimeRequestDto;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;

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
