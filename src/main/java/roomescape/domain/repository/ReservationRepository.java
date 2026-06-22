package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public interface ReservationRepository {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    boolean existsByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId);

    boolean existsBySlotAndName(LocalDate date, Long timeId, Long themeId, String name);


    boolean existsByTimeId(Long timeId);

    List<Reservation> findByNameOrderByDateAscTimeAsc(String name);

    Optional<Reservation> findById(Long id);

    boolean existsByDateAndTimeAndThemeExcludingId(LocalDate date, Long timeId, Long themeId, Long excludeId);

    void updateDateAndTime(Long id, LocalDate date, Long timeId);

    boolean existsByThemeId(Long themeId);

    void updateStatus(Long id, ReservationStatus status);

}
