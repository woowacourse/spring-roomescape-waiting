package roomescape.feature.time.mapper;

import org.springframework.stereotype.Component;
import roomescape.feature.time.dto.command.TimeCreateCommand;
import roomescape.feature.time.dto.request.TimeCreateRequestDto;
import roomescape.feature.time.dto.response.ReservationTimeResponseDto;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@Component
public final class TimeMapper {

    public TimeMapper() {

    }

    public TimeCreateCommand toCreateCommand(TimeCreateRequestDto requestDto) {
        return new TimeCreateCommand(requestDto.startAt());
    }

    public TimeResponseDto toResponseDto(Time time) {
        return new TimeResponseDto(time.getId(), time.getStartAt());
    }

    public ReservationTimeResponseDto toReservationResponseDto(Time time) {
        return new ReservationTimeResponseDto(time.getId(), time.getStartAt(), time.getStatus() == EntityStatus.DELETED);
    }

    public TimeAvailabilityResponseDto toAvailabilityResponseDto(Time time, boolean available) {
        return new TimeAvailabilityResponseDto(time.getId(), time.getStartAt(), available);
    }
}
