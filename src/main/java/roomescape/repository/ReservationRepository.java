package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAllByStoreIds(List<Long> storeIds, int limit, int offset);

    List<Reservation> findAllByStoreIdsAndName(List<Long> storeIds, String name, int limit, int offset);

    List<Reservation> findAllByUserId(Long userId);

    Optional<Reservation> findById(Long id);

    Long save(Reservation reservation);

    int deleteById(Long id);

    int update(Reservation reservation);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByReservationTimeId(Long timeId);
}