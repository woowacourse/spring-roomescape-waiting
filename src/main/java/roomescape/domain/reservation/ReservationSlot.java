package roomescape.domain.reservation;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;

@Embeddable
public record ReservationSlot(
    @Embedded
    ReservationDateTime dateTime,
    @ManyToOne
    Theme theme
) {

    public LocalDate date() {
        return dateTime.date();
    }

    public TimeSlot timeSlot() {
        return dateTime.timeSlot();
    }
}
