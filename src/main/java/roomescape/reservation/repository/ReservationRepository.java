package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAllByName(String name);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAll();

    List<PopularThemeQueryResult> findPopularThemes(LocalDate from, LocalDate to, int limit);

    boolean existByDateAndTimeIdAndThemeIdExceptId(LocalDate date, Long timeId, Long themeId, Long id);

    void update(Reservation reservation);

    int deleteById(Long id);
}
