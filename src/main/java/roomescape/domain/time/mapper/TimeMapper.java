package roomescape.domain.time.mapper;

import roomescape.domain.time.dto.response.ReservationTimeResponseDto;
import roomescape.domain.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;

public final class TimeMapper {

    private TimeMapper() {

    }

    public static TimeResponseDto toResponseDto(Time time) {
        return new TimeResponseDto(time.getId(), time.getStartAt());
    }

    public static ReservationTimeResponseDto toReservationResponseDto(Time time) {
        return new ReservationTimeResponseDto(time.getId(), time.getStartAt(), time.getDeletedAt() != null);
    }

    public static TimeAvailabilityResponseDto toAvailabilityResponseDto(Time time, boolean available) {
        return new TimeAvailabilityResponseDto(time.getId(), time.getStartAt(), available);
    }
}
