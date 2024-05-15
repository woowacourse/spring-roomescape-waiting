package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

//    List<Reservation> searchReservations(SearchReservationsParams searchReservationsParams);

//    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByThemeId(Long themeId);

//    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);
}
