package roomescape.adapter.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.application.dto.command.ReservationTimeCreateCommand;

public class ReservationTimeRequest {

    @NotNull(message = "시간을 입력해 주세요.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startAt;

    public ReservationTimeRequest() {
    }

    public ReservationTimeCreateCommand toCommand() {
        return new ReservationTimeCreateCommand(startAt);
    }
}
