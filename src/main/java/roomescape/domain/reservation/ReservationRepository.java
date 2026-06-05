package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    int deleteById(Long id);

    int countByTimeId(Long timeId);

    int countByReservationDateId(Long dateId);

    List<Long> findReservedTimes(Long themeId, Long dateId);

    int countByThemeId(Long id);

    List<Reservation> findByName(String name);

    Optional<Reservation> findById(Long id);

    int updateReservation(Long id, Long dateId, Long timeId);

    boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId);
}
