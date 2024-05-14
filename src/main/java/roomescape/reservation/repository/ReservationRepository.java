package roomescape.reservation.repository;

import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {
    List<Reservation> findAll();

    List<Reservation> searchReservations(SearchReservationsParams searchReservationsParams);

    Reservation save(Reservation reservation);

    int deleteById(Long reservationId);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existByTimeId(Long reservationTimeId);

    boolean existByThemeId(Long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);
}
