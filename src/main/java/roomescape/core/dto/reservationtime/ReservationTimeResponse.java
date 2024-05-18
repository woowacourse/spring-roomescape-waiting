package roomescape.core.dto.reservationtime;

import java.time.format.DateTimeFormatter;
import roomescape.core.domain.ReservationTime;

public class ReservationTimeResponse {
    public static final String TIME_PATTERN = "HH:mm";

    private Long id;
    private String startAt;

    public ReservationTimeResponse(final ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime);
    }

    public ReservationTimeResponse(final Long id, final ReservationTime time) {
        this.id = id;
        this.startAt = time.getStartAt().format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    public Long getId() {
        return id;
    }

    public String getStartAt() {
        return startAt;
    }
}
