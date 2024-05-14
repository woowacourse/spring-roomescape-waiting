package roomescape.core.dto.reservationtime;

import roomescape.core.domain.ReservationTime;

public class BookedTimeResponse {
    private Long id;
    private String startAt;
    private boolean alreadyBooked;

    public BookedTimeResponse(final ReservationTime time, final boolean alreadyBooked) {
        this(time.getId(), time.getStartAtString(), alreadyBooked);
    }

    public BookedTimeResponse(final Long id, final String startAt, final boolean alreadyBooked) {
        this.id = id;
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public Long getId() {
        return id;
    }

    public String getStartAt() {
        return startAt;
    }

    public boolean isAlreadyBooked() {
        return alreadyBooked;
    }
}
