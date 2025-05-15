package roomescape.domain.time;

import java.util.Objects;

public record AvailableReservationTime(
        ReservationTime time,
        boolean available
) {
    public AvailableReservationTime(final ReservationTime time, final boolean available) {
        this.time = Objects.requireNonNull(time, "time은 null일 수 없습니다.");
        this.available = available;
    }
}
