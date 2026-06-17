package roomescape.reservation;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.common.vo.Slot;
import roomescape.common.CommonDao;

public interface ReservationDao extends CommonDao<Reservation> {
    List<Reservation> findAll(int limit, int offset);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStoreId(Long storeId);

    long count();

    boolean existsBySlotForUpdate(Slot slot);

    Optional<Reservation> findBySlotKeyForUpdate(Long themeId, Long timeId, LocalDate date, Long storeId);

    List<Long> findExpiredPendingIdsWithoutOrder(LocalDateTime threshold);

    boolean existsByThemeId(Long themeId);

    boolean existsByTimeId(Long timeId);
}
