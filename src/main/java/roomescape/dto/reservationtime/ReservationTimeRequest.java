package roomescape.dto.reservationtime;

import roomescape.exception.InvalidInputException;

import java.time.LocalTime;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        validateStartAt(startAt);
    }

    @Override
    public LocalTime startAt() {
        return startAt;
    }

    private static void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new InvalidInputException("생성 시간은 필수입니다.");
        }
    }
}
