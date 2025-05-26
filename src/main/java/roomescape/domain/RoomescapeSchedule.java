package roomescape.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;

@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class RoomescapeSchedule {

    @Embedded
    private ReservationDateTime dateTime;
    @ManyToOne
    private Theme theme;

    private RoomescapeSchedule(final ReservationDateTime dateTime, final Theme theme) {
        this.dateTime = dateTime;
        this.theme = theme;
    }

    public LocalDate date() {
        return dateTime.date();
    }

    public TimeSlot timeSlot() {
        return dateTime.timeSlot();
    }

    public static RoomescapeSchedule of(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        return new RoomescapeSchedule(ReservationDateTime.of(date, timeSlot), theme);
    }

    public static RoomescapeSchedule forReserve(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        return new RoomescapeSchedule(ReservationDateTime.forReserve(date, timeSlot), theme);
    }
}
