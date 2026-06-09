package roomescape.reservation.fixture;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.slot.domain.ReservationSlot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ReservationFixture {

    public static Reservation reservation(String name, ReservationSlot slot) {
        return Reservation.reserve(name, slot.getId(), ReservationStatus.RESERVED, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation reservation(String name, ReservationSlot slot, LocalDateTime reservedAt) {
        return Reservation.reserve(name, slot.getId(), ReservationStatus.RESERVED, reservedAt.truncatedTo(ChronoUnit.MICROS));
    }

    public static Reservation waitReservation(String name, ReservationSlot slot) {
        return Reservation.reserve(name, slot.getId(), ReservationStatus.WAITING, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }

}
