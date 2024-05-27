package roomescape.fixture;

import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

public class ReservationFixtures {

    private ReservationFixtures() {
    }

    public static Reservation createBookingReservation(Member member, LocalDate date, TimeSlot time, Theme theme) {
        return new Reservation(member, date, time, theme, ReservationStatus.BOOKING);
    }

    public static Reservation createPendingReservation(Member member, LocalDate date, TimeSlot time, Theme theme) {
        return new Reservation(member, date, time, theme, ReservationStatus.PENDING);
    }
}
