package roomescape.waiting.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@AllArgsConstructor
@Getter
public class WaitingWithRank {
    private final Waiting waiting;
    private final Long rank;

    public Long getId() {
        return waiting.getId();
    }

    public Theme getTheme() {
        return waiting.getTheme();
    }

    public LocalDate getDate() {
        return waiting.getDate();
    }

    public ReservationTime getTime() {
        return waiting.getTime();
    }
}
