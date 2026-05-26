package roomescape.domain.reservationtime;

import java.time.LocalTime;

public class AvailableReservationTime {

    private Long id;
    private LocalTime startAt;
    private boolean isAvailable;

    public AvailableReservationTime(Long id, LocalTime startAt, boolean isAvailable) {
        this.id = id;
        this.startAt = startAt;
        this.isAvailable = isAvailable;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
