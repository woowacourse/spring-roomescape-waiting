package roomescape.reservation.repository;

import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.model.Reservation;

import java.util.List;

public interface CustomReservationRepository {

    List<Reservation> searchReservations(SearchReservationsParams searchReservationsParams);
}
