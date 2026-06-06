package roomescape.time.controller.dto.response;

import roomescape.time.repository.projection.AvailableSlotTime;

import java.time.LocalTime;

public record TimeOfSlotDetailDto(
        Long slotId,
        Long timeId,
        LocalTime startAt,
        boolean isActive
) {

    public static TimeOfSlotDetailDto from(AvailableSlotTime projection) {
        return new TimeOfSlotDetailDto(
                projection.slotId(),
                projection.timeId(),
                projection.startAt(),
                projection.isActive()
        );
    }

}
