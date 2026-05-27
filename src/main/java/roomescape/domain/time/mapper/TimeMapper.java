package roomescape.domain.time.mapper;

import org.springframework.stereotype.Component;
import roomescape.domain.time.dto.command.TimeCreateCommand;
import roomescape.domain.time.dto.request.TimeCreateRequestDto;
import roomescape.domain.time.dto.response.ReservationTimeResponseDto;
import roomescape.domain.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;

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
        return new ReservationTimeResponseDto(time.getId(), time.getStartAt(), time.getDeletedAt() != null);
    }

    public TimeAvailabilityResponseDto toAvailabilityResponseDto(Time time, boolean available) {
        return new TimeAvailabilityResponseDto(time.getId(), time.getStartAt(), available);
    }
}
