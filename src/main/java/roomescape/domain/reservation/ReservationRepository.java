package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.Theme;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    int deleteById(Long id);

    int countByTimeId(Long timeId);

    int countByReservationDateId(Long dateId);

    List<Long> findReservedTimes(Long themeId, Long dateId);

    List<Theme> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today);

    int countByThemeId(Long id);

    boolean existsReservation(Long timeId, Long dateId, Long themeId);

    boolean existsBySchedule(Long timeId, Long dateId, Long themeId);

    boolean existsOtherReservation(Long id, Long timeId, Long dateId, Long themeId);

    Optional<Reservation> findBySchedule(Long timeId, Long dateId, Long themeId);

    List<Reservation> findByName(String name);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> update(Long id, Reservation withoutId);
}
