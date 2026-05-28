package roomescape.application.dto.command;

import java.time.LocalTime;
import roomescape.exception.client.InvalidCommandException;

public class ReservationTimeCreateCommand {
    private final LocalTime startAt;

    public ReservationTimeCreateCommand(LocalTime startAt) {
        validate(startAt);
        this.startAt = startAt;
    }


    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new InvalidCommandException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
