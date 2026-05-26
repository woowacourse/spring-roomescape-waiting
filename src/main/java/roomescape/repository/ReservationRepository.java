package roomescape.repository;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean isExistBy(Long themeId, LocalDate date, Long reservationTimeId);

    boolean isExistBy(Long reservationId);

    List<Reservation> findByName(String name);

    void updateStatus(Reservation reservation);

    void updateDateAndTimeAndTheme(Reservation reservation);

    boolean existsByThemeId(long themeId);

    boolean existsByTimeId(long timeId);
}
