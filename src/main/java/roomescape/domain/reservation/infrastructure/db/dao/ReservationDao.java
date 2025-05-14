package roomescape.domain.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.model.entity.Reservation;

public interface ReservationDao {

    List<Reservation> selectAll();

    Reservation insertAndGet(Reservation reservation);

    Optional<Reservation> selectById(Long id);

    void deleteById(Long id);

    boolean existDuplicatedDateTime(LocalDate date, Long timeId, Long themeId);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    List<Reservation> selectByFilter(Long themeId, Long memberId, LocalDate from, LocalDate to);
}
