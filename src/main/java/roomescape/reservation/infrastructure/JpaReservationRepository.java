package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationCustomRepository {

    List<Reservation> findByDateAndTheme_Id(LocalDate date, Long themeId);

    boolean existsByTime_Id(Long timeId);

    boolean existsByDateAndTime_StartAtAndTheme_Id(LocalDate date, LocalTime time, Long themeId);

    boolean existsByTheme_Id(Long themeId);

}
