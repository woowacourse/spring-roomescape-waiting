package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationDao {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean isExistsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean isExistsByTimeId(long timeId);

    boolean isExistsByThemeId(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);
}
