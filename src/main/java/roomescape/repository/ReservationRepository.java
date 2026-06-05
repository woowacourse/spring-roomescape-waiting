package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public interface ReservationRepository {

    List<Reservation> findAllByStoreIds(List<Long> storeIds, int limit, int offset);

    List<Reservation> findAllByStoreIdsAndName(List<Long> storeIds, String name, int limit, int offset);

    Map<Reservation, Integer> findAllByUserIdWithWaitingOrder(Long userId, int limit, int offset);

    Optional<Reservation> findById(Long id);

    Long save(Reservation reservation);

    int deleteById(Long id);

    int update(Reservation reservation);

    int updateStatus(Long id, ReservationStatus status);

    Optional<Reservation> findFirstWaitingBySlotId(Long slotId);

    boolean existsBySlotIdAndStatus(Long slotId, ReservationStatus status);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    boolean existsByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId, Long themeId, Long storeId);

    boolean existsByDateAndTimeAndThemeAndStoreAndUser(LocalDate date, Long timeId, Long themeId, Long storeId,
                                                       Long userId);

    boolean existsByReservationTimeId(Long timeId);
}
