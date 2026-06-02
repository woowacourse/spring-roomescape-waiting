package roomescape.reservation.repository;

import roomescape.reservation.domain.ReservationSlot;

public interface ReservationSlotRepository {

    ReservationSlot upsert(ReservationSlot reservationSlot);

}
