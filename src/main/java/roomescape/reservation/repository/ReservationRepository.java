package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);
}
