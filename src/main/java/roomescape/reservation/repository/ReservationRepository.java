package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.param.QueryGenerator;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTime_IdAndTheme_Id(final ReservationDate date, final Long time_id, final Long theme_id);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findAllByDateAndTheme_Id(final ReservationDate date, final Long themeId);
}
