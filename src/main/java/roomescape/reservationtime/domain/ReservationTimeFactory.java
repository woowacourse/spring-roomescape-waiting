package roomescape.reservationtime.domain;

import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class ReservationTimeFactory {

    public ReservationTime create(LocalTime startAt, LocalTime finishAt) {
        validate(startAt, finishAt);
        return ReservationTime.restore(null, startAt, finishAt);
    }

    private void validate(LocalTime startAt, LocalTime finishAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }
        if (finishAt == null) {
            throw new IllegalArgumentException("종료 시간은 필수입니다.");
        }
    }
}