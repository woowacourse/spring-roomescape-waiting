package roomescape.dao;


import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;

public interface ReservationDao extends CommonDao<Reservation> {
    List<Reservation> findAll(int limit, int offset);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStoreId(Long storeId);

    long count();

    boolean selectForUpdateByThemeIdAndTimeIdAndDate(Long themeId, Long timeId, LocalDate date);

    boolean existsByThemeId(Long themeId);

    boolean existsByTimeId(Long timeId);
}
