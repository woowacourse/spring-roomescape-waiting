package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import roomescape.domain.Reservation;

public interface ReservationRepository {
    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByPeriodAndMemberAndTheme(LocalDate start, LocalDate end, String memberName,
                                                    String themeName);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);

    void deleteAll();
}
