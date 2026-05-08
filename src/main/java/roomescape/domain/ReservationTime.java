package roomescape.domain;

import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@RequiredArgsConstructor
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public static ReservationTime create(long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public LocalTime startAt() {
        return startAt;
    }

    public long id() {
        return id;
    }
}
