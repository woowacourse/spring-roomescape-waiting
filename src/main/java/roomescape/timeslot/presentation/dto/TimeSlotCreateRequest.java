package roomescape.timeslot.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.timeslot.application.dto.TimeSlotCreateCommand;

public record TimeSlotCreateRequest(
        @NotNull(message = "시간을 입력해주세요.") LocalTime startAt
) {

    public TimeSlotCreateCommand convertToCreateCommand() {
        return new TimeSlotCreateCommand(startAt);
    }
}
