package roomescape.domain.reservationwaiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingWithRank(ReservationWaiting waiting, long rank) {

    public long id() {
        return waiting.getId();
    }

    public String themeName() {
        return waiting.getThemeName();
    }

    public LocalDate date() {
        return waiting.getDate();
    }

    public LocalTime time() {
        return waiting.getTime();
    }
}
