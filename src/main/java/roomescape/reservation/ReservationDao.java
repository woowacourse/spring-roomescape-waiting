package roomescape.reservation;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.common.vo.Slot;
import roomescape.dao.CommonDao;

public interface ReservationDao extends CommonDao<Reservation> {
    List<Reservation> findAll(int limit, int offset);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStoreId(Long storeId);

    long count();

    boolean existsBySlotForUpdate(Slot slot);

    Optional<Reservation> findBySlotKeyForUpdate(Long themeId, Long timeId, LocalDate date, Long storeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByTimeId(Long timeId);
}
