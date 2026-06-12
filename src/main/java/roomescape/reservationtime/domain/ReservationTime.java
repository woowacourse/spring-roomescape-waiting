package roomescape.reservationtime.domain;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public class ReservationTime {

    private static final String START_AT_REQUIRED_MESSAGE = "예약 시작 시간을 입력해야 합니다.";

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);

        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime create(final LocalTime startAt) {
        return new ReservationTime(
                null,
                startAt
        );
    }

    public static ReservationTime of(final Long id, final LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }


    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException(START_AT_REQUIRED_MESSAGE);
        }
    }
}
