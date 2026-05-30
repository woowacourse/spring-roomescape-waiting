package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.ReservationWithStatusResult;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAllByName(String name);

    Optional<Reservation> findById(Long id);

    List<ReservationWithStatusResult> findAllByNameWithStatus(String name);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndName(LocalDate date, Long timeId, String name);

    List<Reservation> findAll();

    List<PopularThemeQueryResult> findPopularThemes(LocalDate from, LocalDate to, int limit);

    boolean existsByDateAndTimeIdAndNameAndIdNot(LocalDate date, Long timeId, String name, Long id);

    void update(Reservation reservation);

    int deleteById(Long id);
}
