package roomescape.reservation.fixture;

import roomescape.reservation.domain.Reservation;
import roomescape.slot.domain.ReservationSlot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ReservationFixture {

    public static Reservation reservation(String name, ReservationSlot slot) {
        return Reservation.reserve(name, slot.getId(), LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation reservation(String name, ReservationSlot slot, LocalDateTime reservedAt) {
        return Reservation.reserve(name, slot.getId(), reservedAt.truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation waitReservation(String name, ReservationSlot slot) {
        return Reservation.wait(name, slot.getId(), LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

}
