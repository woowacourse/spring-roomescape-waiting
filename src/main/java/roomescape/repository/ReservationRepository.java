package roomescape.repository;

import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    Optional<Reservation> findById(long id);

    Reservation fetchById(long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemesAndDateAndTimeId(long themeId, long timeId, LocalDate date);

    boolean existsByThemeId(long themeId);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    List<Theme> findPopularThemes(LocalDate from, LocalDate until, int limit);

    List<Reservation> searchReservations(ReservationSearchCondition condition);
}
