package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAll(Long memberId, Long themeId, LocalDate fromDate, LocalDate toDate);

    void deleteById(long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeAndTheme(LocalDate date, long timeId, long themeId);
}
