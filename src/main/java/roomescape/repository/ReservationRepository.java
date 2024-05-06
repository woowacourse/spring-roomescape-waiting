package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    Optional<Reservation> findById(Long id);

    Reservation fetchById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemesAndDateAndTimeId(Long themeId, Long timeId, LocalDate date);

    boolean existsByThemeId(Long themeId);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<Theme> findPopularThemes(LocalDate from, LocalDate until, Integer limit);
}
